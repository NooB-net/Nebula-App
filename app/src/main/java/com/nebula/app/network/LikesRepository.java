package com.nebula.app.network;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Post likes, stored in Firebase REALTIME DATABASE (posts themselves live in Firestore).
 * Layout:  /likes/{postId}/{userId} = true
 */
public final class LikesRepository {
    private LikesRepository() {}

    public interface Listener {
        void onLikes(Map<String, Integer> counts, Set<String> likedByMe);
    }

    private static DatabaseReference likes() {
        return FirebaseDatabase.getInstance().getReference("likes");
    }

    /** Starts a live listener. Returns a Runnable that stops it (never null). */
    public static Runnable listen(final String myUid, final Listener l) {
        try {
            final DatabaseReference ref = likes();
            final ValueEventListener vl = new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot snap) {
                    Map<String, Integer> counts = new HashMap<>();
                    Set<String> mine = new HashSet<>();
                    for (DataSnapshot post : snap.getChildren()) {
                        counts.put(post.getKey(), (int) post.getChildrenCount());
                        if (myUid != null && post.hasChild(myUid)) mine.add(post.getKey());
                    }
                    l.onLikes(counts, mine);
                }
                @Override public void onCancelled(DatabaseError error) { /* likes just stay empty */ }
            };
            ref.addValueEventListener(vl);
            return () -> ref.removeEventListener(vl);
        } catch (RuntimeException e) {
            // Realtime Database isn't set up in this Firebase project yet
            return () -> { };
        }
    }

    public static void setLiked(String postId, String uid, boolean liked) {
        try {
            DatabaseReference r = likes().child(postId).child(uid);
            if (liked) r.setValue(true); else r.removeValue();
        } catch (RuntimeException ignored) { }
    }
}
