package fun.tryliner.music;

import android.content.Intent;
import android.os.Build;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONObject;

@CapacitorPlugin(name = "LinerMediaSession")
public class LinerMediaSessionPlugin extends Plugin {
    @PluginMethod public void consumePendingAction(PluginCall call) {
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("liner_media", android.content.Context.MODE_PRIVATE);
        JSObject result = new JSObject();
        String action = prefs.getString("pending_action", null);
        long value = prefs.getLong("pending_value", -1);
        if (action != null) {
            result.put("action", action);
            if (value >= 0) result.put("value", value);
            prefs.edit().remove("pending_action").remove("pending_value").apply();
        }
        call.resolve(result);
    }

    @PluginMethod public void update(PluginCall call) {
        try {
            JSONObject data = new JSONObject(call.getString("metadata", "{}"));
            Intent service = LinerMediaPlaybackService.updateIntent(
                getContext(), data.optString("title", "Liner"), data.optString("artist", ""),
                data.optString("album", "Liner"), data.optString("artwork", ""),
                data.optLong("duration", 0), data.optLong("position", 0), data.optBoolean("playing", false));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(getContext(), service);
            else getContext().startService(service);
            call.resolve();
        } catch (Exception error) { call.reject("Unable to update Android media controls", error); }
    }

    @PluginMethod public void clear(PluginCall call) {
        getContext().startService(new Intent(getContext(), LinerMediaPlaybackService.class).setAction(LinerMediaPlaybackService.ACTION_STOP));
        call.resolve();
    }
}
