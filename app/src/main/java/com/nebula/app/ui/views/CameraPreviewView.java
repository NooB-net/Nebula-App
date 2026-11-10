package com.nebula.app.ui.views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import java.util.Collections;

public class CameraPreviewView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "CameraPreviewView";
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewBuilder;
    private CameraManager cameraManager;
    private String cameraId;
    private LifecycleOwner lifecycleOwner;
    private boolean opening = false;

    public CameraPreviewView(Context context) { this(context, null); }
    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void startCamera(LifecycleOwner owner) {
        this.lifecycleOwner = owner;
        if (isAvailable()) openCamera();
        // else onSurfaceTextureAvailable will call openCamera
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int w, int h) { applyTransform(); }
    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        closeCamera(); return true;
    }
    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}

    private void openCamera() {
        if (cameraDevice != null || opening) return;
        opening = true;
        try {
            cameraId = getCameraId();
            if (cameraId == null) { opening = false; return; }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    opening = false;
                    startPreview();
                }
                @Override
                public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); cameraDevice = null; opening = false; }
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    opening = false;
                    Log.e(TAG, "Camera error: " + error);
                }
            }, null);
        } catch (CameraAccessException | SecurityException e) {
            opening = false;
            Log.e(TAG, "openCamera error", e);
        }
    }

    private String getCameraId() throws CameraAccessException {
        for (String id : cameraManager.getCameraIdList()) {
            CameraCharacteristics ch = cameraManager.getCameraCharacteristics(id);
            Integer facing = ch.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) return id;
        }
        return null;
    }

    private void startPreview() {
        try {
            SurfaceTexture texture = getSurfaceTexture();
            if (texture == null) return;
            texture.setDefaultBufferSize(1920, 1080);
            post(this::applyTransform);
            Surface surface = new Surface(texture);
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                previewBuilder.set(CaptureRequest.CONTROL_MODE,
                                        CaptureRequest.CONTROL_MODE_AUTO);
                                captureSession.setRepeatingRequest(
                                        previewBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "preview error", e);
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "startPreview error", e);
        }
    }

    /** Centre-crop the portrait (9:16) camera image to fill the view without stretching. */
    private void applyTransform() {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        android.graphics.Matrix m = new android.graphics.Matrix();
        float cx = w / 2f, cy = h / 2f, contentW = h * 9f / 16f;
        if (contentW >= w) m.setScale(contentW / w, 1f, cx, cy);
        else m.setScale(1f, (w * 16f / 9f) / h, cx, cy);
        setTransform(m);
    }

    @Override
    protected void onDetachedFromWindow() {
        closeCamera();
        super.onDetachedFromWindow();
    }

    public void closeCamera() {
        if (captureSession != null) { captureSession.close(); captureSession = null; }
        if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
        opening = false;
    }
}
