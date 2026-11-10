package com.nebula.app.util;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nebula.app.worker.DailyPhotoWorker;
import com.nebula.app.worker.HourlyPhotoWorker;
import com.nebula.app.worker.InactivityWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/** Schedules the daily space-photo notification and the "you haven't opened the app" nudge. */
public final class NebulaScheduler {
    private NebulaScheduler() {}

    private static final String WORK_DAILY_PHOTO = "nebula_daily_photo";
    private static final String WORK_HOURLY_PHOTO = "nebula_hourly_photo";
    public static final String TAG_INACTIVITY = "nebula_inactivity";
    public static final long DAY_MS = 24L * 3600_000L;

    /** Hour of day (local time) the daily photo notification should arrive. */
    private static final int DAILY_HOUR = 19;

    // ---- daily photo ----
    public static void scheduleDailyPhoto(Context c) {
        Context app = c.getApplicationContext();
        WorkManager wm = WorkManager.getInstance(app);
        if (!NotifPrefs.dailyPhotoEnabled(app)) { wm.cancelUniqueWork(WORK_DAILY_PHOTO); return; }

        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(DailyPhotoWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(msUntilNext(DAILY_HOUR), TimeUnit.MILLISECONDS)
                .build();
        // KEEP = don't reset the timer every time the app process starts
        wm.enqueueUniquePeriodicWork(WORK_DAILY_PHOTO, ExistingPeriodicWorkPolicy.KEEP, req);
    }

    public static void cancelDailyPhoto(Context c) {
        WorkManager.getInstance(c.getApplicationContext()).cancelUniqueWork(WORK_DAILY_PHOTO);
    }

    // ---- hourly space headline ----
    public static void scheduleHourlyPhoto(Context c) {
        Context app = c.getApplicationContext();
        WorkManager wm = WorkManager.getInstance(app);
        if (!NotifPrefs.hourlyEnabled(app)) { wm.cancelUniqueWork(WORK_HOURLY_PHOTO); return; }

        // needs a connection to fetch the picture; if offline, WorkManager just waits for one
        Constraints online = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(HourlyPhotoWorker.class, 1, TimeUnit.HOURS)
                .setInitialDelay(msUntilNextHour(), TimeUnit.MILLISECONDS)   // first one on the next full hour
                .setConstraints(online)
                .build();
        wm.enqueueUniquePeriodicWork(WORK_HOURLY_PHOTO, ExistingPeriodicWorkPolicy.KEEP, req);
    }

    public static void cancelHourlyPhoto(Context c) {
        WorkManager.getInstance(c.getApplicationContext()).cancelUniqueWork(WORK_HOURLY_PHOTO);
    }

    private static long msUntilNextHour() {
        long hour = 3600_000L;
        long now = System.currentTimeMillis();
        return hour - (now % hour);
    }

    // ---- inactivity nudge ----
    /** Starts (or restarts) the countdown: fires {@code delayMs} from now unless the app is opened. */
    public static void scheduleInactivity(Context c, long delayMs) {
        Context app = c.getApplicationContext();
        if (!NotifPrefs.nudgeEnabled(app)) return;
        WorkManager wm = WorkManager.getInstance(app);
        wm.cancelAllWorkByTag(TAG_INACTIVITY);
        wm.enqueue(new OneTimeWorkRequest.Builder(InactivityWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(TAG_INACTIVITY)
                .build());
    }

    /** Queues another nudge without cancelling the one currently running. */
    public static void scheduleNextNudge(Context c) {
        Context app = c.getApplicationContext();
        WorkManager.getInstance(app).enqueue(new OneTimeWorkRequest.Builder(InactivityWorker.class)
                .setInitialDelay(DAY_MS, TimeUnit.MILLISECONDS)
                .addTag(TAG_INACTIVITY)
                .build());
    }

    public static void cancelInactivity(Context c) {
        WorkManager.getInstance(c.getApplicationContext()).cancelAllWorkByTag(TAG_INACTIVITY);
    }

    private static long msUntilNext(int hour) {
        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1);
        return next.getTimeInMillis() - now.getTimeInMillis();
    }
}
