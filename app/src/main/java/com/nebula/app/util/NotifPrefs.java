package com.nebula.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Notification settings and "last time the app was opened" bookkeeping. */
public final class NotifPrefs {
    private NotifPrefs() {}
    private static SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences("nebula_notif", Context.MODE_PRIVATE);
    }

    public static boolean dailyPhotoEnabled(Context c) { return p(c).getBoolean("daily_photo", true); }
    public static void setDailyPhotoEnabled(Context c, boolean on) { p(c).edit().putBoolean("daily_photo", on).apply(); }

    public static boolean hourlyEnabled(Context c) { return p(c).getBoolean("hourly_photo", true); }
    public static void setHourlyEnabled(Context c, boolean on) { p(c).edit().putBoolean("hourly_photo", on).apply(); }

    public static boolean nudgeEnabled(Context c) { return p(c).getBoolean("nudge", true); }
    public static void setNudgeEnabled(Context c, boolean on) { p(c).edit().putBoolean("nudge", on).apply(); }

    public static boolean soundEnabled(Context c) { return p(c).getBoolean("sound_fx", true); }
    public static void setSoundEnabled(Context c, boolean on) { p(c).edit().putBoolean("sound_fx", on).apply(); }

    public static long lastOpen(Context c) { return p(c).getLong("last_open", System.currentTimeMillis()); }
    public static int nudgesSent(Context c) { return p(c).getInt("nudges_sent", 0); }
    public static void addNudgeSent(Context c) { p(c).edit().putInt("nudges_sent", nudgesSent(c) + 1).apply(); }

    /** Call whenever the user is in the app. Resets the inactivity clock. */
    public static void markOpened(Context c) {
        p(c).edit().putLong("last_open", System.currentTimeMillis()).putInt("nudges_sent", 0).apply();
    }
}
