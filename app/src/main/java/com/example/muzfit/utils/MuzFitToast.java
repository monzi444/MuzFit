package com.example.muzfit.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.muzfit.R;

public final class MuzFitToast {

    public enum Style {
        SUCCESS,
        ERROR
    }

    private MuzFitToast() {
    }

    public static void show(Context context, @StringRes int messageRes) {
        show(context, context.getString(messageRes), Style.SUCCESS);
    }

    public static void show(Context context, CharSequence message) {
        show(context, message, Style.SUCCESS);
    }

    public static void showError(Context context, CharSequence message) {
        show(context, message, Style.ERROR);
    }

    public static void show(Context context, CharSequence message, Style style) {
        if (context == null || message == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        View toastView = LayoutInflater.from(appContext).inflate(R.layout.view_muzfit_toast, null);
        TextView messageView = toastView.findViewById(R.id.tvToastMessage);
        View accent = toastView.findViewById(R.id.toastAccent);
        messageView.setText(message);

        if (style == Style.ERROR) {
            accent.setBackgroundTintList(
                    ContextCompat.getColorStateList(appContext, R.color.fat_color)
            );
            toastView.setBackgroundResource(R.drawable.muzfit_toast_background_error);
        }

        Toast toast = new Toast(appContext);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        float density = appContext.getResources().getDisplayMetrics().density;
        int bottomOffset = (int) (96 * density);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, bottomOffset);
        toast.show();
    }
}
