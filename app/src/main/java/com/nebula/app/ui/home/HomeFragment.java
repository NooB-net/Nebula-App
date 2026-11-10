package com.nebula.app.ui.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.nebula.app.R;
import com.nebula.app.data.model.SpacePhoto;
import com.nebula.app.data.model.IssPosition;
import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.databinding.FragmentHomeBinding;
import com.nebula.app.network.SpacePhotoRepository;
import com.nebula.app.network.IssRepository;
import com.nebula.app.ui.MainActivity;
import com.nebula.app.ui.events.SkyEventAdapter;
import com.nebula.app.ui.profile.ProfileActivity;
import com.nebula.app.util.AstroUtil;
import com.nebula.app.util.IssOrbit;
import com.nebula.app.util.LocalStore;
import com.nebula.app.util.LocationHelper;
import com.nebula.app.util.SampleDataUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private SpacePhoto currentPhoto;
    private final Handler main = new Handler(Looper.getMainLooper());

    // ---- auto-rotation: the photo changes every 5 s for as long as Home is on screen, never paused by touches ----
    private static final long AUTO_MS = 5_000L;
    private static final long FADE_MS = 900L;
    private final List<SpacePhoto> rotation = new ArrayList<>();
    private int rotIndex = 0;
    private boolean ticking;
    // fixed-rate ticker: it always re-arms itself, so nothing the user touches can stall it
    private final Runnable autoNext = () -> { ticking = false; advance(); scheduleAuto(); };
    // a cross-fade in flight (null when idle)
    private SpacePhoto pendingPhoto;
    private String pendingLabel, pendingUrl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setGreeting();
        showMoon();
        showUpcoming();
        loadTodaysPhoto();
        loadIssPass();

        binding.cardPhoto.setOnClickListener(v -> {
            if (currentPhoto != null) {
                Intent i = new Intent(getActivity(), SpacePhotoDetailActivity.class);
                i.putExtra(SpacePhotoDetailActivity.EXTRA_PHOTO, currentPhoto);
                startActivity(i);
            }
        });
        binding.btnShufflePhoto.setOnClickListener(v -> shufflePhoto());
        binding.btnTrackIss.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).selectTab(R.id.nav_track);
        });
        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        scheduleAuto();
    }

    @Override
    public void onPause() {
        super.onPause();
        main.removeCallbacks(autoNext);
        ticking = false;
        finishFadeNow();
    }

    private void setGreeting() {
        binding.tvGreeting.setText("Welcome, " + LocalStore.name(requireContext()));
        binding.tvSubtitle.setText(DateFormat.getDateInstance(DateFormat.FULL).format(new Date()));
    }

    private void showMoon() {
        long now = System.currentTimeMillis();
        binding.tvMoonPhase.setText(AstroUtil.moonPhaseName(now));
        binding.tvMoonSub.setText(Math.round(AstroUtil.moonIllumination(now) * 100) + "% lit");
    }

    private void showUpcoming() {
        List<SkyEvent> events = SampleDataUtil.getSampleEvents();
        binding.llUpcoming.removeAllViews();
        for (int i = 0; i < Math.min(3, events.size()); i++) {
            View item = LayoutInflater.from(requireContext()).inflate(R.layout.item_sky_event, binding.llUpcoming, false);
            SkyEventAdapter.ViewHolder.bind(item, events.get(i));
            binding.llUpcoming.addView(item);
        }
    }

    private void loadTodaysPhoto() {
        binding.progressBar.setVisibility(View.VISIBLE);
        final android.content.Context app = requireContext().getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            final SpacePhoto p = SpacePhotoRepository.today(app);
            main.post(() -> showPhoto(p, "TODAY'S SPACE PHOTO"));
            // then the playlist the card cycles through
            final List<SpacePhoto> list = SpacePhotoRepository.rotation(app);
            main.post(() -> {
                if (binding == null || !isAdded()) return;
                rotation.clear();
                rotation.addAll(list);
                rotIndex = 0;
                scheduleAuto();
            });
        });
    }

    private void shufflePhoto() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnShufflePhoto.setEnabled(false);
        final android.content.Context app = requireContext().getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            final SpacePhoto p = SpacePhotoRepository.random();
            main.post(() -> {
                if (binding == null || !isAdded()) return;
                binding.btnShufflePhoto.setEnabled(true);
                if (p == null) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(app, "Couldn't load a photo. Check your connection.", Toast.LENGTH_SHORT).show();
                } else {
                    showPhoto(p, "RANDOM SPACE PHOTO");
                }
            });
        });
    }

    private void showPhoto(SpacePhoto p, String label) {
        if (binding == null || !isAdded()) return;
        abortFade();                       // a manual shuffle wins over a fade in progress
        binding.progressBar.setVisibility(View.GONE);
        currentPhoto = p;
        setPhotoTexts(p, label);
        SpacePhotoDetailActivity.loadInto(p, binding.ivPhotoThumb);
        scheduleAuto();                    // keep the ticker alive
    }

    private void setPhotoTexts(SpacePhoto p, String label) {
        binding.tvPhotoLabel.setText(label);
        binding.tvPhotoTitle.setText(p.getTitle());
        String meta = p.getDate() == null ? "" : p.getDate();
        if (p.getCredit() != null && !p.getCredit().isEmpty()) meta += (meta.isEmpty() ? "" : "  ·  ") + p.getCredit();
        binding.tvPhotoDate.setText(meta + (meta.isEmpty() ? "" : "  ·  ") + "tap for details");
    }

    // ---------------------------------------------------------------------------------------------
    // Auto-rotation
    // ---------------------------------------------------------------------------------------------

    /** Makes sure the 5 s ticker is running while Home is in front. Never resets a running countdown. */
    private void scheduleAuto() {
        if (binding == null || !isResumed() || ticking) return;
        ticking = true;
        main.postDelayed(autoNext, AUTO_MS);
    }

    private void advance() {
        if (binding == null || !isAdded() || rotation.size() < 2 || pendingPhoto != null) return;
        rotIndex = (rotIndex + 1) % rotation.size();
        SpacePhoto next = rotation.get(rotIndex);
        String label = rotIndex == 0 ? "TODAY'S SPACE PHOTO" : "SPACE PHOTO";
        if (next.getLocalRes() != 0) {
            binding.ivPhotoNext.setImageResource(next.getLocalRes());
            startFade(next, label, null);
        } else {
            loadForFade(next, label, next.getImageUrl(), false);
        }
    }

    /** Loads the next picture invisibly into the overlay; fades it in once it is really ready. */
    private void loadForFade(final SpacePhoto p, final String label, final String url, final boolean isThumbTry) {
        if (binding == null || !isAdded() || url == null) { scheduleAuto(); return; }
        Glide.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean first) {
                main.post(() -> {
                    if (!isThumbTry && p.getThumbUrl() != null) loadForFade(p, label, p.getThumbUrl(), true);
                    else scheduleAuto();           // couldn't load it: stay on this photo and try the next one later
                });
                return true;
            }
            @Override public boolean onResourceReady(Drawable r, Object model, Target<Drawable> target, DataSource ds, boolean first) {
                main.post(() -> startFade(p, label, url));
                return false;                      // Glide puts it into the overlay
            }
        }).into(binding.ivPhotoNext);
    }

    private void startFade(final SpacePhoto p, final String label, final String url) {
        if (binding == null || !isAdded() || pendingPhoto != null) return;
        pendingPhoto = p;
        pendingLabel = label;
        pendingUrl = url;

        // picture: the new photo dissolves in over the old one
        binding.ivPhotoNext.animate().cancel();
        binding.ivPhotoNext.setAlpha(0f);
        binding.ivPhotoNext.animate().alpha(1f).setDuration(FADE_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::commitFade).start();

        // text: fades out, swaps while invisible, fades back in
        final View[] texts = {binding.tvPhotoTitle, binding.tvPhotoDate, binding.tvPhotoLabel};
        for (View tv : texts) tv.animate().alpha(0f).setDuration(300).start();
        main.postDelayed(() -> {
            if (binding == null || pendingPhoto != p) return;
            setPhotoTexts(p, label);
            for (View tv : texts) tv.animate().alpha(1f).setDuration(500).start();
        }, 320);
    }

    /** The new photo is fully visible: make it the real one and reset the overlay. Safe to call twice. */
    private void commitFade() {
        if (pendingPhoto == null || binding == null) { pendingPhoto = null; return; }
        final SpacePhoto p = pendingPhoto;
        final String url = pendingUrl;
        pendingPhoto = null;

        currentPhoto = p;
        setPhotoTexts(p, pendingLabel);
        for (View tv : new View[]{binding.tvPhotoTitle, binding.tvPhotoDate, binding.tvPhotoLabel}) {
            tv.animate().cancel();
            tv.setAlpha(1f);
        }
        if (url == null || p.getLocalRes() != 0) {
            binding.ivPhotoThumb.setImageResource(p.getLocalRes());
        } else {
            Glide.with(this).load(url).into(binding.ivPhotoThumb);   // already in memory: instant, no flicker
        }
        binding.ivPhotoNext.setAlpha(0f);
        Glide.with(this).clear(binding.ivPhotoNext);
        binding.ivPhotoNext.setImageDrawable(null);
        scheduleAuto();
    }

    /** Leaving the screen mid-fade: jump to the finished state instead of freezing half way. */
    private void finishFadeNow() {
        if (pendingPhoto == null || binding == null) return;
        binding.ivPhotoNext.animate().cancel();
        commitFade();
    }

    /** A manual action (shuffle) interrupts a fade: drop it and restore the overlay. */
    private void abortFade() {
        if (binding == null) return;
        pendingPhoto = null;
        binding.ivPhotoNext.animate().cancel();
        binding.ivPhotoNext.setAlpha(0f);
        Glide.with(this).clear(binding.ivPhotoNext);
        binding.ivPhotoNext.setImageDrawable(null);
        for (View tv : new View[]{binding.tvPhotoTitle, binding.tvPhotoDate, binding.tvPhotoLabel}) {
            tv.animate().cancel();
            tv.setAlpha(1f);
        }
    }

    private void loadIssPass() {
        final android.content.Context app = requireContext().getApplicationContext();
        IssRepository.refresh(app, (pos, orbit, live) -> {
            if (binding == null || !isAdded()) return;
            if (orbit == null) {
                binding.tvIssPass.setText("--");
                binding.tvIssSub.setText("Go online to predict");
                return;
            }
            double[] loc = LocationHelper.get(app);
            List<IssOrbit.Pass> passes = orbit.findPasses(loc[0], loc[1], System.currentTimeMillis(), 48, 10);
            if (passes.isEmpty()) {
                binding.tvIssPass.setText("None soon");
                binding.tvIssSub.setText("No pass in 48 h");
                return;
            }
            IssOrbit.Pass p = passes.get(0);
            long mins = Math.max(0, (p.riseMs - System.currentTimeMillis()) / 60000L);
            binding.tvIssPass.setText(mins >= 60 ? (mins / 60) + "h " + (mins % 60) + "m" : (mins == 0 ? "Now" : mins + " min"));
            binding.tvIssSub.setText("Max " + Math.round(p.maxElevDeg) + "°" + (p.visible ? " · visible" : ""));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        main.removeCallbacksAndMessages(null);
        ticking = false;
        pendingPhoto = null;
        binding = null;
    }
}
