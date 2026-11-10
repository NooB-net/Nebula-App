package com.nebula.app.ui.lens;

import com.nebula.app.ui.BaseActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nebula.app.databinding.ActivitySkyLensBinding;
import com.nebula.app.util.LocationHelper;

public class SkyLensActivity extends BaseActivity implements SensorEventListener {
    private static final int CAMERA_PERMISSION_CODE = 101;
    private ActivitySkyLensBinding binding;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private final float[] rot = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySkyLensBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        double[] loc = LocationHelper.get(this);
        binding.constellationOverlay.setLocation(loc[0], loc[1]);
        if (rotationSensor == null)
            Toast.makeText(this, "This device has no rotation sensor, so the overlay can't track movement.", Toast.LENGTH_LONG).show();
        binding.ibBack.setOnClickListener(v -> finish());
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            binding.cameraPreview.startCamera(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            binding.cameraPreview.startCamera(this);
        } else {
            Toast.makeText(this, "Camera permission is needed for Star Lens", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationSensor != null) sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            binding.cameraPreview.startCamera(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        com.nebula.app.util.Sounds.stopConstellation();
        binding.cameraPreview.closeCamera();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rot, event.values);
            binding.constellationOverlay.updateRotation(rot);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
