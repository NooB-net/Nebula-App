package com.nebula.app.ui.iss;

import com.nebula.app.ui.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.nebula.app.databinding.ActivityIssTrackBinding;
import com.nebula.app.service.IssTrackingService;

public class IssTrackActivity extends BaseActivity {

    private ActivityIssTrackBinding binding;
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIssTrackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.ibBack.setOnClickListener(v -> finish());
        startIssService();
    }

    private void startIssService() {
        Intent serviceIntent = new Intent(this, IssTrackingService.class);
        startService(serviceIntent);
    }

    @Override protected void onStart() { super.onStart(); }
    @Override protected void onResume() { super.onResume(); }
    @Override protected void onPause() { super.onPause(); }
    @Override protected void onStop() { super.onStop(); }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null)
            refreshHandler.removeCallbacks(refreshRunnable);
    }
}
