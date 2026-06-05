package com.example.muzfit.ui.dashboard.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.muzfit.R;

import java.util.Locale;

public class NutrientProgressBar extends View {

    private float progress = 0;
    private float max = 100;
    private int baseColor = 0xFF4CAF50;
    private int overflowColor = 0xFF1B5E20;
    private String unit = "";

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF trackRect = new RectF();
    private final Path clipPath = new Path();

    public NutrientProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.muz_container_l2));
        trackPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(ContextCompat.getColor(context, R.color.muz_on_surface));
        textPaint.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
    }

    public void setColors(int baseColor, int overflowColor) {
        this.baseColor = baseColor;
        this.overflowColor = overflowColor;
        invalidate();
    }

    public void setProgress(float progress, float max) {
        setProgress(progress, max, "");
    }

    public void setProgress(float progress, float max, String unit) {
        this.progress = Math.max(0, progress);
        this.max = Math.max(1, max);
        this.unit = unit != null ? unit : "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float verticalInset = density;
        float trackTop = verticalInset;
        float trackBottom = height - verticalInset;
        float trackHeight = trackBottom - trackTop;
        float radius = trackHeight / 2f;

        trackRect.set(0, trackTop, width, trackBottom);
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint);

        if (max <= 0) {
            return;
        }

        clipPath.reset();
        clipPath.addRoundRect(trackRect, radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);

        float fillRatio = Math.min(progress / max, 1f);
        float fillWidth = fillRatio * width;
        if (fillWidth > 0) {
            fillPaint.setColor(baseColor);
            canvas.drawRect(0, trackTop, fillWidth, trackBottom, fillPaint);
        }

        canvas.restore();

        if (trackHeight >= density * 10) {
            String label = unit.isEmpty()
                    ? String.format(Locale.getDefault(), "%.0f/%.0f", progress, max)
                    : String.format(Locale.getDefault(), "%.0f/%.0f %s", progress, max, unit);

            textPaint.setTextSize(Math.min(trackHeight * 0.55f, density * 12));
            float textY = trackTop + (trackHeight / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
            canvas.drawText(label, width / 2f, textY, textPaint);
        }
    }
}
