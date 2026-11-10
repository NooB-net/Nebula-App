package com.nebula.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nebula.app.data.model.SkyEvent;
import com.nebula.app.util.SampleDataUtil;

import java.util.List;

/**
 * Read-only Content Provider that exposes NEBULA's sky events.
 *
 *   content://com.nebula.app.provider/events        -> all upcoming events (dir)
 *   content://com.nebula.app.provider/events/#      -> one event by id     (item)
 *
 * Optional filter: selection = "type = ?" with selectionArgs = {"METEOR"} (or ECLIPSE, MOON, PLANET).
 * The Events tab reads its list through this provider using a ContentResolver.
 * It is not exported, so only this app can use it; set android:exported="true" plus a
 * permission in the manifest if you ever want other apps to read it.
 */
public class NebulaContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.nebula.app.provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri EVENTS_URI = Uri.withAppendedPath(BASE_URI, "events");

    public static final String MIME_DIR = "vnd.android.cursor.dir/vnd.com.nebula.app.event";
    public static final String MIME_ITEM = "vnd.android.cursor.item/vnd.com.nebula.app.event";

    public static final String[] COLUMNS = {"id", "name", "type", "date", "description", "timestamp"};

    private static final int EVENTS = 1;
    private static final int EVENT_ID = 2;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        MATCHER.addURI(AUTHORITY, "events", EVENTS);
        MATCHER.addURI(AUTHORITY, "events/#", EVENT_ID);
    }

    @Override
    public boolean onCreate() { return true; }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = MATCHER.match(uri);
        if (match != EVENTS && match != EVENT_ID) return null;

        long wantedId = match == EVENT_ID ? ContentUris.parseId(uri) : -1;
        String wantedType = null;
        if (selection != null && selection.toLowerCase().contains("type")
                && selectionArgs != null && selectionArgs.length > 0) {
            wantedType = selectionArgs[0];
        }

        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        List<SkyEvent> events = SampleDataUtil.getSampleEvents();
        for (SkyEvent e : events) {
            if (wantedId >= 0 && e.getId() != wantedId) continue;
            if (wantedType != null && !wantedType.equalsIgnoreCase(e.getType())) continue;
            cursor.addRow(new Object[]{e.getId(), e.getName(), e.getType(), e.getDate(),
                    e.getDescription(), e.getTimestamp()});
        }
        if (getContext() != null) cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case EVENTS: return MIME_DIR;
            case EVENT_ID: return MIME_ITEM;
            default: return null;
        }
    }

    @Nullable @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) { return 0; }
}
