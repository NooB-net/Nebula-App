package com.nebula.app.ui.events;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nebula.app.R;
import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.util.SampleDataUtil;

import java.util.ArrayList;
import java.util.List;

public class SkyEventAdapter extends RecyclerView.Adapter<SkyEventAdapter.ViewHolder> {
    private final List<SkyEvent> displayList;
    private final List<SkyEvent> fullList;

    public SkyEventAdapter(List<SkyEvent> events, Context context) {
        this.fullList = new ArrayList<>(events);
        this.displayList = new ArrayList<>(events);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sky_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewHolder.bind(holder.itemView, displayList.get(position));
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    public void filter(String type) {
        displayList.clear();
        for (SkyEvent e : fullList) if (type == null || type.equalsIgnoreCase(e.getType())) displayList.add(e);
        notifyDataSetChanged();
    }

    /** Category colour for the current theme (light and dark use different shades so both stay readable). */
    public static int colorFor(Context ctx, String type) {
        int res = R.color.nebula_text_primary;
        if (type != null) {
            switch (type.toUpperCase()) {
                case "METEOR": res = R.color.color_meteor; break;
                case "ECLIPSE": res = R.color.color_eclipse; break;
                case "MOON": res = R.color.color_moon; break;
                case "CONJUNCTION": res = R.color.color_conjunction; break;
                case "PLANET": res = R.color.color_planet; break;
                default: break;
            }
        }
        return ContextCompat.getColor(ctx, res);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) { super(itemView); }

        /** Fills an item_sky_event view and wires the click to the detail screen. */
        public static void bind(View v, SkyEvent event) {
            ((TextView) v.findViewById(R.id.tv_event_name)).setText(event.getName());
            ((TextView) v.findViewById(R.id.tv_event_date)).setText(event.getDate());
            TextView type = v.findViewById(R.id.tv_event_type);
            type.setText(event.getType());
            type.setTextColor(colorFor(v.getContext(), event.getType()));
            ((TextView) v.findViewById(R.id.tv_event_countdown)).setText(SampleDataUtil.countdown(event.getTimestamp()));
            ((ImageView) v.findViewById(R.id.iv_event_thumb)).setImageResource(SampleDataUtil.thumbFor(event.getType()));
            v.findViewById(R.id.btn_item_reminder).setOnClickListener(
                    view -> {
                        com.nebula.app.util.Sounds.play(view.getContext(), com.nebula.app.util.Sounds.BELL);
                        ReminderSheet.show(view.getContext(), event);
                    });
            v.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
                intent.putExtra(EventDetailActivity.EXTRA_EVENT, event);
                view.getContext().startActivity(intent);
            });
        }
    }
}
