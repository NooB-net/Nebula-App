package com.nebula.app.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.nebula.app.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Short sound effects (bundled in res/raw). They play only on this phone, follow the system volume /
 * silent mode, and can be switched off in Profile.
 */
public final class Sounds {
    private Sounds() {}

    public static final int BELL = 0;           // tapping a reminder bell
    public static final int POSTED = 1;         // your post went up
    public static final int LIKE = 2;           // tapping the heart
    public static final int CONSTELLATION = 3; // ambient shimmer while a constellation animates

    private static SoundPool pool;
    private static final int[] ids = new int[4];
    private static final Set<Integer> loaded = new HashSet<>();
    private static int constellationStream = 0;

    /** Call once at app start; loading happens in the background. */
    public static synchronized void init(Context c) {
        if (pool != null) return;
        Context app = c.getApplicationContext();
        pool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attributes())
                .build();
        pool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) synchronized (Sounds.class) { loaded.add(sampleId); }
        });
        ids[BELL] = pool.load(app, R.raw.bell, 1);
        ids[POSTED] = pool.load(app, R.raw.posted, 1);
        ids[LIKE] = pool.load(app, R.raw.like, 1);
        ids[CONSTELLATION] = pool.load(app, R.raw.constellation, 1);
    }

    public static AudioAttributes attributes() {
        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
    }

    public static void play(Context c, int sound) { play(c, sound, 1f); }

    /** @param rate playback speed, 0.5 to 2.0 (1 = normal) */
    public static synchronized void play(Context c, int sound, float rate) {
        if (!NotifPrefs.soundEnabled(c)) return;
        if (pool == null) init(c);
        if (!loaded.contains(ids[sound])) return;      // still loading: skip rather than lag
        int stream = pool.play(ids[sound], 1f, 1f, 1, 0, rate);
        if (sound == CONSTELLATION) constellationStream = stream;
    }

    public static synchronized void stopConstellation() {
        if (pool != null && constellationStream != 0) pool.stop(constellationStream);
        constellationStream = 0;
    }
}
