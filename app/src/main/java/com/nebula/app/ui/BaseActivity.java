package com.nebula.app.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Parent of every screen in the app. It logs each lifecycle callback so you can watch the
 * Activity lifecycle in Logcat (filter by tag "NebulaLifecycle"), e.g. while rotating the phone
 * or pressing Home / Back.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public static final String TAG = "NebulaLifecycle";

    private void log(String callback) {
        Log.d(TAG, getClass().getSimpleName() + "." + callback);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate (savedState " + (savedInstanceState == null ? "null" : "restored") + ")");
    }
    @Override protected void onRestart() { super.onRestart(); log("onRestart"); }
    @Override protected void onStart() { super.onStart(); log("onStart"); }
    @Override protected void onResume() { super.onResume(); log("onResume"); }
    @Override protected void onPause() { super.onPause(); log("onPause"); }
    @Override protected void onStop() { super.onStop(); log("onStop"); }
    @Override protected void onSaveInstanceState(@NonNull Bundle out) { super.onSaveInstanceState(out); log("onSaveInstanceState"); }
    @Override public void onConfigurationChanged(@NonNull Configuration c) {
        super.onConfigurationChanged(c);
        log("onConfigurationChanged (orientation " + (c.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait") + ")");
    }
    @Override protected void onDestroy() { log("onDestroy"); super.onDestroy(); }
}
