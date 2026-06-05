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

import java.util.Locale;

public class NutrientProgressBar extends View {
    private float progress = 0;
    private float max = 100;
    private int baseColor = 0xFF4CAF50;
    private int overflowColor = 0xFF1B5E20;
    private String unit = "";
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public NutrientProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Track della barra: usiamo muz_glass_border per contrasto in entrambe le modalità
        bgPaint.setColor(ContextCompat.getColor(context, R.color.muz_glass_border));
        
        textPaint.setColor(ContextCompat.getColor(context, R.color.muz_on_surface));
        textPaint.setTextSize(getResources().getDisplayMetrics().density * 11);
        textPaint.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        textPaint.setAntiAlias(true);
    }

    public void setColors(int baseColor, int overflowColor) {
        this.baseColor = baseColor;
        this.overflowColor = overflowColor;
        invalidate();
    }

    public void setProgress(float progress, float max) {
        this.setProgress(progress, max, "");
    }

    public void setProgress(float progress, float max, String unit) {
        this.progress = Math.max(0, progress);
        this.max = Math.max(1, max);
        this.unit = unit;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;

        // 1. Disegna sfondo della barra (parte vuota)
        rect.set(0, 0, width, height);
        canvas.drawRoundRect(rect, radius, radius, bgPaint);

        if (max <= 0) return;

        // 2. Disegna la parte piena
        float progressWidth;
        if (progress <= max) {
            progressWidth = (progress / max) * width;
            if (progressWidth > 0) {
                paint.setColor(baseColor);
                rect.set(0, 0, progressWidth, height);
                canvas.drawRoundRect(rect, radius, radius, paint);
            }
        } else {
            // Caso Overflow: Disegna barra piena + parte extra in overflowColor
            paint.setColor(baseColor);
            rect.set(0, 0, width, height);
            canvas.drawRoundRect(rect, radius, radius, paint);

            float overflow = progress - max;
            float overflowWidth = Math.min(overflow / max, 1.0f) * width;
            if (overflowWidth > 0) {
                paint.setColor(overflowColor);
                rect.set(0, 0, overflowWidth, height);
                canvas.drawRoundRect(rect, radius, radius, paint);
            }
        }

        // 3. Disegna il testo informativo centrato
        if (height >= getResources().getDisplayMetrics().density * 12) {
            String label = String.format(Locale.getDefault(), "%.0f/%.0f %s", progress, max, unit);
            
            float textWidth = textPaint.measureText(label);
            float x = (width - textWidth) / 2f;
            float y = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
            
            // Ombra sottile per massima leggibilità
            textPaint.setShadowLayer(2f, 0, 1f, 0x99000000);
            canvas.drawText(label, x, y, textPaint);
            textPaint.clearShadowLayer();
        }
    }
}
