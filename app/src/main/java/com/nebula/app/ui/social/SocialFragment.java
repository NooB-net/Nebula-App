package com.nebula.app.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.ListenerRegistration;
import com.nebula.app.data.model.SocialPost;
import com.nebula.app.databinding.FragmentSocialBinding;
import com.nebula.app.network.LikesRepository;
import com.nebula.app.network.SocialRepository;
import com.nebula.app.util.Cloud;

import java.util.ArrayList;
import java.util.List;

/** The shared feed: everything posted by anyone using NEBULA, live. */
public class SocialFragment extends Fragment {
    private FragmentSocialBinding binding;
    private SocialPostAdapter adapter;
    private final List<SocialPost> posts = new ArrayList<>();
    private ListenerRegistration registration;
    private Runnable stopLikes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSocialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new SocialPostAdapter(posts, requireContext());
        binding.recyclerSocial.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerSocial.setAdapter(adapter);
        binding.fabPost.setOnClickListener(v -> {
            if (!Cloud.isConfigured(requireContext())) {
                Toast.makeText(requireContext(), "Socials isn't connected to a server yet. See FIREBASE_SETUP.md", Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(getActivity(), CreatePostActivity.class));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Cloud.isConfigured(requireContext())) {
            showStatus("Socials isn't connected to a server yet.\n\nAdd your Firebase google-services.json (see FIREBASE_SETUP.md) and everyone's posts will show up here.");
            return;
        }
        if (posts.isEmpty()) {
            binding.progressSocial.setVisibility(View.VISIBLE);
            binding.tvSocialStatus.setVisibility(View.GONE);
        }
        Cloud.ensureSignedIn(requireContext(), ok -> {
            if (binding == null || !isAdded()) return;
            if (!ok) {
                showStatus("Couldn't connect to Socials.\nCheck your internet connection and that Anonymous sign-in is enabled in Firebase.");
                return;
            }
            if (stopLikes != null) stopLikes.run();
            stopLikes = LikesRepository.listen(Cloud.currentUid(), (counts, mine) -> {
                if (binding != null && isAdded()) adapter.setLikes(counts, mine);
            });
            if (registration != null) registration.remove();
            registration = SocialRepository.listen(new SocialRepository.FeedListener() {
                @Override public void onPosts(List<SocialPost> list) {
                    if (binding == null || !isAdded()) return;
                    binding.progressSocial.setVisibility(View.GONE);
                    posts.clear();
                    posts.addAll(list);
                    adapter.notifyDataSetChanged();
                    if (list.isEmpty()) showStatus("No posts yet.\nBe the first to share the night sky.");
                    else binding.tvSocialStatus.setVisibility(View.GONE);
                }
                @Override public void onError(String message) {
                    if (binding == null || !isAdded()) return;
                    if (posts.isEmpty()) showStatus("Couldn't load posts.\n" + message);
                    else binding.progressSocial.setVisibility(View.GONE);
                }
            });
        });
    }

    private void showStatus(String text) {
        if (binding == null) return;
        binding.progressSocial.setVisibility(View.GONE);
        binding.tvSocialStatus.setText(text);
        binding.tvSocialStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) { registration.remove(); registration = null; }
        if (stopLikes != null) { stopLikes.run(); stopLikes = null; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
