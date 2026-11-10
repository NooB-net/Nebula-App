package com.nebula.app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Turns a camera/gallery image into a small base64 JPEG that fits comfortably inside one
 * Firestore document (1 MiB limit). Respects EXIF rotation so camera photos aren't sideways.
 * BLOCKING: call from a background thread.
 */
public final class ImageCodec {
    private ImageCodec() {}

    private static final int[] SIDES = {1080, 900, 720, 560};
    private static final int MAX_BYTES = 350_000; // ~470 KB once base64-encoded

    public static String toBase64Jpeg(Context c, Uri uri) throws IOException {
        ContentResolver cr = c.getContentResolver();

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = cr.openInputStream(uri)) {
            if (in == null) throw new IOException("Can't open image");
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IOException("That file isn't an image");

        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= SIDES[0]) sample *= 2;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap decoded;
        try (InputStream in = cr.openInputStream(uri)) {
            decoded = BitmapFactory.decodeStream(in, null, opts);
        }
        if (decoded == null) throw new IOException("Couldn't read the image");

        int rotation = exifRotation(cr, uri);
        try {
            for (int side : SIDES) {
                byte[] jpeg = scaleAndCompress(decoded, rotation, side);
                if (jpeg != null) return Base64.encodeToString(jpeg, Base64.NO_WRAP);
            }
            throw new IOException("Photo is too large to post");
        } finally {
            decoded.recycle();
        }
    }

    /** Scales to {@code maxSide}, then steps JPEG quality down until it fits. Null if it never fits. */
    private static byte[] scaleAndCompress(Bitmap src, int rotation, int maxSide) {
        float scale = Math.min(1f, (float) maxSide / Math.max(src.getWidth(), src.getHeight()));
        Matrix m = new Matrix();
        m.postScale(scale, scale);
        if (rotation != 0) m.postRotate(rotation);
        Bitmap out = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        try {
            for (int q = 85; q >= 40; q -= 10) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                out.compress(Bitmap.CompressFormat.JPEG, q, bos);
                if (bos.size() <= MAX_BYTES) return bos.toByteArray();
            }
            return null;
        } finally {
            if (out != src) out.recycle();
        }
    }

    private static int exifRotation(ContentResolver cr, Uri uri) {
        try (InputStream in = cr.openInputStream(uri)) {
            if (in == null) return 0;
            int o = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (o) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
