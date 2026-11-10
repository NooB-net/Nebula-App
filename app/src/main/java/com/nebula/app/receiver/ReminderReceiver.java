package com.nebula.app.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.nebula.app.NebulaApplication;
import com.nebula.app.R;
import com.nebula.app.data.database.NebulaDatabase;
import com.nebula.app.data.model.Reminder;
import com.nebula.app.ui.MainActivity;
import com.nebula.app.util.LocalStore;
import com.nebula.app.util.ReminderScheduler;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        final int id = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1);
        if (id < 0) return;
        final PendingResult result = goAsync();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                NebulaDatabase db = NebulaDatabase.getInstance(context);
                Reminder r = db.reminderDao().get(id);
                if (r == null || !r.isActive()) return;
                // Reminders are personal: only show it if the account that set it is the one signed in.
                if (!LocalStore.isLoggedIn(context) || r.getOwnerId() == null
                        || !r.getOwnerId().equals(LocalStore.ownerId(context))) return;

                show(context, r);

                long next = ReminderScheduler.nextTrigger(r);
                if (next > 0) {
                    r.setTriggerTime(next);
                    db.reminderDao().insert(r);
                    ReminderScheduler.schedule(context, r);
                } else {
                    db.reminderDao().deactivate(id);
                }
            } finally {
                result.finish();
            }
        });
    }

    private void show(Context context, Reminder r) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, r.getId(), mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String body = (r.getNote() != null && !r.getNote().isEmpty())
                ? r.getNote() + "\n" + whenText(r.getEventTime())
                : whenText(r.getEventTime());

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NebulaApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(r.getEventName())
                .setContentText(whenText(r.getEventTime()))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            try { nm.notify(10_000 + r.getId(), b.build()); } catch (SecurityException ignored) { }
        }
    }

    /** Relative text computed when the notification fires, e.g. "Starts in 30 min". */
    static String whenText(long eventTime) {
        long diff = eventTime - System.currentTimeMillis();
        if (diff <= 60_000L) return "Happening now";
        long mins = diff / 60_000L;
        if (mins < 60) return "Starts in " + mins + " min";
        long hours = mins / 60;
        if (hours < 24) return "Starts in " + hours + " h" + (mins % 60 != 0 ? " " + (mins % 60) + " min" : "");
        return "Starts " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(eventTime));
    }
}
