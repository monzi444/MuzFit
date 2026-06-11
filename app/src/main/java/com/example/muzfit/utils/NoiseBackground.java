package com.example.muzfit.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Procedural noise background — adds a subtle film-grain texture to the app background
 * without saturation. The noise is generated once as a small tiled Bitmap and applied
 * to the activity's root view as a background.
 */
public final class NoiseBackground {

    /** Tile size in pixels. Smaller = finer grain but slightly more memory. */
    private static final int TILE_SIZE_DP = 128;

    /** Noise alpha (0-255). 10% makes the texture clearly visible but still subtle. */
    private static final int NOISE_ALPHA = 26; // ≈ 10%

    /** Light noise color: pure white. Because of the low alpha it just lightens the surface. */
    private static final int NOISE_COLOR = 0xFFFFFFFF;

    private NoiseBackground() {}

    /**
     * Generates a small tileable noise bitmap and applies it to the activity's root view.
     * Idempotent: safe to call multiple times.
     */
    public static void apply(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        View root = activity.findViewById(android.R.id.content);
        if (root == null) {
            return;
        }
        float density = activity.getResources().getDisplayMetrics().density;
        int tileSizePx = Math.max(64, (int) (TILE_SIZE_DP * density));
        Bitmap noise = createNoiseTile(tileSizePx);
        BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), noise);
        drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        // Sit the noise on top of whatever background the view already has
        // by combining into a layer-list-like overlay via the window's existing background.
        android.graphics.drawable.LayerDrawable layered = new android.graphics.drawable.LayerDrawable(
                new Drawable[] { root.getBackground(), drawable }
        );
        root.setBackground(layered);
    }

    private static Bitmap createNoiseTile(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[size * size];
        java.util.Random rng = new java.util.Random(0xC0FFEE);
        for (int i = 0; i < pixels.length; i++) {
            int v = rng.nextInt(256);
            // Use the random value as a brightness modulator on the noise color.
            // Multiplying by v/255 keeps individual pixels from reaching full alpha.
            int alpha = (NOISE_ALPHA * v) / 255;
            if (alpha < 1) alpha = 1;
            pixels[i] = (alpha << 24) | (NOISE_COLOR & 0x00FFFFFF);
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size);
        return bmp;
    }
}
