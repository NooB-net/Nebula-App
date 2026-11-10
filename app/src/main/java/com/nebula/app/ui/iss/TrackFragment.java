package com.nebula.app.ui.iss;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nebula.app.R;
import com.nebula.app.data.model.IssPosition;
import com.nebula.app.databinding.FragmentTrackBinding;
import com.nebula.app.network.IssRepository;
import com.nebula.app.util.IssOrbit;
import com.nebula.app.util.LocationHelper;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackFragment extends Fragment {
    private static final int REFRESH_MS = 5000;
    private FragmentTrackBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running;
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            fetch();
            handler.postDelayed(this, REFRESH_MS);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTrackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        double[] loc = LocationHelper.get(requireContext());
        binding.earthMap.setObserver(loc[0], loc[1]);
    }

    @Override public void onResume() { super.onResume(); running = true; handler.post(tick); }
    @Override public void onPause() { super.onPause(); running = false; handler.removeCallbacks(tick); }

    private void fetch() {
        final android.content.Context app = requireContext().getApplicationContext();
        IssRepository.refresh(app, (pos, orbit, live) -> {
            if (binding == null || !isAdded()) return;
            if (pos != null) {
                binding.tvLatitude.setText(String.format(Locale.US, "%.3f°", pos.getLatitude()));
                binding.tvLongitude.setText(String.format(Locale.US, "%.3f°", pos.getLongitude()));
                binding.tvAltitude.setText(String.format(Locale.US, "%.0f km", pos.getAltitude()));
                binding.tvVelocity.setText(String.format(Locale.US, "%,.0f km/h", pos.getVelocity()));
                binding.tvStatus.setText("Live · updated " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()));
                binding.earthMap.setData(orbit, pos.getLatitude(), pos.getLongitude(), true);
            } else if (orbit != null) {
                double[] p = orbit.position(System.currentTimeMillis());
                binding.tvLatitude.setText(String.format(Locale.US, "%.3f°", p[0]));
                binding.tvLongitude.setText(String.format(Locale.US, "%.3f°", p[1]));
                binding.tvAltitude.setText("~420 km");
                binding.tvVelocity.setText("~27,600 km/h");
                binding.tvStatus.setText("Offline · estimated position");
                binding.earthMap.setData(orbit, p[0], p[1], true);
            } else {
                binding.tvStatus.setText(R.string.no_internet);
            }
            if (orbit != null) showPasses(orbit, app);
        });
    }

    private void showPasses(IssOrbit orbit, android.content.Context app) {
        double[] loc = LocationHelper.get(app);
        List<IssOrbit.Pass> passes = orbit.findPasses(loc[0], loc[1], System.currentTimeMillis(), 48, 10);
        binding.llPasses.removeAllViews();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        for (int i = 0; i < Math.min(5, passes.size()); i++) {
            IssOrbit.Pass p = passes.get(i);
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_pass, binding.llPasses, false);
            ((TextView) row.findViewById(R.id.tv_pass_time)).setText(df.format(new Date(p.riseMs)));
            ((TextView) row.findViewById(R.id.tv_pass_detail)).setText(
                    "Max elevation " + Math.round(p.maxElevDeg) + "° · " + (p.durationSec / 60) + " min " + (p.durationSec % 60) + " s");
            TextView vis = row.findViewById(R.id.tv_pass_vis);
            vis.setText(p.visible ? "VISIBLE" : "DAYLIGHT");
            vis.setTextColor(ContextCompat.getColor(requireContext(), p.visible ? R.color.nebula_success : R.color.nebula_text_secondary));
            binding.llPasses.addView(row);
        }
        if (passes.isEmpty()) {
            TextView t = new TextView(requireContext());
            t.setText("No passes above 10° in the next 48 hours.");
            t.setTextColor(ContextCompat.getColor(requireContext(), R.color.nebula_text_secondary));
            binding.llPasses.addView(t);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
        handler.removeCallbacks(tick);
        binding = null;
    }
}
