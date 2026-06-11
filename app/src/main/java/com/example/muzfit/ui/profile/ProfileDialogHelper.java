package com.example.muzfit.ui.profile;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.example.muzfit.R;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.utils.MuzFitToast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;

public class ProfileDialogHelper {

    private final FragmentActivity activity;
    private final ProfileViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public ProfileDialogHelper(FragmentActivity activity, ProfileViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void showObiettiviDialog(User currentUser) {
        if (currentUser == null) {
            return;
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_profile_generic, null);
        setupProfileDialogBlur(dialogView);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        LinearLayout container = dialogView.findViewById(R.id.llInputContainer);
        
        tvTitle.setText(R.string.profile_goals_title);

        final TextInputEditText etKcal = createStyledInput(container, activity.getString(R.string.profile_kcal_hint), 
                InputType.TYPE_CLASS_NUMBER, String.valueOf(currentUser.getCalorieGoal()));
        
        final TextInputEditText etCarbo = createStyledInput(container, activity.getString(R.string.profile_carbs_hint), 
                InputType.TYPE_CLASS_NUMBER, String.valueOf((int) currentUser.getCarbGoal()));
        
        final TextInputEditText etProteine = createStyledInput(container, activity.getString(R.string.profile_protein_hint), 
                InputType.TYPE_CLASS_NUMBER, String.valueOf((int) currentUser.getProteinGoal()));
        
        final TextInputEditText etGrassi = createStyledInput(container, activity.getString(R.string.profile_fat_hint), 
                InputType.TYPE_CLASS_NUMBER, String.valueOf((int) currentUser.getFatGoal()));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            User updated = copyUser(currentUser);
            if (etKcal.getText() != null)
                updated.setCalorieGoal(parseInt(etKcal.getText().toString(), currentUser.getCalorieGoal()));
            if (etCarbo.getText() != null)
                updated.setCarbGoal(parseFloat(etCarbo.getText().toString(), currentUser.getCarbGoal()));
            if (etProteine.getText() != null)
                updated.setProteinGoal(parseFloat(etProteine.getText().toString(), currentUser.getProteinGoal()));
            if (etGrassi.getText() != null)
                updated.setFatGoal(parseFloat(etGrassi.getText().toString(), currentUser.getFatGoal()));
            
            saveGoals(updated);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    public void showEditDialog(User currentUser, ProfileUpdateListener listener) {
        if (currentUser == null) {
            return;
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_profile_generic, null);
        setupProfileDialogBlur(dialogView);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        LinearLayout container = dialogView.findViewById(R.id.llInputContainer);

        tvTitle.setText(R.string.profile_edit_title);

        final TextInputEditText etNome = createStyledInput(container, activity.getString(R.string.profile_name_hint),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, currentUser.getName());

        final TextInputEditText etPeso = createStyledInput(container, activity.getString(R.string.profile_weight_hint),
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, String.valueOf(currentUser.getWeight()));

        final TextInputEditText etAltezza = createStyledInput(container, activity.getString(R.string.profile_height_hint),
                InputType.TYPE_CLASS_NUMBER, String.valueOf((int) currentUser.getHeight()));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
            if (nome.isEmpty()) return;

            User updated = copyUser(currentUser);
            updated.setName(nome);
            if (etPeso.getText() != null)
                updated.setWeight(parseFloat(etPeso.getText().toString(), currentUser.getWeight()));
            if (etAltezza.getText() != null)
                updated.setHeight(parseFloat(etAltezza.getText().toString(), currentUser.getHeight()));
            
            listener.onProfileUpdated(updated);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    public void showWeightEntryDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_profile_generic, null);
        setupProfileDialogBlur(dialogView);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        LinearLayout container = dialogView.findViewById(R.id.llInputContainer);

        tvTitle.setText(R.string.quick_action_weight);

        final TextInputEditText etWeight = createStyledInput(container, activity.getString(R.string.weight_hint),
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, "");

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String weightStr = etWeight.getText().toString().trim();
            if (weightStr.isEmpty()) return;

            try {
                float weight = Float.parseFloat(weightStr);
                WeightEntry entry = new WeightEntry();
                entry.setWeight(weight);
                entry.setDateMillis(System.currentTimeMillis());

                viewModel.addWeightEntry(entry).observe(lifecycleOwner, result -> {
                    if (result.isSuccess()) {
                        MuzFitToast.show(activity, R.string.profile_update_success);
                    } else if (result.isError()) {
                        MuzFitToast.showError(activity, ((Result.Error<?>) result).getMessage());
                    }
                });
                dialog.dismiss();
            } catch (NumberFormatException ignored) {}
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    public interface ProfileUpdateListener {
        void onProfileUpdated(User updatedUser);
    }

    private TextInputEditText createStyledInput(LinearLayout container, String hint, int inputType, String initialValue) {
        View inputView = LayoutInflater.from(activity).inflate(R.layout.item_dialog_input, container, false);
        TextInputLayout til = inputView.findViewById(R.id.textInputLayout);
        TextInputEditText tiet = inputView.findViewById(R.id.textInputEditText);
        
        til.setHint(hint);
        tiet.setInputType(inputType);
        tiet.setText(initialValue);
        
        container.addView(inputView);
        return tiet;
    }

    private void saveGoals(User updated) {
        viewModel.updateGoals(updated).observe(lifecycleOwner, result -> {
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                MuzFitToast.showError(activity, ((Result.Error<Void>) result).getMessage());
                return;
            }
            MuzFitToast.show(activity, R.string.profile_goals_update_success);
        });
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static User copyUser(User source) {
        User copy = new User();
        copy.setUid(source.getUid());
        copy.setName(source.getName());
        copy.setProfileImageUri(source.getProfileImageUri());
        copy.setWeight(source.getWeight());
        copy.setHeight(source.getHeight());
        copy.setGenderCode(source.getGenderCode());
        copy.setCalorieBurnGoal(source.getCalorieBurnGoal());
        copy.setCalorieGoal(source.getCalorieGoal());
        copy.setCarbGoal(source.getCarbGoal());
        copy.setProteinGoal(source.getProteinGoal());
        copy.setFatGoal(source.getFatGoal());
        return copy;
    }

    /**
     * Wires the dialog_profile_generic BlurView (Android 12+). On older devices the
     * BlurView remains transparent and the translucent glass background still
     * gives a soft frosted look.
     */
    private void setupProfileDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.profile_generic_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /**
     * Clips the given BlurView to a 28dp rounded rectangle. The XML
     * `bg_dialog_blur_rounded` background + clipToOutline combo is
     * unreliable for BlurView (which extends ConstraintLayout) on
     * some devices/emulators, so we also set a programmatic outline
     * provider. The two are belt-and-braces and both yield the same
     * 28dp corner radius as the glass card on top.
     */
    private void applyRoundedOutline(BlurView blurView) {
        if (blurView == null) return;
        final float radiusPx = 28f * blurView.getResources().getDisplayMetrics().density;
        blurView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
        blurView.setClipToOutline(true);
    }
}
