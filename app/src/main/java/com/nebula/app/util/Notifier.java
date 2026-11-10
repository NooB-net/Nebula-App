package com.nebula.app.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.nebula.app.NebulaApplication;
import com.nebula.app.R;
import com.nebula.app.data.model.SpacePhoto;
import com.nebula.app.ui.MainActivity;
import com.nebula.app.ui.home.SpacePhotoDetailActivity;

/** Builds the app's "engagement" notifications. */
public final class Notifier {
    private Notifier() {}

    private static final int ID_DAILY_PHOTO = 2001;
    private static final int ID_NUDGE = 2002;
    private static final int ID_HOURLY = 2003;

    public static void postDailyPhoto(Context ctx, SpacePhoto p, Bitmap image) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (!nm.areNotificationsEnabled()) return;

        Intent detail = new Intent(ctx, SpacePhotoDetailActivity.class)
                .putExtra(SpacePhotoDetailActivity.EXTRA_PHOTO, p);
        PendingIntent pi = TaskStackBuilder.create(ctx)
                .addNextIntent(new Intent(ctx, MainActivity.class))
                .addNextIntent(detail)
                .getPendingIntent(ID_DAILY_PHOTO, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = p.getHook() != null ? p.getHook() : "✨ " + p.getTitle();
        String summary = shorten(p.getDescription(), 140);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, NebulaApplication.CHANNEL_ID_DAILY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(summary)
                .setSubText("Today's space photo")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);

        if (image != null) {
            b.setLargeIcon(image);
            b.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image).setSummaryText(summary));
        } else {
            b.setStyle(new NotificationCompat.BigTextStyle().bigText(shorten(p.getDescription(), 400)));
        }
        try { nm.notify(ID_DAILY_PHOTO, b.build()); } catch (SecurityException ignored) { }
    }

    /**
     * Hourly "news" notification: just a headline and a small picture, nothing else. Each hour's
     * notification replaces the previous one so they never pile up in the shade.
     */
    public static void postHourlyHeadline(Context ctx, SpacePhoto p, Bitmap thumb) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (!nm.areNotificationsEnabled()) return;

        Intent detail = new Intent(ctx, SpacePhotoDetailActivity.class)
                .putExtra(SpacePhotoDetailActivity.EXTRA_PHOTO, p);
        PendingIntent pi = TaskStackBuilder.create(ctx)
                .addNextIntent(new Intent(ctx, MainActivity.class))
                .addNextIntent(detail)
                .getPendingIntent(ID_HOURLY, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String headline = p.getHook() != null ? p.getHook() : p.getTitle();
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, NebulaApplication.CHANNEL_ID_HOURLY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(shorten(headline, 90))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(pi);
        if (thumb != null) b.setLargeIcon(thumb);       // the small picture on the right
        try { nm.notify(ID_HOURLY, b.build()); } catch (SecurityException ignored) { }
    }

    public static void postMissTheStars(Context ctx) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (!nm.areNotificationsEnabled()) return;

        PendingIntent pi = PendingIntent.getActivity(ctx, ID_NUDGE, new Intent(ctx, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, NebulaApplication.CHANNEL_ID_NUDGE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(ctx.getString(R.string.nudge_title))
                .setContentText(ctx.getString(R.string.nudge_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);
        try { nm.notify(ID_NUDGE, b.build()); } catch (SecurityException ignored) { }
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        int cut = s.lastIndexOf(' ', max);
        return s.substring(0, cut > max / 2 ? cut : max).trim() + "…";
    }
}
