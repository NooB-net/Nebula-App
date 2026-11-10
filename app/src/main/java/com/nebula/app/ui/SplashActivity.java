package com.nebula.app.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.nebula.app.R;
import com.nebula.app.ui.auth.LoginActivity;
import com.nebula.app.ui.views.SplashStarsView;
import com.nebula.app.ui.views.SplashTitleView;
import com.nebula.app.util.LocalStore;
import com.nebula.app.util.NotifPrefs;
import com.nebula.app.util.Sounds;

/**
 * Opening screen. One timeline (in seconds) drives everything so picture, title and the cinematic
 * opening sound line up: dust falls into a core, the core ignites, orbital rings circle it, NEBULA
 * reveals letter by letter, the tagline draws in between two thin rules, a shooting star crosses.
 * The same timings are baked into res/raw/startup.ogg.
 */
public class SplashActivity extends BaseActivity {
    private static final long SPLASH_MS = (long) (SplashStarsView.END * 1000);

    // title reveal: letter i starts at TITLE_START + i * TITLE_STAGGER and takes TITLE_FADE seconds
    private static final float TITLE_START = 2.75f, TITLE_STAGGER = 0.15f, TITLE_FADE = 0.85f;
    private static final float TAG_START = 3.95f;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private ValueAnimator timeline;
    private final Runnable go = () -> {
        Class<?> target = LocalStore.isLoggedIn(this) ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(this, target));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (savedInstanceState == null) prepareStartupSound();   // don't replay it on rotation
        startTimeline();
        startStartupSound();                                     // same moment as the animation
        handler.postDelayed(go, SPLASH_MS);
    }

    private static float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }
    private static float easeOut(float p) { float q = 1f - p; return 1f - q * q * q; }

    private void startTimeline() {
        final SplashStarsView stars = findViewById(R.id.splash_stars);
        final SplashTitleView title = findViewById(R.id.splash_title);
        final View tagline = findViewById(R.id.tv_splash_tagline);
        final View lineL = findViewById(R.id.splash_line_left);
        final View lineR = findViewById(R.id.splash_line_right);
        final float d = getResources().getDisplayMetrics().density;

        // start hidden; the timeline reveals them
        tagline.setAlpha(0f);
        lineL.setAlpha(0f);
        lineR.setAlpha(0f);
        lineL.setPivotX(lineL.getLayoutParams().width);   // grow outward from the tagline
        lineR.setPivotX(0f);

        timeline = ValueAnimator.ofFloat(0f, SplashStarsView.END);
        timeline.setDuration(SPLASH_MS);
        timeline.setInterpolator(new LinearInterpolator());
        timeline.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            stars.setTime(t);

            // title: drawn per letter on its own canvas (no relayout, no wrapping)
            title.setTime(t);
            float tp = easeOut(clamp01((t - TITLE_START) / 2.4f));
            float s = 0.96f + 0.04f * tp;
            title.setScaleX(s);
            title.setScaleY(s);

            // tagline + rules
            float gp = easeOut(clamp01((t - TAG_START) / 0.9f));
            tagline.setAlpha(gp);
            tagline.setTranslationY((1f - gp) * 10f * d);
            float lp = easeOut(clamp01((t - TAG_START - 0.15f) / 1.0f));
            lineL.setAlpha(lp * 0.7f);
            lineR.setAlpha(lp * 0.7f);
            lineL.setScaleX(lp);
            lineR.setScaleX(lp);
        });
        timeline.start();
    }

    /** Loads the sound first (it blocks briefly) so starting it later is instant and in sync. */
    private void prepareStartupSound() {
        if (!NotifPrefs.soundEnabled(this)) return;
        try {
            player = MediaPlayer.create(this, R.raw.startup, Sounds.attributes(), AudioManager.AUDIO_SESSION_ID_GENERATE);
            if (player != null) player.setOnCompletionListener(mp -> releasePlayer());
        } catch (RuntimeException e) {
            releasePlayer();
        }
    }

    private void startStartupSound() {
        try {
            if (player != null) player.start();
        } catch (RuntimeException e) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            try { player.release(); } catch (RuntimeException ignored) { }
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(go);
        if (timeline != null) { timeline.removeAllUpdateListeners(); timeline.cancel(); }
        releasePlayer();
        super.onDestroy();
    }
}
