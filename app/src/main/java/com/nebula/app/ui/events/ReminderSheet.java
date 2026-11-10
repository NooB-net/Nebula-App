package com.nebula.app.ui.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.nebula.app.R;
import com.nebula.app.data.database.NebulaDatabase;
import com.nebula.app.data.model.Reminder;
import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.util.LocalStore;
import com.nebula.app.util.ReminderScheduler;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Reminders-app style picker for a sky event: preset offsets or a custom date/time,
 * optional note, repeat, and a list of the reminders already set for the event.
 */
public final class ReminderSheet {
    private ReminderSheet() {}

    private static final long MIN = 60_000L;
    private static final long HOUR = 60 * MIN;
    private static final long DAY = 24 * HOUR;

    private static final int[] CHIP_IDS = {
            R.id.ch_at_time, R.id.ch_5m, R.id.ch_15m, R.id.ch_30m, R.id.ch_1h, R.id.ch_1d, R.id.ch_1w};
    private static final long[] OFFSETS = {0, 5 * MIN, 15 * MIN, 30 * MIN, HOUR, DAY, 7 * DAY};

    public static void show(final Context ctx, final SkyEvent event) {
        if (event == null) return;
        final long now = System.currentTimeMillis();
        if (event.getTimestamp() <= now) {
            Toast.makeText(ctx, "This event has already started", Toast.LENGTH_SHORT).show();
            return;
        }

        final Handler main = new Handler(Looper.getMainLooper());
        final Context app = ctx.getApplicationContext();
        final BottomSheetDialog dialog = new BottomSheetDialog(ctx, R.style.Theme_Nebula_BottomSheetDialog);
        final View root = LayoutInflater.from(ctx).inflate(R.layout.dialog_reminder, null);
        dialog.setContentView(root);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getBehavior().setSkipCollapsed(true);

        ((TextView) root.findViewById(R.id.tv_rem_event)).setText(
                event.getName() + " · " + event.getDate());

        final ChipGroup cgWhen = root.findViewById(R.id.cg_when);
        final ChipGroup cgRepeat = root.findViewById(R.id.cg_repeat);
        final TextView tvCustom = root.findViewById(R.id.tv_custom_time);
        final TextInputEditText etNote = root.findViewById(R.id.et_rem_note);
        final long[] customTime = {0};

        // Disable presets that would already be in the past. Default to "30 min before" when
        // possible, otherwise the largest preset that still works.
        int defaultChip = R.id.ch_custom;
        for (int i = CHIP_IDS.length - 1; i >= 0; i--) {
            Chip chip = root.findViewById(CHIP_IDS[i]);
            boolean ok = event.getTimestamp() - OFFSETS[i] > now + 5_000L;
            chip.setEnabled(ok);
            if (ok && defaultChip == R.id.ch_custom) defaultChip = CHIP_IDS[i];
        }
        if (root.findViewById(R.id.ch_30m).isEnabled()) defaultChip = R.id.ch_30m;
        cgWhen.check(defaultChip);
        tvCustom.setVisibility(defaultChip == R.id.ch_custom ? View.VISIBLE : View.GONE);

        cgWhen.setOnCheckedStateChangeListener((group, ids) -> {
            boolean custom = !ids.isEmpty() && ids.get(0) == R.id.ch_custom;
            tvCustom.setVisibility(custom ? View.VISIBLE : View.GONE);
            if (custom && customTime[0] == 0) pickDateTime(ctx, event, customTime, tvCustom);
        });
        tvCustom.setOnClickListener(v -> pickDateTime(ctx, event, customTime, tvCustom));

        root.findViewById(R.id.btn_rem_save).setOnClickListener(v -> {
            long trigger;
            int checked = cgWhen.getCheckedChipId();
            if (checked == R.id.ch_custom) {
                trigger = customTime[0];
                if (trigger == 0) {
                    Toast.makeText(ctx, "Pick a date and time first", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                trigger = event.getTimestamp();
                for (int i = 0; i < CHIP_IDS.length; i++) if (CHIP_IDS[i] == checked) trigger -= OFFSETS[i];
            }
            if (trigger <= System.currentTimeMillis()) {
                Toast.makeText(ctx, "That time has already passed", Toast.LENGTH_SHORT).show();
                return;
            }

            int rep = Reminder.REPEAT_NONE;
            int rc = cgRepeat.getCheckedChipId();
            if (rc == R.id.ch_rep_daily) rep = Reminder.REPEAT_DAILY;
            else if (rc == R.id.ch_rep_weekly) rep = Reminder.REPEAT_WEEKLY;

            final Reminder r = new Reminder();
            r.setOwnerId(LocalStore.ownerId(app));
            r.setEventId(event.getId());
            r.setEventName(event.getName());
            r.setEventTime(event.getTimestamp());
            r.setTriggerTime(trigger);
            r.setNote(etNote.getText() == null ? "" : etNote.getText().toString().trim());
            r.setRepeat(rep);
            r.setActive(true);

            final long fireAt = trigger;
            Executors.newSingleThreadExecutor().execute(() -> {
                long id = NebulaDatabase.getInstance(app).reminderDao().insert(r);
                r.setId((int) id);
                ReminderScheduler.schedule(app, r);
                main.post(() -> Toast.makeText(app, "Reminder set for " + fmt(fireAt), Toast.LENGTH_SHORT).show());
            });
            dialog.dismiss();
        });

        loadExisting(app, main, root, event, dialog);
        Context host = ctx;
        while (host instanceof android.content.ContextWrapper && !(host instanceof androidx.lifecycle.LifecycleOwner))
            host = ((android.content.ContextWrapper) host).getBaseContext();
        if (host instanceof androidx.lifecycle.LifecycleOwner) {
            ((androidx.lifecycle.LifecycleOwner) host).getLifecycle().addObserver(
                    (androidx.lifecycle.LifecycleEventObserver) (src, e) -> {
                        if (e == androidx.lifecycle.Lifecycle.Event.ON_DESTROY && dialog.isShowing()) dialog.dismiss();
                    });
        }
        dialog.show();
    }

    private static void pickDateTime(final Context ctx, final SkyEvent event, final long[] out, final TextView label) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Math.max(System.currentTimeMillis() + HOUR, 0));
        new DatePickerDialog(ctx, (view, y, m, d) -> {
            new TimePickerDialog(ctx, (tv, h, min) -> {
                Calendar c = Calendar.getInstance();
                c.set(y, m, d, h, min, 0);
                c.set(Calendar.MILLISECOND, 0);
                if (c.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(ctx, "Pick a time in the future", Toast.LENGTH_SHORT).show();
                    return;
                }
                out[0] = c.getTimeInMillis();
                label.setText(fmt(out[0]));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                    android.text.format.DateFormat.is24HourFormat(ctx)).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static void loadExisting(final Context app, final Handler main, final View root,
                                     final SkyEvent event, final BottomSheetDialog dialog) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<Reminder> list = NebulaDatabase.getInstance(app).reminderDao().getActiveForEvent(event.getId(), LocalStore.ownerId(app));
            main.post(() -> {
                if (!dialog.isShowing()) return;
                LinearLayout wrap = root.findViewById(R.id.ll_rem_existing_wrap);
                LinearLayout box = root.findViewById(R.id.ll_rem_existing);
                box.removeAllViews();
                wrap.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                for (final Reminder r : list) {
                    LinearLayout row = new LinearLayout(root.getContext());
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(0, 10, 0, 10);

                    TextView t = new TextView(root.getContext());
                    String label = fmt(r.getTriggerTime());
                    if (r.getRepeat() == Reminder.REPEAT_DAILY) label += " · daily";
                    else if (r.getRepeat() == Reminder.REPEAT_WEEKLY) label += " · weekly";
                    if (r.getNote() != null && !r.getNote().isEmpty()) label += "\n" + r.getNote();
                    t.setText(label);
                    t.setTextColor(ContextCompat.getColor(root.getContext(), R.color.nebula_text_body));
                    t.setTextSize(13);
                    row.addView(t, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView del = new TextView(root.getContext());
                    del.setText("Delete");
                    del.setTextColor(ContextCompat.getColor(root.getContext(), R.color.nebula_danger));
                    del.setTextSize(13);
                    del.setPadding(24, 12, 8, 12);
                    del.setOnClickListener(v -> {
                        ReminderScheduler.cancel(app, r.getId());
                        Executors.newSingleThreadExecutor().execute(() -> {
                            NebulaDatabase.getInstance(app).reminderDao().delete(r.getId());
                            main.post(() -> loadExisting(app, main, root, event, dialog));
                        });
                    });
                    row.addView(del);
                    box.addView(row);
                }
            });
        });
    }

    private static String fmt(long t) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(t));
    }
}
