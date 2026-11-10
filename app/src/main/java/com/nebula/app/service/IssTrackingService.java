package com.nebula.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.nebula.app.NebulaApplication;
import com.nebula.app.R;
import com.nebula.app.data.model.IssPosition;
import com.nebula.app.network.ApiClient;
import com.nebula.app.ui.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssTrackingService extends Service {

    private static final int NOTIF_ID = 1001;
    private static final int INTERVAL_MS = 10000;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable trackRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification("Tracking ISS..."));
        trackRunnable = new Runnable() {
            @Override
            public void run() {
                fetchAndUpdateIss();
                handler.postDelayed(this, INTERVAL_MS);
            }
        };
        handler.post(trackRunnable);
        return START_STICKY;
    }

    private void fetchAndUpdateIss() {
        ApiClient.getIssApi().getIssPosition().enqueue(new Callback<IssPosition>() {
            @Override
            public void onResponse(Call<IssPosition> call, Response<IssPosition> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IssPosition pos = response.body();
                    String msg = String.format("ISS: %.2f°, %.2f°",
                            pos.getLatitude(), pos.getLongitude());
                    updateNotification(msg);
                }
            }
            @Override
            public void onFailure(Call<IssPosition> call, Throwable t) {}
        });
    }

    private Notification buildNotification(String content) {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, notifIntent,
                PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, NebulaApplication.CHANNEL_ID_ISS)
                .setContentTitle("NEBULA — ISS Tracker")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_track)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String content) {
        Notification n = buildNotification(content);
        android.app.NotificationManager nm =
                (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, n);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(trackRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
