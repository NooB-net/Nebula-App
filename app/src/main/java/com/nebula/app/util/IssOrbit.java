package com.nebula.app.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Circular-orbit ISS model anchored on one real position sample.
 * Good to a few degrees over a few hours; re-anchored on every live update.
 */
public class IssOrbit {
    private static final double INC = Math.toRadians(51.64);
    private static final double N = 2 * Math.PI / 5560.0;        // rad/s (~92.7 min)
    private static final double WE = 7.2921159e-5;                // Earth rotation rad/s
    private static final double RE = 6371.0, ALT = 420.0;

    public static class Pass {
        public long riseMs; public long durationSec; public double maxElevDeg; public boolean visible;
    }

    private final double u0, lonNode; private final long t0;

    public IssOrbit(double latDeg, double lonDeg, boolean ascending, long t0ms) {
        double s = Math.sin(Math.toRadians(latDeg)) / Math.sin(INC);
        double u = Math.asin(Math.max(-1, Math.min(1, s)));
        if (!ascending) u = Math.PI - u;
        this.u0 = u;
        this.lonNode = Math.toRadians(lonDeg) - Math.atan2(Math.cos(INC) * Math.sin(u), Math.cos(u));
        this.t0 = t0ms;
    }

    /** {latDeg, lonDeg in -180..180} */
    public double[] position(long ms) {
        double dt = (ms - t0) / 1000.0, u = u0 + N * dt;
        double lat = Math.asin(Math.sin(INC) * Math.sin(u));
        double lon = lonNode - WE * dt + Math.atan2(Math.cos(INC) * Math.sin(u), Math.cos(u));
        double d = Math.toDegrees(lon); d = ((d + 180) % 360 + 360) % 360 - 180;
        return new double[]{Math.toDegrees(lat), d};
    }

    public double elevationDeg(double obsLat, double obsLon, long ms) {
        double[] p = position(ms);
        double g = AstroUtil.centralAngle(obsLat, obsLon, p[0], p[1]);
        double k = RE / (RE + ALT);
        return Math.toDegrees(Math.atan2(Math.cos(g) - k, Math.sin(g)));
    }

    public List<Pass> findPasses(double obsLat, double obsLon, long fromMs, int hours, double minElev) {
        List<Pass> out = new ArrayList<>();
        long end = fromMs + hours * 3600_000L; Pass cur = null;
        for (long t = fromMs; t < end; t += 15_000L) {
            double e = elevationDeg(obsLat, obsLon, t);
            if (e >= minElev) {
                if (cur == null) { cur = new Pass(); cur.riseMs = t; }
                cur.maxElevDeg = Math.max(cur.maxElevDeg, e);
                cur.durationSec = (t - cur.riseMs) / 1000 + 15;
            } else if (cur != null) { finish(cur, obsLat, obsLon); out.add(cur); cur = null; }
        }
        if (cur != null) { finish(cur, obsLat, obsLon); out.add(cur); }
        return out;
    }
    private void finish(Pass p, double lat, double lon) {
        long mid = p.riseMs + p.durationSec * 500L;
        p.visible = AstroUtil.sunAltAz(mid, lat, lon)[0] < -6;
    }
}
