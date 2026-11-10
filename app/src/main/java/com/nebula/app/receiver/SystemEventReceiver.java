package com.nebula.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

/**
 * Listens for SYSTEM broadcasts while the app is on screen: airplane-mode changes,
 * battery low / OK, and Battery Saver on/off. It is registered in code (not in the manifest)
 * because Android no longer delivers these implicit broadcasts to manifest receivers.
 */
public class SystemEventReceiver extends BroadcastReceiver {

    public interface Listener {
        /** @param message text to show the user; @param conserveBattery true if heavy work should stop */
        void onSystemEvent(String message, boolean conserveBattery);
    }

    private final Listener listener;

    public SystemEventReceiver(Listener listener) { this.listener = listener; }

    public static IntentFilter filter() {
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        f.addAction(Intent.ACTION_BATTERY_LOW);
        f.addAction(Intent.ACTION_BATTERY_OKAY);
        f.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        return f;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                boolean on = intent.getBooleanExtra("state", false);
                listener.onSystemEvent(on
                        ? "Airplane mode is on. ISS tracking, space photos and Socials need a connection."
                        : "Airplane mode is off. Reconnecting…", false);
                break;
            case Intent.ACTION_BATTERY_LOW:
                listener.onSystemEvent("Battery is low. Live ISS tracking was stopped to save power.", true);
                break;
            case Intent.ACTION_BATTERY_OKAY:
                listener.onSystemEvent("Battery is OK again. You can restart ISS tracking from the Track tab.", false);
                break;
            case PowerManager.ACTION_POWER_SAVE_MODE_CHANGED:
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean saver = pm != null && pm.isPowerSaveMode();
                listener.onSystemEvent(saver
                        ? "Battery Saver is on. Live ISS tracking was stopped to save power."
                        : "Battery Saver is off.", saver);
                break;
            default:
                break;
        }
    }
}
