package com.nebula.app.ui.social;

import com.nebula.app.ui.BaseActivity;
import android.Manifest;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.nebula.app.databinding.ActivityCreatePostBinding;
import com.nebula.app.network.SocialRepository;
import com.nebula.app.util.Cloud;
import com.nebula.app.util.ImageCodec;
import com.nebula.app.util.LocalStore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

/** New post: pick a photo from the Camera or the Gallery, add a caption, share it with everyone. */
public class CreatePostActivity extends BaseActivity {
    private static final String KEY_IMAGE = "image_uri";
    private static final String KEY_CAMERA_URI = "camera_uri";
    private static final String KEY_CAMERA_FILE = "camera_file";
    private static final long UPLOAD_TIMEOUT_MS = 20_000L;

    private ActivityCreatePostBinding binding;
    private final Handler main = new Handler(Looper.getMainLooper());

    private Uri imageUri;       // what will be posted (camera or gallery)
    private Uri cameraUri;      // pending camera output
    private File cameraFile;
    private boolean finished;   // upload completed or timed out

    private final ActivityResultLauncher<Uri> takePicture =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && cameraUri != null) setImage(cameraUri);
            });
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) setImage(uri);
            });
    private final ActivityResultLauncher<String> mediaPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) loadRecentPhotos();
                else Toast.makeText(this, "Allow photo access to see your recent photos", Toast.LENGTH_SHORT).show();
            });
    private final ActivityResultLauncher<String> cameraPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) launchCamera();
                else Toast.makeText(this, "Camera permission is needed to take a photo", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            // The camera app can cost us our process; restore what we were doing.
            String img = savedInstanceState.getString(KEY_IMAGE);
            String cam = savedInstanceState.getString(KEY_CAMERA_URI);
            String file = savedInstanceState.getString(KEY_CAMERA_FILE);
            if (cam != null) cameraUri = Uri.parse(cam);
            if (file != null) cameraFile = new File(file);
            if (img != null) setImage(Uri.parse(img));
        }

        binding.btnCamera.setOnClickListener(v -> openCamera());
        binding.btnGallery.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.tvShowRecent.setOnClickListener(v -> showRecentPhotos());
        binding.btnPost.setOnClickListener(v -> post());
        binding.ibBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (imageUri != null) out.putString(KEY_IMAGE, imageUri.toString());
        if (cameraUri != null) out.putString(KEY_CAMERA_URI, cameraUri.toString());
        if (cameraFile != null) out.putString(KEY_CAMERA_FILE, cameraFile.getAbsolutePath());
    }

    // ---- Shared device media, read through the system MediaStore Content Provider ----
    private void showRecentPhotos() {
        String perm = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) loadRecentPhotos();
        else mediaPerm.launch(perm);
    }

    private void loadRecentPhotos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<Uri> found = new ArrayList<>();
            Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            try (Cursor c = getContentResolver().query(collection,
                    new String[]{MediaStore.Images.Media._ID}, null, null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC")) {
                if (c != null) {
                    int idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    while (c.moveToNext() && found.size() < 12)
                        found.add(ContentUris.withAppendedId(collection, c.getLong(idCol)));
                }
            } catch (RuntimeException e) {
                // permission revoked or provider unavailable: show nothing
            }
            main.post(() -> {
                if (found.isEmpty()) {
                    Toast.makeText(this, "No photos found on this device", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.llRecent.removeAllViews();
                float d = getResources().getDisplayMetrics().density;
                for (final Uri u : found) {
                    ImageView iv = new ImageView(this);
                    android.widget.LinearLayout.LayoutParams lp =
                            new android.widget.LinearLayout.LayoutParams((int) (84 * d), (int) (84 * d));
                    lp.setMarginEnd((int) (8 * d));
                    iv.setLayoutParams(lp);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv.setContentDescription("Recent photo");
                    Glide.with(this).load(u).into(iv);
                    iv.setOnClickListener(v -> setImage(u));
                    binding.llRecent.addView(iv);
                }
                binding.hsvRecent.setVisibility(View.VISIBLE);
                binding.tvShowRecent.setVisibility(View.GONE);
            });
        });
    }

    private void setImage(Uri uri) {
        imageUri = uri;
        binding.ivPostPreview.setPadding(0, 0, 0, 0);
        binding.ivPostPreview.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(uri).into(binding.ivPostPreview);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPerm.launch(Manifest.permission.CAMERA);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File dir = getExternalFilesDir("Pictures");
            if (dir != null && !dir.exists()) dir.mkdirs();
            cameraFile = File.createTempFile("NEBULA_" + ts, ".jpg", dir);
            cameraUri = FileProvider.getUriForFile(this, "com.nebula.app.fileprovider", cameraFile);
            takePicture.launch(cameraUri);
        } catch (IOException | RuntimeException e) {
            Toast.makeText(this, "Could not start the camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void post() {
        if (imageUri == null) {
            Toast.makeText(this, "Choose a photo from the Camera or Gallery first", Toast.LENGTH_SHORT).show();
            return;
        }
        final String caption = binding.etCaption.getText() == null ? "" : binding.etCaption.getText().toString().trim();
        final Uri source = imageUri;
        setBusy(true);

        // 1) shrink/encode off the main thread
        Executors.newSingleThreadExecutor().execute(() -> {
            String encoded;
            try {
                encoded = ImageCodec.toBase64Jpeg(getApplicationContext(), source);
            } catch (IOException | RuntimeException e) {
                final String msg = e.getMessage();
                main.post(() -> fail("Couldn't prepare the photo: " + msg));
                return;
            }
            final String image = encoded;
            // 2) make sure we're signed in, then upload
            main.post(() -> Cloud.ensureSignedIn(this, ok -> {
                String uid = Cloud.currentUid();
                if (!ok || uid == null) { fail("Couldn't connect to Socials. Check your connection."); return; }
                upload(uid, caption, image);
            }));
        });
    }

    private void upload(String uid, String caption, String image) {
        // Firestore queues writes while offline and only reports success once the server has them.
        // Don't leave the user staring at a spinner: after a while tell them it'll send later.
        final Runnable timeout = () -> {
            if (finished) return;
            finished = true;
            Toast.makeText(this, "No connection. Your post will upload when you're back online.", Toast.LENGTH_LONG).show();
            finish();
        };
        main.postDelayed(timeout, UPLOAD_TIMEOUT_MS);

        SocialRepository.create(UUID.randomUUID().toString(), uid, LocalStore.name(this), caption, image, (ok, err) -> {
            main.removeCallbacks(timeout);
            if (finished) return;
            finished = true;
            if (ok) {
                if (cameraFile != null) //noinspection ResultOfMethodCallIgnored
                    cameraFile.delete();
                com.nebula.app.util.Sounds.play(this, com.nebula.app.util.Sounds.POSTED);
                Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                finished = false;
                fail("Couldn't post: " + err);
            }
        });
    }

    private void fail(String message) {
        setBusy(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setBusy(boolean busy) {
        binding.progressPost.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.btnPost.setEnabled(!busy);
        binding.btnCamera.setEnabled(!busy);
        binding.btnGallery.setEnabled(!busy);
    }

    @Override
    protected void onDestroy() {
        main.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
