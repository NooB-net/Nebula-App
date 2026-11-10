package com.nebula.app.ui.lens;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nebula.app.databinding.FragmentLensBinding;
import com.nebula.app.util.AstroUtil;
import com.nebula.app.util.LocationHelper;

public class LensFragment extends Fragment {
    private FragmentLensBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLensBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        double[] loc = LocationHelper.get(requireContext());
        long now = System.currentTimeMillis();
        binding.skyChart.setSky(loc[0], loc[1], now);
        double[] sun = AstroUtil.sunAltAz(now, loc[0], loc[1]);
        String s = sun[0] > 0 ? "Daytime — stars hidden" : sun[0] > -12 ? "Twilight" : "Night sky";
        binding.tvSkyInfo.setText(s + " · Moon " + AstroUtil.moonPhaseName(now));
        binding.btnOpenLens.setOnClickListener(v -> startActivity(new Intent(getActivity(), SkyLensActivity.class)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
