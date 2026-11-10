package com.nebula.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nebula.app.util.LocalStore;
import com.nebula.app.util.NebulaScheduler;
import com.nebula.app.util.ReminderScheduler;

import java.util.concurrent.Executors;

/** Re-arms reminder alarms after a reboot (the system forgets them). */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String a = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || "android.intent.action.QUICKBOOT_POWERON".equals(a)) {
            final PendingResult result = goAsync();
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    NebulaScheduler.scheduleDailyPhoto(context);
                    if (LocalStore.isLoggedIn(context)) {
                        ReminderScheduler.armAllForOwner(context, LocalStore.ownerId(context));
                    }
                } finally {
                    result.finish();
                }
            });
        }
    }
}
