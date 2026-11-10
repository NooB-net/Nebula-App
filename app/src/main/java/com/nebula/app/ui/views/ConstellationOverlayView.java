package com.nebula.app.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.nebula.app.util.AstroUtil;
import com.nebula.app.util.Sounds;
import com.nebula.app.util.StarCatalog;

import java.util.Arrays;

/**
 * Sky overlay for the camera: projects real star positions through the device rotation matrix.
 * Tap a constellation (e.g. Pegasus) and it comes alive: a ripple spreads from your finger, the
 * figure is drawn star-to-star with a glowing tip, its stars pulse, and a card tells its story.
 */
public class ConstellationOverlayView extends View {
    private final Paint star = new Paint(Paint.ANTI_ALIAS_FLAG), line = new Paint(Paint.ANTI_ALIAS_FLAG),
            label = new Paint(Paint.ANTI_ALIAS_FLAG), name = new Paint(Paint.ANTI_ALIAS_FLAG), obj = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double lat = 23.8, lon = 90.4;
    private final float[] rot = new float[9]; private boolean hasRot = false;
    private final double[][] aa = new double[StarCatalog.RA.length][];
    private long lastCompute = 0; private double[] sun, moon;
    private static final double HFOV = Math.toRadians(62);

    // screen position of every star for the current frame
    private final float[] sx = new float[StarCatalog.RA.length], sy = new float[StarCatalog.RA.length];
    private final boolean[] vis = new boolean[StarCatalog.RA.length];
    private final boolean[] mark = new boolean[StarCatalog.RA.length];

