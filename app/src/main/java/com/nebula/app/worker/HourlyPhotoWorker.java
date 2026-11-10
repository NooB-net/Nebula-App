package com.nebula.app.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.nebula.app.data.model.SpacePhoto;
import com.nebula.app.network.SpacePhotoRepository;
import com.nebula.app.util.NotifPrefs;
import com.nebula.app.util.Notifier;

import java.util.concurrent.TimeUnit;

/** Every hour: pick a space photo and post it like a news headline (title + small picture). */
public class HourlyPhotoWorker extends Worker {
    private static final int THUMB_PX = 256;

    public HourlyPhotoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        if (!NotifPrefs.hourlyEnabled(ctx)) return Result.success();

        SpacePhoto photo = SpacePhotoRepository.forHour();
        Bitmap thumb = null;
        try {
            if (photo.getLocalRes() != 0) {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = 4;                         // it only needs to be small
                Bitmap full = BitmapFactory.decodeResource(ctx.getResources(), photo.getLocalRes(), o);
                if (full != null) thumb = ThumbnailUtils.extractThumbnail(full, THUMB_PX, THUMB_PX);
            } else {
                thumb = download(ctx, photo.getThumbUrl());
                if (thumb == null) thumb = download(ctx, photo.getImageUrl());
            }
        } catch (Exception ignored) { }

        Notifier.postHourlyHeadline(ctx, photo, thumb);
        return Result.success();
    }

    /** A small centre-cropped square, which is all a notification thumbnail needs. */
    private Bitmap download(Context ctx, String url) {
        if (url == null) return null;
        try {
            return Glide.with(ctx).asBitmap().load(url).centerCrop().submit(THUMB_PX, THUMB_PX).get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}
