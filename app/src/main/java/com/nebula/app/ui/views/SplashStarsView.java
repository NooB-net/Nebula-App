package com.nebula.app.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.nebula.app.R;

import java.util.Random;

/**
 * Splash background. It does NOT run its own clock: SplashActivity feeds it the time (seconds) so the
 * picture, the NEBULA title and the opening sound stay perfectly in step.
 *
 *   0.0 - 1.45  nebula glow blooms, dust streaks fall toward the centre
 *   1.45        the core ignites (flash + shockwave)
 *   1.5  - 3.9  glowing orbital rings circle the core, then open out and dissolve
 *   1.8  - 3.4  constellations draw themselves in
 *   2.7  - 4.6  (title reveals, handled by the activity)
 *   4.9  - 5.6  a shooting star crosses
 *   after       the glow keeps breathing
 */
public class SplashStarsView extends View {
    public static final float END = 6.4f;

    private static final int STARS = 150;
    private static final int DUST = 64;
    private static final int RINGS = 3;
    private static final int RING_SEGS = 72;

    private final float[] sx = new float[STARS], sy = new float[STARS], sr = new float[STARS],
            sPhase = new float[STARS], sSpeed = new float[STARS], sDepth = new float[STARS];
    private final float[] dAng = new float[DUST], dR = new float[DUST], dDelay = new float[DUST], dLen = new float[DUST];

    // constellations: x,y pairs in 0..1 of the screen, drawn as polylines
    private static final float[][] CONSTELLATIONS = {
            {0.08f, 0.16f, 0.17f, 0.11f, 0.24f, 0.19f, 0.33f, 0.13f, 0.40f, 0.20f},
            {0.62f, 0.12f, 0.70f, 0.18f, 0.78f, 0.10f, 0.86f, 0.17f, 0.93f, 0.11f},
            {0.18f, 0.86f, 0.27f, 0.80f, 0.35f, 0.88f, 0.46f, 0.82f},
            {0.60f, 0.85f, 0.70f, 0.90f, 0.79f, 0.83f, 0.90f, 0.88f}
    };

    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float density;
    private final int starColor, coreColor, ringColor, lineColor, glowCenter, glowMid;

    private float time = 0f;           // seconds, set by the activity
    private float glowRadius, coreRadius;
    private float cx, cy;

    public SplashStarsView(Context c) { this(c, null); }

    public SplashStarsView(Context c, AttributeSet a) {
        super(c, a);
        density = c.getResources().getDisplayMetrics().density;
        starColor = ContextCompat.getColor(c, R.color.nebula_splash_star);
        coreColor = ContextCompat.getColor(c, R.color.nebula_splash_core);
        ringColor = ContextCompat.getColor(c, R.color.nebula_splash_ring);
        lineColor = ContextCompat.getColor(c, R.color.nebula_splash_line);
        glowCenter = ContextCompat.getColor(c, R.color.nebula_splash_glow_center);
        glowMid = ContextCompat.getColor(c, R.color.nebula_splash_glow_mid);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setStyle(Paint.Style.STROKE);
    }

