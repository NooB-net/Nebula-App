package com.nebula.app.ui.profile;

import com.nebula.app.ui.BaseActivity;
import android.content.Intent;
import android.os.Bundle;


import com.nebula.app.databinding.ActivityProfileBinding;
import com.nebula.app.ui.auth.LoginActivity;
import com.nebula.app.util.Cloud;
import com.nebula.app.util.LocalStore;
import com.nebula.app.util.NebulaScheduler;
import com.nebula.app.util.NotifPrefs;

public class ProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityProfileBinding binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String email = LocalStore.email(this);
        binding.tvEmail.setText(email != null ? email : "Guest Mode");
        binding.tvUid.setText(LocalStore.name(this));
        binding.swDailyPhoto.setChecked(NotifPrefs.dailyPhotoEnabled(this));
        binding.swDailyPhoto.setOnCheckedChangeListener((b, on) -> {
            NotifPrefs.setDailyPhotoEnabled(this, on);
            if (on) NebulaScheduler.scheduleDailyPhoto(this); else NebulaScheduler.cancelDailyPhoto(this);
        });
        binding.swHourlyPhoto.setChecked(NotifPrefs.hourlyEnabled(this));
        binding.swHourlyPhoto.setOnCheckedChangeListener((b, on) -> {
            NotifPrefs.setHourlyEnabled(this, on);
            if (on) NebulaScheduler.scheduleHourlyPhoto(this); else NebulaScheduler.cancelHourlyPhoto(this);
        });
        binding.swSound.setChecked(NotifPrefs.soundEnabled(this));
        binding.swSound.setOnCheckedChangeListener((b, on) -> {
            NotifPrefs.setSoundEnabled(this, on);
            if (on) com.nebula.app.util.Sounds.play(this, com.nebula.app.util.Sounds.LIKE);
        });
        binding.swNudge.setChecked(NotifPrefs.nudgeEnabled(this));
        binding.swNudge.setOnCheckedChangeListener((b, on) -> {
            NotifPrefs.setNudgeEnabled(this, on);
            if (!on) NebulaScheduler.cancelInactivity(this);
        });
        binding.btnLogout.setOnClickListener(v -> {
            final android.content.Context app = getApplicationContext();
            final String owner = LocalStore.ownerId(app);
            final boolean guest = LocalStore.isGuest(app);
            java.util.concurrent.Executors.newSingleThreadExecutor()
                    .execute(() -> com.nebula.app.util.ReminderScheduler.cancelAllForOwner(app, owner, guest));
            LocalStore.logout(this);
            Cloud.signOut(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        binding.ibBack.setOnClickListener(v -> finish());
    }
}
