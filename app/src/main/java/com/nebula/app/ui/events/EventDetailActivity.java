package com.nebula.app.ui.events;

import com.nebula.app.ui.BaseActivity;
import android.os.Bundle;


import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.databinding.ActivityEventDetailBinding;
import com.nebula.app.util.LocationHelper;
import com.nebula.app.util.SampleDataUtil;


public class EventDetailActivity extends BaseActivity {
    public static final String EXTRA_EVENT = "extra_event";
    private ActivityEventDetailBinding binding;
    private SkyEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        event = (SkyEvent) getIntent().getSerializableExtra(EXTRA_EVENT);
        if (event != null) {
            binding.tvEventName.setText(event.getName());
            binding.tvEventDate.setText(event.getDate());
            binding.tvEventDesc.setText(event.getDescription());
            binding.tvEventType.setText(event.getType());
            binding.tvEventType.setTextColor(SkyEventAdapter.colorFor(this, event.getType()));
            binding.tvEventCountdown.setText(SampleDataUtil.countdown(event.getTimestamp()));
            binding.ivEventHero.setImageResource(SampleDataUtil.thumbFor(event.getType()));
            double[] loc = LocationHelper.get(this);
            binding.skyChartDetail.setSky(loc[0], loc[1], event.getTimestamp());
        }
        binding.btnSetReminder.setOnClickListener(v -> {
            com.nebula.app.util.Sounds.play(this, com.nebula.app.util.Sounds.BELL);
            ReminderSheet.show(this, event);
        });
        binding.ibBack.setOnClickListener(v -> finish());
    }
}
