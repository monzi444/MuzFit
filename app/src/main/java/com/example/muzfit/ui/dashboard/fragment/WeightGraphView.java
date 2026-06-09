package com.example.muzfit.ui.dashboard.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.muzfit.R;

import java.util.Locale;

public class WeightGraphView extends View {
    private float[] data = {75.0f, 74.5f, 74.8f, 74.2f, 73.9f, 73.5f, 73.2f};
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private static final float LABEL_GAP = 12f; // gap between y-labels and chart

    public WeightGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Card border is rendered by the MaterialCardView (MuzFit.Card.Graph).
        // The View itself is transparent and only draws the chart content.
        setBackgroundColor(android.graphics.Color.TRANSPARENT);

        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.graph_line));
        linePaint.setStrokeWidth(7f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.graph_dot));
        dotPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.graph_grid));
        gridPaint.setStrokeWidth(2f);

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.muz_on_surface_variant));
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setData(float[] data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.length == 0) return;

        float width = getWidth();
        float height = getHeight();

        float labelWidth = 0f;
        float minWeight = getMinWeight();
        float maxWeight = getMaxWeight(minWeight);
        float weightRange = maxWeight - minWeight;
        for (int i = 0; i <= 4; i++) {
            String label = String.format(Locale.getDefault(), "%.1f kg", minWeight + (i * weightRange / 4));
            labelWidth = Math.max(labelWidth, textPaint.measureText(label));
        }

        float leftPadding = getPaddingLeft() + labelWidth + LABEL_GAP;
        float rightPadding = getPaddingRight();
        float topPadding = getPaddingTop();
        float bottomPadding = getPaddingBottom();
        float graphWidth = Math.max(0f, width - leftPadding - rightPadding);
        float graphHeight = Math.max(0f, height - topPadding - bottomPadding);

        // Draw grid lines and labels
        for (int i = 0; i <= 4; i++) {
            float y = topPadding + graphHeight - (i * graphHeight / 4);
            canvas.drawLine(leftPadding, y, width - rightPadding, y, gridPaint);
            String label = String.format(Locale.getDefault(), "%.1f kg", minWeight + (i * weightRange / 4));
            canvas.drawText(label, leftPadding - LABEL_GAP, y + 9f, textPaint);
        }

        path.reset();
        float stepX = data.length > 1 ? graphWidth / (data.length - 1) : 0f;

        for (int i = 0; i < data.length; i++) {
            float x = data.length > 1 ? leftPadding + i * stepX : leftPadding + (graphWidth / 2f);
            float y = topPadding + graphHeight - ((data[i] - minWeight) / weightRange * graphHeight);

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        canvas.drawPath(path, linePaint);

        // Draw dots
        for (int i = 0; i < data.length; i++) {
            float x = data.length > 1 ? leftPadding + i * stepX : leftPadding + (graphWidth / 2f);
            float y = topPadding + graphHeight - ((data[i] - minWeight) / weightRange * graphHeight);
            canvas.drawCircle(x, y, 11f, dotPaint);
        }
    }

    private float getMinWeight() {
        float min = data[0];
        for (float value : data) {
            min = Math.min(min, value);
        }
        return (float) Math.floor(min - 1f);
    }

    private float getMaxWeight(float minWeight) {
        float max = data[0];
        for (float value : data) {
            max = Math.max(max, value);
        }
        return Math.max(minWeight + 1f, (float) Math.ceil(max + 1f));
    }
}
