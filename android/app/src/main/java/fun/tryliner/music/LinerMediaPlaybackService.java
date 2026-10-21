package fun.tryliner.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinerMediaPlaybackService extends Service {
    static final String ACTION_UPDATE = "fun.tryliner.music.MEDIA_UPDATE";
    static final String ACTION_STOP = "fun.tryliner.music.MEDIA_STOP";
    static final String ACTION_HIDE = "fun.tryliner.music.MEDIA_HIDE";
    static final String ACTION_PLAY = "fun.tryliner.music.MEDIA_PLAY";
    static final String ACTION_PAUSE = "fun.tryliner.music.MEDIA_PAUSE";
    static final String ACTION_NEXT = "fun.tryliner.music.MEDIA_NEXT";
    static final String ACTION_PREVIOUS = "fun.tryliner.music.MEDIA_PREVIOUS";
    static final String ACTION_SEEK = "fun.tryliner.music.MEDIA_SEEK";
    private static final String CHANNEL_ID = "liner_playback";
    private static final int NOTIFICATION_ID = 4182;

    private MediaSession session;
    private NotificationManager notifications;
    private ExecutorService artworkExecutor;
    private String title = "Liner";
    private String artist = "";
    private String album = "Liner";
    private String artworkUrl = "";
    private long duration;
    private long position;
    private boolean playing;
    private Bitmap artwork;
    private long playbackStartedAt;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable progressTicker = new Runnable() {
        @Override public void run() {
            if (session == null || !session.isActive()) return;
            publishSession();
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        notifications = getSystemService(NotificationManager.class);
        artworkExecutor = Executors.newSingleThreadExecutor();
        createChannel();
        session = new MediaSession(this, "LinerPlayback");
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { dispatch(ACTION_PLAY); }
            @Override public void onPause() { dispatch(ACTION_PAUSE); }
            @Override public void onSkipToNext() { dispatch(ACTION_NEXT); }
            @Override public void onSkipToPrevious() { dispatch(ACTION_PREVIOUS); }
            @Override public void onSeekTo(long value) { dispatch(ACTION_SEEK, value); }
            @Override public void onStop() { dispatch(ACTION_PAUSE); }
        });
        session.setActive(false);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopPlaybackService();
            return START_NOT_STICKY;
        }
        if (ACTION_HIDE.equals(action)) {
            // The user swiped the notification away. Playback continues in the
            // WebView, but do not keep (or rebuild) the notification or the
            // service alive after that: the notification comes back only when
            // the player starts playing again and re-updates the session.
            if (!playing) stopPlaybackService();
            return START_NOT_STICKY;
        }
        if (ACTION_PLAY.equals(action) || ACTION_PAUSE.equals(action) || ACTION_NEXT.equals(action) || ACTION_PREVIOUS.equals(action)) {
            dispatch(action);
        } else if (ACTION_SEEK.equals(action)) {
            dispatch(ACTION_SEEK, intent.getLongExtra("position", 0));
        }
        if (ACTION_UPDATE.equals(action)) applyUpdate(intent);
        session.setActive(true);
        startForeground(NOTIFICATION_ID, buildNotification());
        publishSession();
        handler.removeCallbacks(progressTicker);
        handler.postDelayed(progressTicker, 1000);
        return START_NOT_STICKY;
    }

    private void applyUpdate(Intent intent) {
        title = intent.getStringExtra("title"); if (title == null) title = "Liner";
        artist = intent.getStringExtra("artist"); if (artist == null) artist = "";
        album = intent.getStringExtra("album"); if (album == null) album = "Liner";
        duration = Math.max(0, intent.getLongExtra("duration", 0));
        position = Math.max(0, intent.getLongExtra("position", 0));
        playing = intent.getBooleanExtra("playing", false);
        playbackStartedAt = SystemClock.elapsedRealtime();
        String nextArtwork = intent.getStringExtra("artwork");
        if (nextArtwork != null && !nextArtwork.equals(artworkUrl)) {
            artworkUrl = nextArtwork;
            loadArtwork(nextArtwork);
        }
    }

    private void loadArtwork(String uri) {
        if (uri.isEmpty()) { artwork = null; publishSession(); return; }
        artworkExecutor.execute(() -> {
            try (InputStream in = new URL(uri).openStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (bitmap != null) {
                    artwork = Bitmap.createScaledBitmap(bitmap, 512, 512, true);
                    publishSession();
                }
            } catch (Exception ignored) { }
        });
    }

    private void publishSession() {
        if (session == null) return;
        MediaMetadata.Builder metadata = new MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration);
        if (artwork != null) metadata.putBitmap(MediaMetadata.METADATA_KEY_ART, artwork);
        session.setMetadata(metadata.build());
        long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE
            | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS
            | PlaybackState.ACTION_SEEK_TO | PlaybackState.ACTION_STOP;
        session.setPlaybackState(new PlaybackState.Builder()
            .setActions(actions)
            .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                position + (playing ? Math.max(0, SystemClock.elapsedRealtime() - playbackStartedAt) : 0), 1f, SystemClock.elapsedRealtime())
            .build());
        if (notifications != null && session.isActive()) notifications.notify(NOTIFICATION_ID, buildNotification());
    }

    private Notification buildNotification() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent content = PendingIntent.getActivity(this, 0, launch,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent hide = new Intent(this, LinerMediaPlaybackService.class).setAction(ACTION_HIDE);
        PendingIntent delete = PendingIntent.getService(this, ACTION_HIDE.hashCode(), hide,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);
        builder.setContentTitle(title).setContentText(artist).setSubText("Liner")
            .setSmallIcon(R.drawable.ic_stat_liner).setContentIntent(content)
            .setShowWhen(false).setOngoing(playing).setVisibility(Notification.VISIBILITY_PUBLIC)
            .setDeleteIntent(delete);
        if (artwork != null) builder.setLargeIcon(artwork);
        builder.addAction(action(R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(action(playing ? R.drawable.ic_media_pause : R.drawable.ic_media_play,
            playing ? "Pause" : "Play", playing ? ACTION_PAUSE : ACTION_PLAY));
        builder.addAction(action(R.drawable.ic_media_next, "Next", ACTION_NEXT));
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setStyle(new Notification.MediaStyle().setMediaSession(session == null ? null : session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        }
        return builder.build();
    }

    private Notification.Action action(int icon, String label, String action) {
        Intent intent = new Intent(this, LinerMediaPlaybackService.class).setAction(action);
        PendingIntent pending = PendingIntent.getService(this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Action.Builder(icon, label, pending).build();
    }

    private void dispatch(String action) { dispatch(action, null); }
    private void dispatch(String action, Long value) {
        if (MainActivity.instance != null) {
            MainActivity.instance.dispatchMediaAction(action, value);
            return;
        }
        android.content.SharedPreferences pending = getSharedPreferences("liner_media", MODE_PRIVATE);
        pending.edit().putString("pending_action", action)
            .putLong("pending_value", value == null ? -1 : value).apply();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music playback", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Playback controls and current track");
            notifications.createNotificationChannel(channel);
        }
    }

    private void stopPlaybackService() {
        if (session != null) { session.setActive(false); session.release(); session = null; }
        handler.removeCallbacks(progressTicker);
        if (notifications != null) notifications.cancel(NOTIFICATION_ID);
        stopForeground(true);
        stopSelf();
    }

    @Override public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // The app was swiped away from recents: the WebView (and playback with
        // it) is gone, so the foreground service must not linger with a stale
        // "playing" notification.
        stopPlaybackService();
    }

    @Override public void onDestroy() {
        if (session != null) { session.setActive(false); session.release(); session = null; }
        handler.removeCallbacks(progressTicker);
        if (artworkExecutor != null) artworkExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    static Intent updateIntent(android.content.Context context, String title, String artist, String album,
                               String artwork, long duration, long position, boolean playing) {
        return new Intent(context, LinerMediaPlaybackService.class).setAction(ACTION_UPDATE)
            .putExtra("title", title).putExtra("artist", artist).putExtra("album", album)
            .putExtra("artwork", artwork).putExtra("duration", duration).putExtra("position", position)
            .putExtra("playing", playing);
    }
}
