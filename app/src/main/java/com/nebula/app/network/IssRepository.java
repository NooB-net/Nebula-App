package com.nebula.app.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nebula.app.data.model.IssPosition;
import com.nebula.app.util.IssOrbit;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Fetches a live ISS sample and anchors the orbit model on it (cached for offline use). */
public final class IssRepository {
    private IssRepository() {}

    public interface Listener {
        /** pos is null when offline; orbit may be null if never anchored. */
        void onResult(@Nullable IssPosition pos, @Nullable IssOrbit orbit, boolean live);
    }

    private static final String PREFS = "nebula_iss";

    public static void refresh(Context context, Listener listener) {
        final Context app = context.getApplicationContext();
        long now = System.currentTimeMillis() / 1000L;
        ApiClient.getIssApi().getPositions((now - 10) + "," + now).enqueue(new Callback<List<IssPosition>>() {
            @Override
            public void onResponse(@NonNull Call<List<IssPosition>> call, @NonNull Response<List<IssPosition>> r) {
                List<IssPosition> list = r.body();
                if (r.isSuccessful() && list != null && list.size() >= 2) {
                    IssPosition a = list.get(0), b = list.get(list.size() - 1);
                    boolean asc = b.getLatitude() >= a.getLatitude();
                    long t = b.getTimestamp() > 0 ? b.getTimestamp() * 1000L : System.currentTimeMillis();
                    save(app, b.getLatitude(), b.getLongitude(), asc, t);
                    listener.onResult(b, new IssOrbit(b.getLatitude(), b.getLongitude(), asc, t), true);
                } else {
                    listener.onResult(null, cached(app), false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<IssPosition>> call, @NonNull Throwable t) {
                listener.onResult(null, cached(app), false);
            }
        });
    }

    private static void save(Context c, double lat, double lon, boolean asc, long t) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("lat", Double.toString(lat)).putString("lon", Double.toString(lon))
                .putBoolean("asc", asc).putLong("t", t).apply();
    }

    @Nullable
    public static IssOrbit cached(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String lat = p.getString("lat", null), lon = p.getString("lon", null);
        if (lat == null || lon == null) return null;
        try {
            return new IssOrbit(Double.parseDouble(lat), Double.parseDouble(lon),
                    p.getBoolean("asc", true), p.getLong("t", System.currentTimeMillis()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
