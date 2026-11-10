package com.nebula.app.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nebula.app.util.NebulaScheduler;
import com.nebula.app.util.NotifPrefs;
import com.nebula.app.util.Notifier;

/**
 * Armed every time the app goes to the background and cancelled whenever it is opened, so it only
 * fires after a full day without opening the app. Nudges at most 3 days in a row.
 */
public class InactivityWorker extends Worker {
    private static final int MAX_NUDGES = 3;

    public InactivityWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        if (!NotifPrefs.nudgeEnabled(ctx)) return Result.success();

        long idle = System.currentTimeMillis() - NotifPrefs.lastOpen(ctx);
        if (idle < NebulaScheduler.DAY_MS - 30 * 60_000L) return Result.success();

        Notifier.postMissTheStars(ctx);
        NotifPrefs.addNudgeSent(ctx);
        if (NotifPrefs.nudgesSent(ctx) < MAX_NUDGES) {
            NebulaScheduler.scheduleNextNudge(ctx);
        }
        return Result.success();
    }
}
