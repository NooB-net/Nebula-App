package com.nebula.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.nebula.app.util.NebulaScheduler;

public class NebulaApplication extends Application {
    public static final String CHANNEL_ID = "nebula_reminders";
    public static final String CHANNEL_ID_ISS = "nebula_iss";
    public static final String CHANNEL_ID_DAILY = "nebula_daily_photo";
    public static final String CHANNEL_ID_NUDGE = "nebula_nudge";
    public static final String CHANNEL_ID_HOURLY = "nebula_hourly_headline";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        NebulaScheduler.scheduleDailyPhoto(this);
        NebulaScheduler.scheduleHourlyPhoto(this);
        com.nebula.app.util.Sounds.init(this);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription(getString(R.string.notification_channel_desc));

            NotificationChannel issChannel = new NotificationChannel(
                    CHANNEL_ID_ISS,
                    "ISS Tracker",
                    NotificationManager.IMPORTANCE_LOW
            );
            issChannel.setDescription("Live ISS tracking service");

            NotificationChannel dailyChannel = new NotificationChannel(
                    CHANNEL_ID_DAILY,
                    getString(R.string.channel_daily_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            dailyChannel.setDescription(getString(R.string.channel_daily_desc));

            NotificationChannel nudgeChannel = new NotificationChannel(
                    CHANNEL_ID_NUDGE,
                    getString(R.string.channel_nudge_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nudgeChannel.setDescription(getString(R.string.channel_nudge_desc));

            // low importance: shows in the shade with its picture, but never makes a sound every hour
            NotificationChannel hourlyChannel = new NotificationChannel(
                    CHANNEL_ID_HOURLY,
                    getString(R.string.channel_hourly_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            hourlyChannel.setDescription(getString(R.string.channel_hourly_desc));

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(dailyChannel);
                manager.createNotificationChannel(hourlyChannel);
                manager.createNotificationChannel(nudgeChannel);
                manager.createNotificationChannel(reminderChannel);
                manager.createNotificationChannel(issChannel);
            }
        }
    }
}
