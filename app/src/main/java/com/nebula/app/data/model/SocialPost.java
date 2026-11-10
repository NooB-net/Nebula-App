package com.nebula.app.data.model;

import android.util.Base64;

/** One post in the shared Socials feed (stored in Cloud Firestore, visible to every account). */
public class SocialPost {
    public String id;
    public String uid;
    public String username;
    public String notes;
    public String imageBase64;   // JPEG, base64
    public long timestamp;

    private byte[] decoded;

    /** Decoded JPEG bytes, memoised so scrolling doesn't re-decode (and Glide can cache by identity). */
    public byte[] imageBytes() {
        if (decoded == null && imageBase64 != null) {
            try { decoded = Base64.decode(imageBase64, Base64.DEFAULT); } catch (IllegalArgumentException ignored) { }
        }
        return decoded;
    }
}
