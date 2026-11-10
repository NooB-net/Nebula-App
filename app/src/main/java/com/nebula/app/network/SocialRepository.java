package com.nebula.app.network;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.nebula.app.data.model.SocialPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads / writes the shared "posts" collection in Cloud Firestore. */
public final class SocialRepository {
    private SocialRepository() {}

    public static final String COLLECTION = "posts";
    private static final int FEED_LIMIT = 40;

    public interface FeedListener {
        void onPosts(List<SocialPost> posts);
        void onError(String message);
    }

    public interface DoneListener {
        void onDone(boolean ok, String error);
    }

    /** Live feed, newest first. Remove the returned registration when the screen stops. */
    public static ListenerRegistration listen(final FeedListener l) {
        return FirebaseFirestore.getInstance().collection(COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(FEED_LIMIT)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { l.onError(e.getMessage()); return; }
                    if (snap == null) return;
                    List<SocialPost> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        SocialPost p = new SocialPost();
                        p.id = d.getId();
                        p.uid = d.getString("uid");
                        p.username = d.getString("username");
                        p.notes = d.getString("notes");
                        p.imageBase64 = d.getString("image");
                        Long ts = d.getLong("timestamp");
                        p.timestamp = ts != null ? ts : 0L;
                        out.add(p);
                    }
                    l.onPosts(out);
                });
    }

    public static void create(String id, String uid, String username, String notes,
                              String imageBase64, final DoneListener done) {
        Map<String, Object> m = new HashMap<>();
        m.put("uid", uid);
        m.put("username", username);
        m.put("notes", notes);
        m.put("image", imageBase64);
        m.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection(COLLECTION).document(id).set(m)
                .addOnSuccessListener(v -> done.onDone(true, null))
                .addOnFailureListener(e -> done.onDone(false, e.getMessage()));
    }

    public static void delete(String id, final DoneListener done) {
        FirebaseFirestore.getInstance().collection(COLLECTION).document(id).delete()
                .addOnSuccessListener(v -> done.onDone(true, null))
                .addOnFailureListener(e -> done.onDone(false, e.getMessage()));
    }
}
