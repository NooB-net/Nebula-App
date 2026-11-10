package com.nebula.app.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nebula.app.R;
import com.nebula.app.databinding.FragmentEventsBinding;
import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.provider.NebulaContentProvider;
import com.nebula.app.util.SampleDataUtil;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {
    private FragmentEventsBinding binding;
    private SkyEventAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new SkyEventAdapter(loadEventsFromProvider(), getContext());
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerEvents.setAdapter(adapter);
        binding.chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            int id = ids.isEmpty() ? R.id.chip_all : ids.get(0);
            String type = null;
            if (id == R.id.chip_meteor) type = "METEOR";
            else if (id == R.id.chip_eclipse) type = "ECLIPSE";
            else if (id == R.id.chip_moon) type = "MOON";
            else if (id == R.id.chip_planet) type = "PLANET";
            adapter.filter(type);
        });
    }

    /** Reads events through the app's Content Provider (ContentResolver -> NebulaContentProvider). */
    private List<SkyEvent> loadEventsFromProvider() {
        List<SkyEvent> out = new ArrayList<>();
        Cursor c = requireContext().getContentResolver().query(NebulaContentProvider.EVENTS_URI, null, null, null, null);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    SkyEvent e = new SkyEvent();
                    e.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                    e.setName(c.getString(c.getColumnIndexOrThrow("name")));
                    e.setType(c.getString(c.getColumnIndexOrThrow("type")));
                    e.setDate(c.getString(c.getColumnIndexOrThrow("date")));
                    e.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                    e.setTimestamp(c.getLong(c.getColumnIndexOrThrow("timestamp")));
                    out.add(e);
                }
            } finally {
                c.close();
            }
        }
        return out.isEmpty() ? SampleDataUtil.getSampleEvents() : out;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
