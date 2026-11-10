package com.nebula.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

/**
 * On-device accounts and the current session.
 *
 * - Several accounts can exist on one phone, each kept separate (own name, own password hash).
 * - A "session" says who is using the app right now: an account, or a brand-new guest.
 * - Every guest visit is a NEW identity: it never inherits a previous account or a previous guest.
 * - {@link #ownerId} is the key used to keep per-person data (e.g. reminders) apart.
 */
public final class LocalStore {
    private LocalStore() {}

    private static SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences("nebula_user", Context.MODE_PRIVATE);
    }

    private static String norm(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
    private static String key(String email, String field) { return "acct." + norm(email) + "." + field; }

    private static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return s; }
    }

    /** Older versions kept a single account in flat keys; fold it into the multi-account layout once. */
    private static SharedPreferences store(Context c) {
        SharedPreferences s = p(c);
        if (s.contains("email") && !s.getBoolean("legacy_done", false)) {
            String e = norm(s.getString("email", ""));
            String n = s.getString("name", "Explorer");
            SharedPreferences.Editor ed = s.edit();
            if (!e.isEmpty()) {
                ed.putString(key(e, "name"), n);
                if (s.getString("pw", null) != null) ed.putString(key(e, "pw"), s.getString("pw", null));
                if (s.getBoolean("in", false)) {
                    ed.putString("session_type", "account").putString("session_email", e).putString("session_name", n);
                }
            }
            ed.remove("email").remove("name").remove("pw").remove("in").putBoolean("legacy_done", true).apply();
        }
        return s;
    }

    private static void startAccountSession(Context c, String email, String name) {
        store(c).edit().putString("session_type", "account").putString("session_email", norm(email))
                .putString("session_name", name).remove("guest_id").apply();
    }

    // ---- accounts ----
    public static boolean accountExists(Context c, String email) {
        return store(c).contains(key(email, "name"));
    }

    public static boolean hasAccount(Context c) {
        for (String k : store(c).getAll().keySet()) if (k.startsWith("acct.")) return true;
        return false;
    }

    public static void register(Context c, String name, String email, String pw) {
        store(c).edit().putString(key(email, "name"), name).putString(key(email, "pw"), hash(pw)).apply();
        startAccountSession(c, email, name);
    }

    /** Remember who's signed in on this phone (cloud accounts: no password stored). */
    public static void saveSession(Context c, String name, String email) {
        store(c).edit().putString(key(email, "name"), name).apply();
        startAccountSession(c, email, name);
    }

    public static boolean login(Context c, String email, String pw) {
        SharedPreferences s = store(c);
        String saved = s.getString(key(email, "pw"), null);
        if (saved != null && saved.equals(hash(pw))) {
            startAccountSession(c, email, s.getString(key(email, "name"), "Explorer"));
            return true;
        }
        return false;
    }

    // ---- session ----
    public static boolean isLoggedIn(Context c) { return store(c).getString("session_type", null) != null; }
    public static boolean isGuest(Context c) { return "guest".equals(store(c).getString("session_type", null)); }

    /** Starts a fresh, empty guest identity. Nothing from any earlier account or guest carries over. */
    public static void guest(Context c) {
        store(c).edit().putString("session_type", "guest").putString("guest_id", UUID.randomUUID().toString())
                .putString("session_name", "Guest").remove("session_email").apply();
    }

    public static void logout(Context c) {
        store(c).edit().remove("session_type").remove("session_email").remove("session_name").remove("guest_id").apply();
    }

    public static String name(Context c) { return store(c).getString("session_name", "Explorer"); }
    public static String email(Context c) { return store(c).getString("session_email", null); }

    /** Stable key for "who is this": used to keep each account's (or guest's) personal data separate. */
    public static String ownerId(Context c) {
        SharedPreferences s = store(c);
        String type = s.getString("session_type", null);
        if ("guest".equals(type)) return "guest:" + s.getString("guest_id", "");
        if ("account".equals(type)) return "acct:" + s.getString("session_email", "");
        return "none";
    }
}
