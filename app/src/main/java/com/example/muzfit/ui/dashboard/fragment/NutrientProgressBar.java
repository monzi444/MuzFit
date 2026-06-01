package com.example.muzfit.ui.dashboard.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class NutrientProgressBar extends View {
    private float progress = 0;
    private float max = 100;
    private int baseColor = 0xFF4CAF50;
    private int overflowColor = 0xFF1B5E20;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect = new RectF();

    public NutrientProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bgPaint.setColor(0xFFEEEEEE);
    }

    public void setColors(int baseColor, int overflowColor) {
        this.baseColor = baseColor;
        this.overflowColor = overflowColor;
        invalidate();
    }

    public void setProgress(float progress, float max) {
        this.progress = progress;
        this.max = max;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;

        // Draw background
        rect.set(0, 0, width, height);
        canvas.drawRoundRect(rect, radius, radius, bgPaint);

        if (max <= 0) return;

        if (progress <= max) {
            float progressWidth = (progress / max) * width;
            if (progressWidth > 0) {
                paint.setColor(baseColor);
                rect.set(0, 0, progressWidth, height);
                canvas.drawRoundRect(rect, radius, radius, paint);
            }
        } else {
            // Draw base color for the full bar
            paint.setColor(baseColor);
            rect.set(0, 0, width, height);
            canvas.drawRoundRect(rect, radius, radius, paint);

            // Draw overflow part (e.g. at 120%, draw 20% in overflow color)
            float overflow = progress - max;
            float overflowWidth = Math.min(overflow / max, 1.0f) * width;
            if (overflowWidth > 0) {
                paint.setColor(overflowColor);
                rect.set(0, 0, overflowWidth, height);
                canvas.drawRoundRect(rect, radius, radius, paint);
            }
        }
    }
}
