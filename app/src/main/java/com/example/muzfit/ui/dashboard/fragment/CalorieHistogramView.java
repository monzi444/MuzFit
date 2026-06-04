package com.example.muzfit.ui.dashboard.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.muzfit.R;

/**
 * Custom view to display a histogram of calories burned over the last week.
 */
public class CalorieHistogramView extends View {
    private int[] caloriesData = {420, 720, 580, 1020, 850, 600, 910};
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final String[] days = {"M", "T", "W", "T", "F", "S", "S"};

    public CalorieHistogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.calorie_color));
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.muz_on_surface_variant));
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(ContextCompat.getColor(getContext(), R.color.muz_on_surface));
        valuePaint.setTextSize(20f);
        valuePaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int[] data) {
        this.caloriesData = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (caloriesData == null || caloriesData.length == 0) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 44f;
        float bottomPadding = 60f;
        float graphWidth = width - 2 * padding;
        float graphHeight = height - padding - bottomPadding - 24f;

        int maxCalories = 0;
        for (int calorie : caloriesData) {
            if (calorie > maxCalories) maxCalories = calorie;
        }
        if (maxCalories == 0) maxCalories = 1000; // Default max

        float barWidth = (graphWidth / caloriesData.length) * 0.7f;
        float spacing = (graphWidth / caloriesData.length) * 0.3f;

        for (int i = 0; i < caloriesData.length; i++) {
            float barHeight = (caloriesData[i] / (float) maxCalories) * graphHeight;
            float left = padding + i * (barWidth + spacing) + spacing / 2;
            float top = padding + graphHeight - barHeight;
            float right = left + barWidth;
            float bottom = padding + graphHeight;

            rectF.set(left, top, right, bottom);
            canvas.drawRoundRect(rectF, 10f, 10f, barPaint);

            canvas.drawText(String.valueOf(caloriesData[i]), left + barWidth / 2, top - 8f, valuePaint);
            canvas.drawText(days[i], left + barWidth / 2, height - 20f, textPaint);
        }
    }
}
