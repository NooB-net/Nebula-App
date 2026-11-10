package com.nebula.app.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.nebula.app.R;

/**
 * The NEBULA title, drawn on a canvas so it can never wrap or break apart. The text is always
 * fitted to one line. Each letter rises, de-blurs (glow shrinks) and scales into place, then a
 * soft highlight sweeps across the finished word. Driven by SplashActivity's timeline.
 */
public class SplashTitleView extends View {
    private static final String WORD = "NEBULA";
    private static final float START = 2.75f, STAGGER = 0.15f, FADE = 0.85f;
    private static final float SWEEP_START = 4.35f, SWEEP_DUR = 1.1f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final float[] widths = new float[WORD.length()];
    private final float[] xs = new float[WORD.length()];
    private final float density;
    private final int textColor, glowColor;
    private float textSize, totalWidth, baseline;
    private LinearGradient sweepShader;
    private float time = 0f;

    public SplashTitleView(Context c) { this(c, null); }

    public SplashTitleView(Context c, AttributeSet a) {
        super(c, a);
        density = c.getResources().getDisplayMetrics().density;
        textColor = ContextCompat.getColor(c, R.color.nebula_text_primary);
        glowColor = ContextCompat.getColor(c, R.color.nebula_splash_ring);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.LEFT);
        sweepPaint.setTypeface(paint.getTypeface());
    }

    public void setTime(float seconds) {
        time = seconds;
        invalidate();
    }

    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
    private static float easeOutBack(float p) {
        float s = 1.4f, q = p - 1f;
        return 1f + q * q * ((s + 1f) * q + s);
    }
    private static float easeOut(float p) { float q = 1f - p; return 1f - q * q * q; }
    private static float smooth(float p) { return p * p * (3f - 2f * p); }

    @Override protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        int h = (int) (68 * density);
        setMeasuredDimension(w, h);
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        float maxSize = 48f * density;
        float target = w * 0.84f;
        textSize = maxSize;
        layoutLetters();
        if (totalWidth > target) {                 // shrink to fit: never wraps, never clips
            textSize = maxSize * target / totalWidth;
            layoutLetters();
        }
        paint.setTextSize(textSize);
        sweepPaint.setTextSize(textSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        baseline = h / 2f - (fm.ascent + fm.descent) / 2f;
        float startX = (w - totalWidth) / 2f;
        for (int i = 0; i < xs.length; i++) xs[i] += startX;

        float band = textSize * 2.2f;
        sweepShader = new LinearGradient(0, 0, band, 0,
                new int[]{0x00FFFFFF, 0xCCFFFFFF, 0x00FFFFFF},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        sweepPaint.setShader(sweepShader);
    }

    private void layoutLetters() {
        paint.setTextSize(textSize);
        float gap = textSize * 0.26f;
        float x = 0f;
        for (int i = 0; i < WORD.length(); i++) {
            widths[i] = paint.measureText(WORD, i, i + 1);
            xs[i] = x;
            x += widths[i] + (i < WORD.length() - 1 ? gap : 0f);
        }
        totalWidth = x;
    }

    @Override protected void onDraw(Canvas c) {
        if (getWidth() == 0 || time < START) return;
        for (int i = 0; i < WORD.length(); i++) {
            float p = clamp01((time - START - i * STAGGER) / FADE);
            if (p <= 0f) continue;
            float e = easeOut(p);
            float s = 1.5f - 0.5f * easeOutBack(p);     // big and soft -> settles with a tiny overshoot
            float rise = (1f - e) * 22f * density;
            float glow = (1f - e) * 18f * density + 3f * density * (1f - clamp01((time - 4.4f) / 1.2f));

            c.save();
            float cx = xs[i] + widths[i] / 2f;
            c.translate(0f, rise);
            c.scale(s, s, cx, baseline - textSize * 0.35f);
            paint.setColor(ColorUtils.setAlphaComponent(textColor, Math.round(255 * e)));
            if (glow > 0.5f) paint.setShadowLayer(glow, 0f, 0f, ColorUtils.setAlphaComponent(glowColor, Math.round(200 * (1f - e * 0.4f))));
            else paint.clearShadowLayer();
            c.drawText(WORD, i, i + 1, xs[i], baseline, paint);
            c.restore();
        }
        paint.clearShadowLayer();

        // highlight sweep across the finished word
        float sp = clamp01((time - SWEEP_START) / SWEEP_DUR);
        if (sp > 0f && sp < 1f) {
            float band = textSize * 2.2f;
            float x = (xs[0] - band) + (totalWidth + band) * smooth(sp);
            matrix.setTranslate(x, 0f);
            sweepShader.setLocalMatrix(matrix);
            for (int i = 0; i < WORD.length(); i++) {
                c.drawText(WORD, i, i + 1, xs[i], baseline, sweepPaint);
            }
        }
    }
}
