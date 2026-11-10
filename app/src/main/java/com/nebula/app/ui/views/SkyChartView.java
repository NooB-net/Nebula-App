package com.nebula.app.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.nebula.app.R;
import com.nebula.app.util.AstroUtil;
import com.nebula.app.util.StarCatalog;

/** All-sky planisphere (zenith at centre, drag to rotate) for a place and time. */
public class SkyChartView extends View {
    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG), star = new Paint(Paint.ANTI_ALIAS_FLAG),
            line = new Paint(Paint.ANTI_ALIAS_FLAG), txt = new Paint(Paint.ANTI_ALIAS_FLAG),
            ringP = new Paint(Paint.ANTI_ALIAS_FLAG), obj = new Paint(Paint.ANTI_ALIAS_FLAG),
            cardinal = new Paint(Paint.ANTI_ALIAS_FLAG);   // N/E/S/W sit OUTSIDE the dark sky disc, on the themed card
    private double lat = 23.8, lon = 90.4; private long when = System.currentTimeMillis();
    private final double[][] aa = new double[StarCatalog.RA.length][];
    private double[] sun, moon; private float rot = 0f, lastX;
    private double hiAlt = Double.NaN, hiAz; private String hiLabel;

    public SkyChartView(Context c) { this(c, null); }
    public SkyChartView(Context c, AttributeSet a) {
        super(c, a);
        bg.setColor(Color.parseColor("#0B1030"));
        star.setColor(Color.WHITE);
        line.setColor(Color.parseColor("#667B2FBE")); line.setStrokeWidth(2.5f); line.setStyle(Paint.Style.STROKE);
        txt.setColor(Color.parseColor("#B0B8D1")); txt.setTextSize(26f);
        txt.setShadowLayer(4f, 0f, 0f, Color.parseColor("#E00B1030"));   // halo: stays legible if a label spills past the dark disc onto a light card
        cardinal.setColor(ContextCompat.getColor(c, R.color.nebula_text_secondary)); cardinal.setTextSize(26f); cardinal.setFakeBoldText(true);
        ringP.setColor(Color.parseColor("#2A3560")); ringP.setStyle(Paint.Style.STROKE); ringP.setStrokeWidth(2f);
        recompute();
    }

    public void setSky(double lat, double lon, long whenMs) { this.lat = lat; this.lon = lon; this.when = whenMs; recompute(); invalidate(); }
    public void setHighlight(double alt, double az, String label) { hiAlt = alt; hiAz = az; hiLabel = label; invalidate(); }

    private void recompute() {
        StarCatalog.project(lat, lon, when, aa);
        sun = AstroUtil.sunAltAz(when, lat, lon); moon = AstroUtil.moonAltAz(when, lat, lon);
    }

    @Override protected void onMeasure(int w, int h) {
        int s = MeasureSpec.getSize(w);
        setMeasuredDimension(s, MeasureSpec.getMode(h) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(h) : s);
    }

    private float[] pt(double alt, double az, float cx, float cy, float R) {
        double r = (90 - alt) / 90.0 * R, a = Math.toRadians(az) + Math.toRadians(rot);
        return new float[]{(float) (cx - r * Math.sin(a)), (float) (cy - r * Math.cos(a))};
    }

    @Override protected void onDraw(Canvas c) {
        float cx = getWidth() / 2f, cy = getHeight() / 2f, R = Math.min(cx, cy) - 36;
        float sunAlt = (float) sun[0];
        int sky = sunAlt > 0 ? Color.parseColor("#2C4A86") : sunAlt > -12 ? Color.parseColor("#141F4D") : Color.parseColor("#0B1030");
        bg.setColor(sky); c.drawCircle(cx, cy, R, bg);
        c.drawCircle(cx, cy, R, ringP); c.drawCircle(cx, cy, R * 2 / 3, ringP); c.drawCircle(cx, cy, R / 3, ringP);
        String[] card = {"N", "E", "S", "W"};
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(i * 90 + rot);
            c.drawText(card[i], (float) (cx - (R + 20) * Math.sin(a)) - 9, (float) (cy - (R + 20) * Math.cos(a)) + 9, cardinal);
        }
        float dim = sunAlt > 0 ? 0.15f : sunAlt > -12 ? 0.6f : 1f;
        for (int i = 0; i < StarCatalog.LINE_IDX.length; i++) {
            double[] a = aa[StarCatalog.LINE_IDX[i][0]], b = aa[StarCatalog.LINE_IDX[i][1]];
            if (a[0] > 0 && b[0] > 0) { float[] p = pt(a[0], a[1], cx, cy, R), q = pt(b[0], b[1], cx, cy, R); line.setAlpha((int) (110 * dim)); c.drawLine(p[0], p[1], q[0], q[1], line); }
        }
        for (int i = 0; i < aa.length; i++) {
            if (aa[i][0] <= 0) continue;
            float[] p = pt(aa[i][0], aa[i][1], cx, cy, R);
            float r = Math.max(1.4f, 5.2f - StarCatalog.MAG[i] * 1.1f);
            star.setAlpha((int) (255 * dim)); c.drawCircle(p[0], p[1], r, star);
            if (StarCatalog.NAME[i] != null && StarCatalog.MAG[i] < 1.0f && dim > 0.5f) c.drawText(StarCatalog.NAME[i], p[0] + 10, p[1] - 8, txt);
        }
        if (moon[0] > 0) { float[] p = pt(moon[0], moon[1], cx, cy, R); obj.setColor(Color.parseColor("#F4F1E6")); c.drawCircle(p[0], p[1], 16, obj); c.drawText("Moon", p[0] + 20, p[1] + 8, txt); }
        if (sun[0] > 0) { float[] p = pt(sun[0], sun[1], cx, cy, R); obj.setColor(Color.parseColor("#FFC857")); c.drawCircle(p[0], p[1], 18, obj); c.drawText("Sun", p[0] + 22, p[1] + 8, txt); }
        if (!Double.isNaN(hiAlt) && hiAlt > 0) {
            float[] p = pt(hiAlt, hiAz, cx, cy, R); obj.setStyle(Paint.Style.STROKE); obj.setStrokeWidth(3f); obj.setColor(Color.parseColor("#A855F7"));
            c.drawCircle(p[0], p[1], 22, obj); obj.setStyle(Paint.Style.FILL);
            if (hiLabel != null) c.drawText(hiLabel, p[0] + 28, p[1] + 8, txt);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN: lastX = e.getX(); getParent().requestDisallowInterceptTouchEvent(true); return true;
            case MotionEvent.ACTION_MOVE: rot += (e.getX() - lastX) * 0.35f; lastX = e.getX(); invalidate(); return true;
            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL: getParent().requestDisallowInterceptTouchEvent(false); return true;
        }
        return super.onTouchEvent(e);
    }
}
