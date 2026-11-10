package com.nebula.app.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

/** Last known location with a saved/default fallback. Returns {lat, lon}. */
public final class LocationHelper {
    private LocationHelper() {}
    private static final double DEF_LAT = 23.8103, DEF_LON = 90.4125;

    public static double[] get(Context ctx) {
        Context c = ctx.getApplicationContext();
        boolean ok = ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (ok) {
            try {
                LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
                Location best = null;
                if (lm != null) {
                    for (String provider : lm.getProviders(true)) {
                        Location l = lm.getLastKnownLocation(provider);
                        if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
                    }
                }
                if (best != null) {
                    c.getSharedPreferences("nebula_loc", Context.MODE_PRIVATE).edit()
                            .putString("lat", Double.toString(best.getLatitude()))
                            .putString("lon", Double.toString(best.getLongitude())).apply();
                    return new double[]{best.getLatitude(), best.getLongitude()};
                }
            } catch (SecurityException ignored) { }
        }
        try {
            android.content.SharedPreferences p = c.getSharedPreferences("nebula_loc", Context.MODE_PRIVATE);
            String la = p.getString("lat", null), lo = p.getString("lon", null);
            if (la != null && lo != null) return new double[]{Double.parseDouble(la), Double.parseDouble(lo)};
        } catch (NumberFormatException ignored) { }
        return new double[]{DEF_LAT, DEF_LON};
    }
}
