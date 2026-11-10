package com.nebula.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nebula.app.data.model.Reminder;
import com.nebula.app.data.database.NebulaDatabase;
import com.nebula.app.receiver.ReminderReceiver;

import java.util.List;

/** Schedules / cancels the system alarm behind a {@link Reminder}. */
public final class ReminderScheduler {
    private ReminderScheduler() {}

    public static final String EXTRA_REMINDER_ID = "reminder_id";

    private static PendingIntent pending(Context c, int reminderId, int flags) {
        Intent i = new Intent(c, ReminderReceiver.class);
        i.putExtra(EXTRA_REMINDER_ID, reminderId);
        return PendingIntent.getBroadcast(c, reminderId, i, flags | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void schedule(Context c, Reminder r) {
        if (r.getTriggerTime() <= System.currentTimeMillis()) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(c, r.getId(), PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                // Exact alarms not granted: still fires, just within a small window.
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.getTriggerTime(), pi);
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.getTriggerTime(), pi);
            }
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, r.getTriggerTime(), pi);
        }
    }

    public static void cancel(Context c, int reminderId) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(c, reminderId, PendingIntent.FLAG_NO_CREATE);
        if (am != null && pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }

    /** Next fire time for a repeating reminder, or -1 if it should stop (event has started). */
    public static long nextTrigger(Reminder r) {
        long step;
        if (r.getRepeat() == Reminder.REPEAT_DAILY) step = 24L * 3600_000L;
        else if (r.getRepeat() == Reminder.REPEAT_WEEKLY) step = 7L * 24L * 3600_000L;
        else return -1;
        long next = r.getTriggerTime() + step;
        long now = System.currentTimeMillis();
        while (next <= now) next += step;           // skip any we missed (e.g. phone was off)
        return next < r.getEventTime() ? next : -1; // only repeat until the event starts
    }

    /**
     * Cancels every alarm that belongs to {@code owner} (call when they log out), optionally deleting the
     * rows too (guests). BLOCKING: call from a background thread.
     */
    public static void cancelAllForOwner(Context c, String owner, boolean deleteRows) {
        NebulaDatabase db = NebulaDatabase.getInstance(c);
        List<Reminder> list = db.reminderDao().getActiveForOwner(owner);
        for (Reminder r : list) cancel(c, r.getId());
        if (deleteRows) db.reminderDao().deleteForOwner(owner);
    }

    /**
     * Re-arms the future reminders of {@code owner} (call when they log in or after a reboot).
     * BLOCKING: call from a background thread.
     */
    public static void armAllForOwner(Context c, String owner) {
        NebulaDatabase db = NebulaDatabase.getInstance(c);
        long now = System.currentTimeMillis();
        for (Reminder r : db.reminderDao().getActiveForOwner(owner)) {
            if (r.getTriggerTime() > now) schedule(c, r);
            else if (r.getEventTime() <= now) db.reminderDao().deactivate(r.getId());
        }
    }
}
