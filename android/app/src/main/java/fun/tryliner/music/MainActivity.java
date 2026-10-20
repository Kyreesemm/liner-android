package fun.tryliner.music;

import com.getcapacitor.BridgeActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;

public class MainActivity extends BridgeActivity {
    static volatile MainActivity instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(LinerSignerPlugin.class);
        registerPlugin(LinerMediaSessionPlugin.class);
        super.onCreate(savedInstanceState);
        instance = this;
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 4182);
        }
    }

    @Override
    public void onBackPressed() {
        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().evaluateJavascript(
                "window.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',code:'Escape',bubbles:true}))",
                null
            );
            return;
        }
        super.onBackPressed();
    }

    void dispatchMediaAction(String action, Long value) {
        runOnUiThread(() -> {
            if (getBridge() == null) return;
            String detail = value == null ? "null" : String.valueOf(value);
            getBridge().eval("window.dispatchEvent(new CustomEvent('liner-media-action',{detail:{action:'" + action + "',value:" + detail + "}}))", null);
        });
    }

    @Override public void onDestroy() {
        if (instance == this) instance = null;
        super.onDestroy();
    }
}
