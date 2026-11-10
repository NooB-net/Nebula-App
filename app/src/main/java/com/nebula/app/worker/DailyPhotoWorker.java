package com.nebula.app.worker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.nebula.app.data.model.SpacePhoto;
import com.nebula.app.network.SpacePhotoRepository;
import com.nebula.app.util.NotifPrefs;
import com.nebula.app.util.Notifier;

import java.util.concurrent.TimeUnit;

/** Once a day: fetch today's space photo and show it as a notification. */
public class DailyPhotoWorker extends Worker {

    public DailyPhotoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        if (!NotifPrefs.dailyPhotoEnabled(ctx)) return Result.success();

        SpacePhoto photo = SpacePhotoRepository.today(ctx);
        Bitmap bmp = null;
        try {
            if (photo.getLocalRes() != 0) {
                bmp = BitmapFactory.decodeResource(ctx.getResources(), photo.getLocalRes());
            } else {
                bmp = download(ctx, photo.getImageUrl());
                if (bmp == null) bmp = download(ctx, photo.getThumbUrl());
            }
        } catch (Exception ignored) { }

        Notifier.postDailyPhoto(ctx, photo, bmp);
        return Result.success();
    }

    private Bitmap download(Context ctx, String url) {
        if (url == null) return null;
        try {
            return Glide.with(ctx).asBitmap().load(url).submit(1024, 512).get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}
