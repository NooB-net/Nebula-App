package com.nebula.app.ui.social;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nebula.app.R;
import com.nebula.app.data.model.SocialPost;
import com.nebula.app.network.LikesRepository;
import com.nebula.app.network.SocialRepository;
import com.nebula.app.util.Cloud;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

public class SocialPostAdapter extends RecyclerView.Adapter<SocialPostAdapter.ViewHolder> {
    private final List<SocialPost> posts;
    private final Context context;
    private Map<String, Integer> likeCounts = new HashMap<>();
    private Set<String> likedByMe = new HashSet<>();

    /** Called with live data from the Realtime Database. */
    public void setLikes(Map<String, Integer> counts, Set<String> mine) {
        likeCounts = counts;
        likedByMe = mine;
        notifyDataSetChanged();
    }

    public SocialPostAdapter(List<SocialPost> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_social_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final SocialPost post = posts.get(position);

        String notes = post.notes == null ? "" : post.notes.trim();
        h.tvNotes.setText(notes);
        h.tvNotes.setVisibility(notes.isEmpty() ? View.GONE : View.VISIBLE);
        h.tvTimestamp.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date(post.timestamp)));

        String user = post.username != null && !post.username.isEmpty() ? post.username : "Explorer";
        h.tvUsername.setText(user);
        h.tvAvatar.setText(user.substring(0, 1).toUpperCase(Locale.ROOT));

        byte[] img = post.imageBytes();
        if (img != null) Glide.with(context).load(img).placeholder(R.drawable.bg_gradient).into(h.ivPhoto);
        else h.ivPhoto.setImageResource(R.drawable.bg_gradient);

        final boolean isLiked = likedByMe.contains(post.id);
        Integer n = likeCounts.get(post.id);
        h.tvLike.setText((isLiked ? "♥ " : "♡ ") + (n == null ? 0 : n));
        h.tvLike.setTextColor(ContextCompat.getColor(context, isLiked ? R.color.nebula_like : R.color.nebula_text_secondary));
        h.tvLike.setOnClickListener(v -> {
            String uid = Cloud.currentUid();
            if (uid != null) {
                com.nebula.app.util.Sounds.play(context, com.nebula.app.util.Sounds.LIKE, isLiked ? 0.8f : 1f);
                LikesRepository.setLiked(post.id, uid, !isLiked);
            }
        });

        String me = Cloud.currentUid();
        boolean mine = me != null && me.equals(post.uid);
        h.tvDelete.setVisibility(mine ? View.VISIBLE : View.GONE);
        h.tvDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setMessage("Delete this post for everyone?")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> SocialRepository.delete(post.id, (ok, err) -> {
                    if (!ok) Toast.makeText(context, "Couldn't delete: " + err, Toast.LENGTH_SHORT).show();
                }))
                .show());
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvNotes, tvTimestamp, tvUsername, tvAvatar, tvDelete, tvLike;

        ViewHolder(@NonNull View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_post_photo);
            tvNotes = v.findViewById(R.id.tv_post_notes);
            tvTimestamp = v.findViewById(R.id.tv_post_timestamp);
            tvUsername = v.findViewById(R.id.tv_username);
            tvAvatar = v.findViewById(R.id.tv_avatar);
            tvDelete = v.findViewById(R.id.tv_delete);
            tvLike = v.findViewById(R.id.tv_like);
        }
    }
}
