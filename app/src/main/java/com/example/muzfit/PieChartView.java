package com.example.muzfit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Custom view to display a pie chart representing activity level statistics.
 */
public class PieChartView extends View {
    private final Map<HomeFragment.ActivityLevel, Integer> stats = new HashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    /**
     * Updates the chart data based on the provided activity levels.
     */
    public void setData(List<HomeFragment.ActivityLevel> activityData) {
        stats.clear();
        for (HomeFragment.ActivityLevel level : activityData) {
            stats.put(level, stats.getOrDefault(level, 0) + 1);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stats.isEmpty()) return;

        int totalDays = 0;
        for (int count : stats.values()) {
            totalDays += count;
        }

        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height) * 0.9f;
        float left = (width - size) / 2;
        float top = (height - size) / 2;
        rectF.set(left, top, left + size, top + size);

        float currentAngle = -90f; // Start from the top
        
        // Draw GOAL segment (Green)
        currentAngle = drawSegment(canvas, HomeFragment.ActivityLevel.GOAL, totalDays, currentAngle);
        
        // Draw PARTIAL segment (Yellow)
        currentAngle = drawSegment(canvas, HomeFragment.ActivityLevel.PARTIAL, totalDays, currentAngle);
        
        // Draw NONE segment (Red)
        drawSegment(canvas, HomeFragment.ActivityLevel.NONE, totalDays, currentAngle);
    }

    private float drawSegment(Canvas canvas, HomeFragment.ActivityLevel level, int total, float startAngle) {
        float sweepAngle = getSweepAngle(level, total);
        if (sweepAngle <= 0) return startAngle;

        int colorResId;
        switch (level) {
            case GOAL:
                colorResId = R.color.activity_high;
                break;
            case PARTIAL:
                colorResId = R.color.activity_medium;
                break;
            case NONE:
            default:
                colorResId = R.color.activity_low;
                break;
        }

        paint.setColor(ContextCompat.getColor(getContext(), colorResId));
        canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

        // Draw percentage text
        if (sweepAngle > 20) { // Only draw if the slice is large enough
            float percentage = (stats.getOrDefault(level, 0) / (float) total) * 100;
            String text = String.format(Locale.getDefault(), "%.0f%%", percentage);
            
            float radius = rectF.width() / 2 * 0.7f; // Position text at 70% of radius
            double angleRad = Math.toRadians(startAngle + sweepAngle / 2);
            float x = rectF.centerX() + (float) (radius * Math.cos(angleRad));
            float y = rectF.centerY() + (float) (radius * Math.sin(angleRad)) - (textPaint.descent() + textPaint.ascent()) / 2;
            
            canvas.drawText(text, x, y, textPaint);
        }
        
        return startAngle + sweepAngle;
    }

    private float getSweepAngle(HomeFragment.ActivityLevel level, int total) {
        int count = stats.getOrDefault(level, 0);
        return (count / (float) total) * 360f;
    }
}
