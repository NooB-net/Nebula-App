package com.nebula.app.util;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/** Small helpers around Firebase so the rest of the app doesn't have to care whether it's set up. */
public final class Cloud {
    private Cloud() {}

    public interface Ready { void onReady(boolean ok); }

    /**
     * True only when a real google-services.json is in place. The placeholder file that ships
     * with the project has a dummy API key, so Socials shows a setup hint instead of failing.
     */
    public static boolean isConfigured(Context c) {
        try {
            if (FirebaseApp.getApps(c.getApplicationContext()).isEmpty()) return false;
            String key = FirebaseApp.getInstance().getOptions().getApiKey();
            return key != null && !key.contains("Dummy") && !key.contains("Placeholder");
        } catch (Exception e) {
            return false;
        }
    }

    /** Makes sure there's a signed-in Firebase user (anonymous if nobody is), then calls back on the main thread. */
    public static void ensureSignedIn(Context c, final Ready cb) {
        if (!isConfigured(c)) { cb.onReady(false); return; }
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) { cb.onReady(true); return; }
            auth.signInAnonymously().addOnCompleteListener(t -> cb.onReady(t.isSuccessful()));
        } catch (Exception e) {
            cb.onReady(false);
        }
    }

    public static String currentUid() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void signOut(Context c) {
        if (!isConfigured(c)) return;
        try { FirebaseAuth.getInstance().signOut(); } catch (Exception ignored) { }
    }
}
