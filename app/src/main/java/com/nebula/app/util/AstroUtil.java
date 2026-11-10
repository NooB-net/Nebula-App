package com.nebula.app.util;

/** Low-precision astronomy helpers (pure Java, no Android deps). */
public final class AstroUtil {
    private AstroUtil() {}

    public static double norm360(double d) { d %= 360.0; return d < 0 ? d + 360.0 : d; }
    public static double julianDay(long ms) { return ms / 86400000.0 + 2440587.5; }
    public static double gmstDeg(double jd) { return norm360(280.46061837 + 360.98564736629 * (jd - 2451545.0)); }
    public static double lstDeg(double jd, double lonDeg) { return norm360(gmstDeg(jd) + lonDeg); }

    /** Returns {altitudeDeg, azimuthDeg (0=N, 90=E)}. */
    public static double[] raDecToAltAz(double raDeg, double decDeg, double latDeg, double lstDeg) {
        double ha = Math.toRadians(lstDeg - raDeg);
        double dec = Math.toRadians(decDeg), lat = Math.toRadians(latDeg);
        double sinAlt = Math.sin(dec) * Math.sin(lat) + Math.cos(dec) * Math.cos(lat) * Math.cos(ha);
        double alt = Math.asin(Math.max(-1, Math.min(1, sinAlt)));
        double y = -Math.sin(ha) * Math.cos(dec);
        double x = Math.sin(dec) * Math.cos(lat) - Math.cos(dec) * Math.sin(lat) * Math.cos(ha);
        return new double[]{Math.toDegrees(alt), norm360(Math.toDegrees(Math.atan2(y, x)))};
    }

    /** Unit vector in local East-North-Up from alt/az degrees. */
    public static double[] enu(double altDeg, double azDeg) {
        double a = Math.toRadians(altDeg), z = Math.toRadians(azDeg);
        return new double[]{Math.sin(z) * Math.cos(a), Math.cos(z) * Math.cos(a), Math.sin(a)};
    }

    private static double[] eclToEq(double lonDeg, double latDeg, double n) {
        double eps = Math.toRadians(23.439 - 0.0000004 * n);
        double l = Math.toRadians(lonDeg), b = Math.toRadians(latDeg);
        double x = Math.cos(b) * Math.cos(l);
        double y = Math.cos(b) * Math.sin(l) * Math.cos(eps) - Math.sin(b) * Math.sin(eps);
        double z = Math.cos(b) * Math.sin(l) * Math.sin(eps) + Math.sin(b) * Math.cos(eps);
        return new double[]{norm360(Math.toDegrees(Math.atan2(y, x))), Math.toDegrees(Math.asin(z))};
    }

    public static double sunEclLon(double jd) {
        double n = jd - 2451545.0;
        double L = 280.460 + 0.9856474 * n, g = Math.toRadians(357.528 + 0.9856003 * n);
        return norm360(L + 1.915 * Math.sin(g) + 0.020 * Math.sin(2 * g));
    }
    public static double[] sunRaDec(double jd) { return eclToEq(sunEclLon(jd), 0, jd - 2451545.0); }

    public static double moonEclLon(double jd) {
        double n = jd - 2451545.0;
        return norm360(218.316 + 13.176396 * n + 6.289 * Math.sin(Math.toRadians(134.963 + 13.064993 * n)));
    }
    public static double[] moonRaDec(double jd) {
        double n = jd - 2451545.0;
        double lon = moonEclLon(jd);
        double lat = 5.128 * Math.sin(Math.toRadians(93.272 + 13.229350 * n));
        return eclToEq(lon, lat, n);
    }

    public static double[] sunAltAz(long ms, double lat, double lon) {
        double jd = julianDay(ms); double[] eq = sunRaDec(jd);
        return raDecToAltAz(eq[0], eq[1], lat, lstDeg(jd, lon));
    }
    public static double[] moonAltAz(long ms, double lat, double lon) {
        double jd = julianDay(ms); double[] eq = moonRaDec(jd);
        return raDecToAltAz(eq[0], eq[1], lat, lstDeg(jd, lon));
    }
    /** Sub-solar point {lat, lon}. */
    public static double[] sunSubpoint(long ms) {
        double jd = julianDay(ms); double[] eq = sunRaDec(jd);
        double lon = eq[0] - gmstDeg(jd); lon = norm360(lon + 180) - 180;
        return new double[]{eq[1], lon};
    }

    /** Moon elongation 0..360 (0 = new, 180 = full). */
    public static double moonElongation(long ms) {
        double jd = julianDay(ms);
        return norm360(moonEclLon(jd) - sunEclLon(jd));
    }
    public static double moonIllumination(long ms) { return (1 - Math.cos(Math.toRadians(moonElongation(ms)))) / 2.0; }
    public static String moonPhaseName(long ms) {
        double e = moonElongation(ms);
        if (e < 11.25 || e >= 348.75) return "New Moon";
        if (e < 78.75) return "Waxing Crescent";
        if (e < 101.25) return "First Quarter";
        if (e < 168.75) return "Waxing Gibbous";
        if (e < 191.25) return "Full Moon";
        if (e < 258.75) return "Waning Gibbous";
        if (e < 281.25) return "Last Quarter";
        return "Waning Crescent";
    }

    /** Great-circle central angle in radians. */
    public static double centralAngle(double lat1, double lon1, double lat2, double lon2) {
        double p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2), dl = Math.toRadians(lon2 - lon1);
        double c = Math.sin(p1) * Math.sin(p2) + Math.cos(p1) * Math.cos(p2) * Math.cos(dl);
        return Math.acos(Math.max(-1, Math.min(1, c)));
    }
}