    /** Called every frame by SplashActivity. */
    public void setTime(float seconds) {
        time = seconds;
        invalidate();
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        cx = w / 2f;
        cy = h / 2f;
        Random rnd = new Random(11);
        for (int i = 0; i < STARS; i++) {
            sx[i] = rnd.nextFloat() * w;
            sy[i] = rnd.nextFloat() * h;
            sDepth[i] = 0.3f + rnd.nextFloat() * 0.7f;                 // parallax layer
            sr[i] = (0.4f + rnd.nextFloat() * 1.2f) * density * (0.6f + sDepth[i] * 0.6f);
            sPhase[i] = rnd.nextFloat() * 6.28f;
            sSpeed[i] = 0.8f + rnd.nextFloat() * 1.8f;
        }
        float diag = (float) Math.hypot(w, h);
        for (int i = 0; i < DUST; i++) {
            dAng[i] = rnd.nextFloat() * 6.2832f;
            dR[i] = diag * (0.28f + rnd.nextFloat() * 0.32f);
            dDelay[i] = rnd.nextFloat() * 0.55f;
            dLen[i] = (10f + rnd.nextFloat() * 26f) * density;
        }
        glowRadius = Math.min(w, h) * 0.78f;
        glowPaint.setShader(new RadialGradient(cx, cy, glowRadius,
                new int[]{glowCenter, glowMid, ColorUtils.setAlphaComponent(glowMid, 0)},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        coreRadius = Math.min(w, h) * 0.16f;
        corePaint.setShader(new RadialGradient(cx, cy, coreRadius,
                new int[]{coreColor, ColorUtils.setAlphaComponent(ringColor, 150), ColorUtils.setAlphaComponent(ringColor, 0)},
                new float[]{0f, 0.3f, 1f}, Shader.TileMode.CLAMP));
    }

    // ---- easing / helpers ----------------------------------------------------------------------
    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
    /** 0 before a, 1 after b, smooth in between. */
    private static float range(float t, float a, float b) { return clamp01((t - a) / (b - a)); }
    private static float easeOut(float p) { float q = 1f - p; return 1f - q * q * q; }
    private static float easeIn(float p) { return p * p * p; }
    private static float smooth(float p) { return p * p * (3f - 2f * p); }

    @Override protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (w == 0) return;
        final float t = time;

        // slow cinematic push-in on the whole scene
        float push = 1f + 0.05f * smooth(range(t, 0f, END));
        c.save();
        c.scale(push, push, cx, cy);

        float bloom = easeOut(range(t, 0f, 1.6f));
        float breathe = t < 3f ? 1f : 1f + 0.035f * (float) Math.sin((t - 3f) * 1.7f);

        drawGlow(c, bloom, breathe);
        drawStars(c, bloom);
        drawConstellations(c);
        drawDust(c);
        drawIgnition(c);
        drawRings(c);
        drawShootingStar(c, w, h);

        c.restore();
    }

    private void drawGlow(Canvas c, float bloom, float breathe) {
        c.save();
        float s = (0.3f + 0.7f * bloom) * breathe;
        // the flash makes the whole nebula swell for a heartbeat
        s += 0.10f * flash(time);
        c.scale(s, s, cx, cy);
        glowPaint.setAlpha((int) (255 * Math.min(1f, bloom * (1f + 0.25f * flash(time)))));
        c.drawCircle(cx, cy, glowRadius, glowPaint);
        c.restore();
    }

    private void drawStars(Canvas c, float bloom) {
        for (int i = 0; i < STARS; i++) {
            float tw = 0.5f + 0.5f * (float) Math.sin(time * sSpeed[i] * 1.4f + sPhase[i]);
            float a = bloom * (0.22f + 0.78f * tw) * (0.45f + 0.55f * sDepth[i]);
            // gentle drift upward, nearer stars faster
            float y = sy[i] - time * 2.2f * density * sDepth[i];
            float h = getHeight();
            y = ((y % h) + h) % h;
            starPaint.setColor(starColor);
            starPaint.setAlpha((int) (255 * a));
            c.drawCircle(sx[i], y, sr[i] * (0.85f + 0.4f * tw), starPaint);
        }
    }

    private void drawConstellations(Canvas c) {
        float w = getWidth(), h = getHeight();
        for (int k = 0; k < CONSTELLATIONS.length; k++) {
            float[] pts = CONSTELLATIONS[k];
            int n = pts.length / 2;
            float start = 1.8f + k * 0.28f;
            float draw = easeOut(range(time, start, start + 1.1f));
            float fade = 1f - smooth(range(time, 4.4f, 5.8f));
            if (draw <= 0f || fade <= 0f) continue;

            linePaint.setColor(lineColor);
            linePaint.setStrokeWidth(1f * density);
            float total = n - 1;
            float reach = draw * total;
            for (int i = 0; i < n - 1; i++) {
                float seg = clamp01(reach - i);
                if (seg <= 0f) break;
                float x0 = pts[i * 2] * w, y0 = pts[i * 2 + 1] * h;
                float x1 = x0 + (pts[(i + 1) * 2] * w - x0) * seg;
                float y1 = y0 + (pts[(i + 1) * 2 + 1] * h - y0) * seg;
                linePaint.setAlpha((int) (110 * fade));
                c.drawLine(x0, y0, x1, y1, linePaint);
            }
            starPaint.setColor(starColor);
            for (int i = 0; i < n; i++) {
                if (reach < i) break;
                float pop = easeOut(clamp01(reach - i + 0.3f));
                starPaint.setAlpha((int) (230 * fade * pop));
                c.drawCircle(pts[i * 2] * w, pts[i * 2 + 1] * h, 2.2f * density * pop, starPaint);
            }
        }
    }

    /** Streaks of dust spiral inward and are swallowed by the core at the ignition moment. */
    private void drawDust(Canvas c) {
        if (time > 1.7f) return;
        linePaint.setColor(lineColor);
        for (int i = 0; i < DUST; i++) {
            float p = range(time, 0.1f + dDelay[i], 1.45f);
            if (p <= 0f) continue;
            float e = easeIn(p);                                   // accelerates into the core
            float r = dR[i] * (1f - e) + 6f * density;
            float ang = dAng[i] + 0.9f * e;                        // slight swirl
            float x = cx + (float) Math.cos(ang) * r;
            float y = cy + (float) Math.sin(ang) * r;
            float tail = dLen[i] * (0.4f + p) * (1f - e * 0.6f);
            float tx = x + (float) Math.cos(ang) * tail;
            float ty = y + (float) Math.sin(ang) * tail;
            float a = smooth(range(p, 0f, 0.2f)) * (1f - smooth(range(time, 1.35f, 1.65f)));
            linePaint.setStrokeWidth((0.8f + 1.0f * p) * density);
            linePaint.setAlpha((int) (200 * a));
            c.drawLine(x, y, tx, ty, linePaint);
        }
    }

    /** 0..1..0 pulse around the ignition moment (1.45s). */
    private static float flash(float t) {
        if (t < 1.25f) return 0f;
        if (t < 1.47f) return easeIn(range(t, 1.25f, 1.47f));
        return 1f - easeOut(range(t, 1.47f, 2.4f));
    }

    private void drawIgnition(Canvas c) {
        float t = time;
        float f = flash(t);
        // persistent core: a small star that lingers, then is absorbed by the title
        float linger = t < 1.47f ? 0f : (1f - smooth(range(t, 2.8f, 4.0f)));
        float level = Math.max(f, 0.55f * linger);
        if (level > 0.01f) {
            float s = 0.35f + 0.9f * f + 0.25f * linger;
            c.save();
            c.scale(s, s, cx, cy);
            corePaint.setAlpha((int) (255 * Math.min(1f, level * 1.15f)));
            c.drawCircle(cx, cy, coreRadius, corePaint);
            c.restore();

            // horizontal lens streak at the flash: layered lines so it tapers to nothing at the ends
            if (f > 0.02f) {
                float full = getWidth() * 0.9f * f;
                linePaint.setColor(coreColor);
                linePaint.setStrokeWidth(1.2f * density);
                for (int k = 0; k < 4; k++) {
                    float len = full * (1f - k * 0.25f);
                    linePaint.setAlpha((int) (75 * f));
                    c.drawLine(cx - len / 2f, cy, cx + len / 2f, cy, linePaint);
                }
            }
        }

        // shockwave ring
        float sw = range(t, 1.45f, 2.8f);
        if (sw > 0f && sw < 1f) {
            float r = easeOut(sw) * Math.max(getWidth(), getHeight()) * 0.62f;
            ringPaint.setColor(ringColor);
            ringPaint.setStrokeWidth((2.6f - 2.0f * sw) * density);
            ringPaint.setAlpha((int) (170 * (1f - sw) * (1f - sw)));
            c.drawCircle(cx, cy, r, ringPaint);
        }
    }

    /** Three tilted orbital ellipses with a bright comet-head sweeping each one. */
    private void drawRings(Canvas c) {
        float t = time;
        float appear = easeOut(range(t, 1.5f, 2.3f));
        float dissolve = smooth(range(t, 3.0f, 4.1f));
        float alpha = appear * (1f - dissolve);
        if (alpha <= 0.01f) return;

        float base = Math.min(getWidth(), getHeight()) * 0.30f;
        float scale = (0.25f + 0.75f * appear) * (1f + 0.45f * dissolve);
        // {semi-major, semi-minor, tilt(deg), spin speed, head offset}
        float[][] r = {
                {1.00f, 0.62f, -24f, 1.10f, 0.0f},
                {0.92f, 0.55f, 38f, -0.85f, 2.1f},
                {1.08f, 0.70f, 102f, 0.65f, 4.2f}
        };
        for (int k = 0; k < RINGS; k++) {
            float a = base * r[k][0] * scale, b = base * r[k][1] * scale;
            float tilt = (float) Math.toRadians(r[k][2] + t * 8f * (k % 2 == 0 ? 1 : -1));
            float ct = (float) Math.cos(tilt), st = (float) Math.sin(tilt);
            float head = r[k][4] + t * r[k][3] * 2.2f;

            float prevX = 0, prevY = 0;
            for (int i = 0; i <= RING_SEGS; i++) {
                float th = i * 6.2832f / RING_SEGS;
                float ex = a * (float) Math.cos(th), ey = b * (float) Math.sin(th);
                float x = cx + ex * ct - ey * st;
                float y = cy + ex * st + ey * ct;
                if (i > 0) {
                    // brightness trails behind the head, wrapping around the ellipse
                    float d = ((head - th) % 6.2832f + 6.2832f) % 6.2832f;
                    float glow = (float) Math.exp(-d * 1.5f);
                    ringPaint.setColor(ringColor);
                    ringPaint.setStrokeWidth((0.9f + 2.2f * glow) * density);
                    ringPaint.setAlpha((int) (255 * alpha * (0.22f + 0.78f * glow)));
                    c.drawLine(prevX, prevY, x, y, ringPaint);
                }
                prevX = x;
                prevY = y;
            }
        }
    }

    private void drawShootingStar(Canvas c, int w, int h) {
        float st = time - 4.9f, dur = 0.75f;
        if (st < 0f || st > dur) return;
        float p = st / dur;
        float x0 = w * 0.78f, y0 = h * 0.14f, x1 = w * 0.18f, y1 = h * 0.40f;
        int segs = 18;
        linePaint.setColor(coreColor);
        for (int i = 0; i < segs; i++) {
            float a = Math.max(0f, p - i * 0.016f), b = Math.max(0f, p - (i + 1) * 0.016f);
            float fade = (1f - i / (float) segs);
            linePaint.setAlpha((int) (235 * fade * fade * (1f - smooth(range(p, 0.7f, 1f)))));
            linePaint.setStrokeWidth((3.2f * fade + 0.4f) * density);
            c.drawLine(x0 + (x1 - x0) * a, y0 + (y1 - y0) * a, x0 + (x1 - x0) * b, y0 + (y1 - y0) * b, linePaint);
        }
    }
}