    // ---- tap animation state ----
    private static final long TRACE_MS = 1500, HOLD_MS = 5000, FADE_MS = 800, TOTAL_MS = TRACE_MS + HOLD_MS + FADE_MS;
    private final float density;
    private final Paint glowLine = new Paint(Paint.ANTI_ALIAS_FLAG), brightLine = new Paint(Paint.ANTI_ALIAS_FLAG),
            halo = new Paint(Paint.ANTI_ALIAS_FLAG), core = new Paint(Paint.ANTI_ALIAS_FLAG),
            ripple = new Paint(Paint.ANTI_ALIAS_FLAG), card = new Paint(Paint.ANTI_ALIAS_FLAG),
            cardEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG), bodyPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cardRect = new RectF();
    private int selected = -1;
    private float tapX, tapY, downX, downY;
    private ValueAnimator anim;
    private StaticLayout bodyLayout; private int bodyLayoutWidth = -1;

    public ConstellationOverlayView(Context c) { this(c, null); }
    public ConstellationOverlayView(Context c, AttributeSet a) {
        super(c, a);
        density = c.getResources().getDisplayMetrics().density;
        setBackgroundColor(Color.TRANSPARENT);
        star.setColor(Color.WHITE);
        line.setColor(Color.parseColor("#CC8E5CFF")); line.setStrokeWidth(3f); line.setStyle(Paint.Style.STROKE);
        label.setColor(Color.parseColor("#E6C8A0FF")); label.setTextSize(36f); label.setFakeBoldText(true);
        name.setColor(Color.WHITE); name.setTextSize(30f); name.setShadowLayer(5f, 0, 0, Color.BLACK);
        label.setShadowLayer(5f, 0, 0, Color.BLACK);

        glowLine.setColor(Color.parseColor("#FFB547")); glowLine.setStyle(Paint.Style.STROKE);
        glowLine.setStrokeWidth(14f * density); glowLine.setStrokeCap(Paint.Cap.ROUND);
        brightLine.setColor(Color.parseColor("#FFF1B8")); brightLine.setStyle(Paint.Style.STROKE);
        brightLine.setStrokeWidth(3.5f * density); brightLine.setStrokeCap(Paint.Cap.ROUND);
        halo.setColor(Color.parseColor("#FFD36B")); halo.setStyle(Paint.Style.FILL);
        core.setColor(Color.WHITE); core.setStyle(Paint.Style.FILL);
        ripple.setColor(Color.WHITE); ripple.setStyle(Paint.Style.STROKE); ripple.setStrokeWidth(3f * density);
        card.setColor(Color.parseColor("#E60B1026")); card.setStyle(Paint.Style.FILL);
        cardEdge.setColor(Color.parseColor("#A855F7")); cardEdge.setStyle(Paint.Style.STROKE); cardEdge.setStrokeWidth(1.5f * density);
        titlePaint.setColor(Color.WHITE); titlePaint.setTextSize(20f * density); titlePaint.setFakeBoldText(true);
        bodyPaint.setColor(Color.parseColor("#DDE3FF")); bodyPaint.setTextSize(14f * density);
    }

    public void setLocation(double lat, double lon) { this.lat = lat; this.lon = lon; lastCompute = 0; }

    /** Row-major device->world rotation matrix from SensorManager. */
    public void updateRotation(float[] r) { System.arraycopy(r, 0, rot, 0, 9); hasRot = true; invalidate(); }

    private float[] project(double alt, double az, int w, int h, float f) {
        double[] v = AstroUtil.enu(alt, az);
        double dx = rot[0] * v[0] + rot[3] * v[1] + rot[6] * v[2];
        double dy = rot[1] * v[0] + rot[4] * v[1] + rot[7] * v[2];
        double dz = rot[2] * v[0] + rot[5] * v[1] + rot[8] * v[2];
        double depth = -dz; if (depth <= 0.05) return null;
        return new float[]{(float) (w / 2f + f * dx / depth), (float) (h / 2f - f * dy / depth)};
    }

    // ------------------------------------------------------------------ drawing
    @Override protected void onDraw(Canvas c) {
        if (!hasRot) return;
        long now = System.currentTimeMillis();
        if (now - lastCompute > 5000) {
            StarCatalog.project(lat, lon, now, aa); sun = AstroUtil.sunAltAz(now, lat, lon); moon = AstroUtil.moonAltAz(now, lat, lon); lastCompute = now;
        }
        int w = getWidth(), h = getHeight(); float f = (float) (w / 2.0 / Math.tan(HFOV / 2));

        for (int i = 0; i < aa.length; i++) {
            float[] p = project(aa[i][0], aa[i][1], w, h, f);
            vis[i] = p != null;
            if (p != null) { sx[i] = p[0]; sy[i] = p[1]; }
        }

        for (int[] l : StarCatalog.LINE_IDX) {
            if (vis[l[0]] && vis[l[1]]) c.drawLine(sx[l[0]], sy[l[0]], sx[l[1]], sy[l[1]], line);
        }
        for (int i = 0; i < aa.length; i++) {
            if (!vis[i]) continue;
            if (sx[i] < -50 || sx[i] > w + 50 || sy[i] < -50 || sy[i] > h + 50) continue;
            c.drawCircle(sx[i], sy[i], Math.max(2f, 8f - StarCatalog.MAG[i] * 1.6f), star);
            if (StarCatalog.NAME[i] != null && StarCatalog.MAG[i] < 1.7f) c.drawText(StarCatalog.NAME[i], sx[i] + 14, sy[i] - 10, name);
        }
        for (int i = 0; i < StarCatalog.LABEL_IDX.length; i++) {
            int s = StarCatalog.LABEL_IDX[i][0];
            if (vis[s]) c.drawText(StarCatalog.LABEL_TEXT[i], sx[s] + 14, sy[s] + 44, label);
        }
        float[] mp = project(moon[0], moon[1], w, h, f);
        if (mp != null) { obj.setColor(Color.parseColor("#F4F1E6")); c.drawCircle(mp[0], mp[1], 26, obj); c.drawText("Moon", mp[0] + 34, mp[1] + 10, name); }
        float[] sp = project(sun[0], sun[1], w, h, f);
        if (sp != null) { obj.setColor(Color.parseColor("#FFC857")); c.drawCircle(sp[0], sp[1], 30, obj); c.drawText("Sun", sp[0] + 38, sp[1] + 10, name); }

        if (selected >= 0) drawSelected(c, w, h);
    }

    private static int a(int alpha, float scale) { return Math.max(0, Math.min(255, Math.round(alpha * scale))); }

    private void drawSelected(Canvas c, int w, int h) {
        float elapsed = anim != null ? anim.getAnimatedFraction() * TOTAL_MS : TOTAL_MS;
        float fade = elapsed > TOTAL_MS - FADE_MS ? 1f - (elapsed - (TOTAL_MS - FADE_MS)) / FADE_MS : 1f;
        fade = Math.max(0f, Math.min(1f, fade));

        // 1) ripple spreading from the finger
        if (elapsed < 900f) {
            float t = elapsed / 900f;
            ripple.setAlpha(a(210, (1f - t) * fade));
            c.drawCircle(tapX, tapY, 16f * density + t * 110f * density, ripple);
        }

        // 2) the figure is drawn line by line, with a glowing tip
        int nLines = StarCatalog.LINES_IN_CONST[selected];
        float prog = Math.min(1f, elapsed / TRACE_MS) * nLines;
        int k = 0;
        float tipX = -1, tipY = -1;
        Arrays.fill(mark, false);
        for (int li = 0; li < StarCatalog.LINE_IDX.length; li++) {
            if (StarCatalog.CONST_OF_LINE[li] != selected) continue;
            int sa = StarCatalog.LINE_IDX[li][0], sb = StarCatalog.LINE_IDX[li][1];
            float seg = Math.max(0f, Math.min(1f, prog - k));
            k++;
            if (!vis[sa] || !vis[sb] || seg <= 0f) continue;
            mark[sa] = true;
            if (seg >= 1f) mark[sb] = true;
            float ex = sx[sa] + (sx[sb] - sx[sa]) * seg, ey = sy[sa] + (sy[sb] - sy[sa]) * seg;
            glowLine.setAlpha(a(90, fade));
            brightLine.setAlpha(a(255, fade));
            c.drawLine(sx[sa], sy[sa], ex, ey, glowLine);
            c.drawLine(sx[sa], sy[sa], ex, ey, brightLine);
            if (seg < 1f) { tipX = ex; tipY = ey; }
        }

        // 3) stars of the figure pulse once reached
        for (int i = 0; i < mark.length; i++) {
            if (!mark[i]) continue;
            float pulse = 0.5f + 0.5f * (float) Math.sin(elapsed / 230.0 + i);
            float base = Math.max(4f * density, 9f * density - StarCatalog.MAG[i] * 1.6f * density);
            halo.setAlpha(a(120, (0.5f + 0.5f * pulse) * fade));
            c.drawCircle(sx[i], sy[i], base + (5f + 9f * pulse) * density, halo);
            core.setAlpha(a(255, fade));
            c.drawCircle(sx[i], sy[i], base, core);
        }
        if (tipX >= 0) {
            halo.setAlpha(a(200, fade));
            c.drawCircle(tipX, tipY, 11f * density, halo);
            core.setAlpha(a(255, fade));
            c.drawCircle(tipX, tipY, 5f * density, core);
        }

        // 4) story card
        float cardIn = Math.max(0f, Math.min(1f, (elapsed - TRACE_MS * 0.4f) / 450f));
        if (cardIn > 0f) {
            float margin = 16f * density, pad = 14f * density;
            int textW = (int) (w - 2 * margin - 2 * pad);
            if (textW > 0 && (bodyLayout == null || bodyLayoutWidth != textW)) {
                String info = StarCatalog.LABEL_INFO[selected];
                bodyLayout = StaticLayout.Builder.obtain(info, 0, info.length(), bodyPaint, textW)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.1f).build();
                bodyLayoutWidth = textW;
            }
            if (bodyLayout == null) return;
            float titleH = titlePaint.getFontMetrics().descent - titlePaint.getFontMetrics().ascent;
            float cardH = pad + titleH + 6f * density + bodyLayout.getHeight() + pad;
            float bottom = h - 92f * density + (1f - cardIn) * 24f * density;
            cardRect.set(margin, bottom - cardH, w - margin, bottom);
            float al = cardIn * fade;
            card.setAlpha(a(230, al)); cardEdge.setAlpha(a(255, al));
            c.drawRoundRect(cardRect, 16f * density, 16f * density, card);
            c.drawRoundRect(cardRect, 16f * density, 16f * density, cardEdge);
            titlePaint.setAlpha(a(255, al)); bodyPaint.setAlpha(a(255, al));
            c.drawText(StarCatalog.LABEL_TEXT[selected], cardRect.left + pad, cardRect.top + pad - titlePaint.getFontMetrics().ascent, titlePaint);
            c.save();
            c.translate(cardRect.left + pad, cardRect.top + pad + titleH + 6f * density);
            bodyLayout.draw(c);
            c.restore();
        }
    }

    // ------------------------------------------------------------------ touch
    @Override public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = e.getX(); downY = e.getY();
                return true;
            case MotionEvent.ACTION_UP:
                if (Math.hypot(e.getX() - downX, e.getY() - downY) < 24f * density) {
                    performClick();
                    handleTap(e.getX(), e.getY());
                }
                return true;
            default:
                return true;
        }
    }

    @Override public boolean performClick() { return super.performClick(); }

    private void handleTap(float x, float y) {
        if (!hasRot) return;
        int best = -1;
        float bestDist = 56f * density;
        for (int li = 0; li < StarCatalog.LINE_IDX.length; li++) {
            int sa = StarCatalog.LINE_IDX[li][0], sb = StarCatalog.LINE_IDX[li][1];
            if (!vis[sa] || !vis[sb]) continue;
            float d = distToSegment(x, y, sx[sa], sy[sa], sx[sb], sy[sb]);
            if (d < bestDist) { bestDist = d; best = StarCatalog.CONST_OF_LINE[li]; }
        }
        if (best >= 0) select(best, x, y);
    }

    private static float distToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float len2 = dx * dx + dy * dy;
        float t = len2 == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / len2;
        t = Math.max(0f, Math.min(1f, t));
        float cx = ax + t * dx, cy = ay + t * dy;
        return (float) Math.hypot(px - cx, py - cy);
    }

    private void select(int constellation, float x, float y) {
        if (anim != null) { anim.removeAllListeners(); anim.cancel(); }
        Sounds.stopConstellation();
        Sounds.play(getContext(), Sounds.CONSTELLATION);
        selected = constellation;
        tapX = x; tapY = y;
        bodyLayout = null;
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

        final ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(TOTAL_MS);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(an -> invalidate());
        va.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (anim == va) { selected = -1; Sounds.stopConstellation(); invalidate(); }
            }
        });
        anim = va;
        va.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (anim != null) { anim.removeAllListeners(); anim.cancel(); anim = null; }
        Sounds.stopConstellation();
        super.onDetachedFromWindow();
    }
}
