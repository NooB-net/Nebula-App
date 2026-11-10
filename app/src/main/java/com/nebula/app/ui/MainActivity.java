package com.nebula.app.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import com.nebula.app.R;
import com.nebula.app.databinding.ActivityMainBinding;
import com.nebula.app.receiver.SystemEventReceiver;
import com.nebula.app.service.IssTrackingService;
import com.nebula.app.ui.home.HomeFragment;
import com.nebula.app.ui.social.SocialFragment;
import com.nebula.app.ui.events.EventsFragment;
import com.nebula.app.ui.iss.TrackFragment;
import com.nebula.app.ui.lens.LensFragment;
import com.nebula.app.util.LocalStore;
import com.nebula.app.util.NebulaScheduler;
import com.nebula.app.util.NotifPrefs;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;

    // Reacts to airplane mode / low battery / Battery Saver while the app is visible.
    private final SystemEventReceiver systemReceiver = new SystemEventReceiver((message, conserve) -> {
        if (conserve) stopService(new Intent(this, IssTrackingService.class));
        if (binding != null) {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNav).show();
        }
    });
    private boolean receiverRegistered = false;
    private final ActivityResultLauncher<String[]> permLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) fragment = new HomeFragment();
            else if (id == R.id.nav_track) fragment = new TrackFragment();
            else if (id == R.id.nav_events) fragment = new EventsFragment();
            else if (id == R.id.nav_lens) fragment = new LensFragment();
            else if (id == R.id.nav_social) fragment = new SocialFragment();
            if (fragment != null) { loadFragment(fragment); return true; }
            return false;
        });
        if (savedInstanceState == null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
            // Re-arm only the signed-in account's reminders (others were cancelled when they logged out)
            final android.content.Context app = getApplicationContext();
            final String owner = LocalStore.ownerId(app);
            java.util.concurrent.Executors.newSingleThreadExecutor()
                    .execute(() -> com.nebula.app.util.ReminderScheduler.armAllForOwner(app, owner));
        }
        askPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The user is here: reset the "haven't opened the app" clock.
        NotifPrefs.markOpened(this);
        NebulaScheduler.cancelInactivity(this);
        registerReceiver(systemReceiver, SystemEventReceiver.filter());
        receiverRegistered = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (receiverRegistered) {
            unregisterReceiver(systemReceiver);
            receiverRegistered = false;
        }
        // Leaving: if they don't come back within a day, nudge them.
        NebulaScheduler.scheduleInactivity(this, NebulaScheduler.DAY_MS);
    }

    /** Called by fragments to jump to a tab. */
    public void selectTab(int menuId) { binding.bottomNav.setSelectedItemId(menuId); }

    private void askPermissions() {
        List<String> need = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            need.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            need.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!need.isEmpty()) permLauncher.launch(need.toArray(new String[0]));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
