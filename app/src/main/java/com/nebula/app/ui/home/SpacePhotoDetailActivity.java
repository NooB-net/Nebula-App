package com.nebula.app.ui.home;

import com.nebula.app.ui.BaseActivity;
import android.content.Intent;
import android.os.Bundle;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.nebula.app.R;
import com.nebula.app.data.model.SpacePhoto;
import com.nebula.app.databinding.ActivityPhotoDetailBinding;

public class SpacePhotoDetailActivity extends BaseActivity {

    public static final String EXTRA_PHOTO = "extra_photo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPhotoDetailBinding binding = ActivityPhotoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final SpacePhoto photo = (SpacePhoto) getIntent().getSerializableExtra(EXTRA_PHOTO);
        if (photo != null) {
            binding.tvTitle.setText(photo.getTitle());
            String meta = photo.getDate() == null ? "" : photo.getDate();
            if (photo.getCredit() != null && !photo.getCredit().isEmpty())
                meta += (meta.isEmpty() ? "" : "  ·  ") + photo.getCredit();
            binding.tvDate.setText(meta);
            binding.tvExplanation.setText(photo.getDescription());
            loadInto(photo, binding.ivPhoto);
        }

        binding.btnShare.setOnClickListener(v -> share(photo));
        binding.ibBack.setOnClickListener(v -> finish());
    }

    /** Loads the bundled image, or the NASA image (falling back to the small thumbnail). */
    public static void loadInto(SpacePhoto photo, android.widget.ImageView target) {
        if (photo.getLocalRes() != 0) {
            target.setImageResource(photo.getLocalRes());
            return;
        }
        RequestBuilder<android.graphics.drawable.Drawable> thumb = Glide.with(target).load(photo.getThumbUrl());
        Glide.with(target).load(photo.getImageUrl())
                .placeholder(R.drawable.bg_gradient)
                .error(thumb)
                .into(target);
    }

    private void share(SpacePhoto photo) {
        if (photo == null) return;
        String link = photo.getNasaId() != null ? "https://images.nasa.gov/details/" + photo.getNasaId() : "";
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, photo.getTitle() + (link.isEmpty() ? "" : "\n" + link));
        startActivity(Intent.createChooser(i, getString(R.string.share)));
    }
}
