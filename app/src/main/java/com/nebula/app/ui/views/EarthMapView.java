package com.nebula.app.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.nebula.app.R;
import com.nebula.app.util.AstroUtil;
import com.nebula.app.util.IssOrbit;

/** Equirectangular Earth with day/night terminator, ISS marker and ground track. */
public class EarthMapView extends View {
    private static final int GW = 144, GH = 72;
    private Bitmap day, lights, mask;
    private final Paint bmp = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF dst = new RectF();
    private final Path path = new Path();
    private final int[] px = new int[GW * GH];

    private IssOrbit orbit;
    private double issLat, issLon; private boolean hasIss;
    private double obsLat, obsLon; private boolean hasObs;
    private long when = System.currentTimeMillis();

    public EarthMapView(Context c) { this(c, null); }
    public EarthMapView(Context c, AttributeSet a) {
        super(c, a);
        BitmapFactory.Options o = new BitmapFactory.Options(); o.inPreferredConfig = Bitmap.Config.RGB_565;
        day = BitmapFactory.decodeResource(getResources(), R.drawable.earth_day, o);
        lights = BitmapFactory.decodeResource(getResources(), R.drawable.earth_lights, o);
        mask = Bitmap.createBitmap(GW, GH, Bitmap.Config.ARGB_8888);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        track.setStyle(Paint.Style.STROKE); track.setStrokeWidth(3f); track.setColor(Color.parseColor("#CC00D4FF"));
        track.setPathEffect(new android.graphics.DashPathEffect(new float[]{14, 10}, 0));
        dot.setColor(Color.parseColor("#00D4FF")); ring.setStyle(Paint.Style.STROKE); ring.setStrokeWidth(3f);
        ring.setColor(Color.WHITE); label.setColor(Color.WHITE); label.setTextSize(30f); label.setFakeBoldText(true);
        label.setShadowLayer(4f, 0, 0, Color.BLACK);
        rebuildMask();
    }

    public void setData(IssOrbit orbit, double issLat, double issLon, boolean hasIss) {
        this.orbit = orbit; this.issLat = issLat; this.issLon = issLon; this.hasIss = hasIss;
        this.when = System.currentTimeMillis(); rebuildMask(); invalidate();
    }
    public void setObserver(double lat, double lon) { obsLat = lat; obsLon = lon; hasObs = true; invalidate(); }

    private void rebuildMask() {
        double[] sp = AstroUtil.sunSubpoint(when);
        double sLat = Math.toRadians(sp[0]), sLon = Math.toRadians(sp[1]);
        for (int y = 0; y < GH; y++) {
            double lat = Math.toRadians(90 - (y + .5) * 180.0 / GH);
            for (int x = 0; x < GW; x++) {
                double lon = Math.toRadians((x + .5) * 360.0 / GW - 180);
                double sinAlt = Math.sin(lat) * Math.sin(sLat) + Math.cos(lat) * Math.cos(sLat) * Math.cos(lon - sLon);
                double night = Math.max(0, Math.min(1, (0.08 - sinAlt) / 0.18));     // soft terminator
                px[y * GW + x] = ((int) (night * 215) << 24) | 0x00FFFFFF;
            }
        }
        mask.setPixels(px, 0, GW, 0, 0, GW, GH);
    }

    @Override protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec); setMeasuredDimension(w, w / 2);
    }

    private float X(double lon) { return (float) ((lon + 180) / 360.0 * getWidth()); }
    private float Y(double lat) { return (float) ((90 - lat) / 180.0 * getHeight()); }

    @Override protected void onDraw(Canvas canvas) {
        dst.set(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(day, null, dst, bmp);
        int sc = canvas.saveLayer(dst, null);
        canvas.drawBitmap(lights, null, dst, bmp);
        canvas.drawBitmap(mask, null, dst, maskPaint);
        canvas.restoreToCount(sc);
        // dim the night side of the day map a little
        if (orbit != null) {
            path.reset(); double prevLon = 0; boolean first = true;
            for (int m = -45; m <= 95; m += 1) {
                double[] p = orbit.position(when + m * 60_000L);
                float x = X(p[1]), y = Y(p[0]);
                if (first || Math.abs(p[1] - prevLon) > 180) path.moveTo(x, y); else path.lineTo(x, y);
                prevLon = p[1]; first = false;
            }
            canvas.drawPath(path, track);
        }
        if (hasObs) {
            dot.setColor(Color.parseColor("#FFD166")); canvas.drawCircle(X(obsLon), Y(obsLat), 7f, dot);
            canvas.drawText("You", X(obsLon) + 12, Y(obsLat) - 8, label);
        }
        if (hasIss) {
            float x = X(issLon), y = Y(issLat);
            dot.setColor(Color.parseColor("#6600D4FF")); canvas.drawCircle(x, y, 26f, dot);
            dot.setColor(Color.parseColor("#00D4FF")); canvas.drawCircle(x, y, 11f, dot);
            canvas.drawCircle(x, y, 17f, ring);
            canvas.drawText("ISS", x + 24, y - 12, label);
        }
    }
}
