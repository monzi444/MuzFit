package com.example.muzfit.ui.profile;

import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.example.muzfit.R;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.utils.MuzFitToast;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_MuzFit_Dialog);
        builder.setTitle(R.string.profile_goals_title);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        EditText etKcal = createStyledEditText(activity.getString(R.string.profile_kcal_hint), InputType.TYPE_CLASS_NUMBER);
        etKcal.setText(String.valueOf(currentUser.getCalorieGoal()));
        layout.addView(etKcal);

        EditText etCarbo = createStyledEditText(activity.getString(R.string.profile_carbs_hint), InputType.TYPE_CLASS_NUMBER);
        etCarbo.setText(String.valueOf((int) currentUser.getCarbGoal()));
        layout.addView(etCarbo);

        EditText etProteine = createStyledEditText(activity.getString(R.string.profile_protein_hint), InputType.TYPE_CLASS_NUMBER);
        etProteine.setText(String.valueOf((int) currentUser.getProteinGoal()));
        layout.addView(etProteine);

        EditText etGrassi = createStyledEditText(activity.getString(R.string.profile_fat_hint), InputType.TYPE_CLASS_NUMBER);
        etGrassi.setText(String.valueOf((int) currentUser.getFatGoal()));
        layout.addView(etGrassi);

        builder.setView(layout);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            User updated = copyUser(currentUser);
            if (hasContent(etKcal)) {
                updated.setCalorieGoal(parseInt(etKcal.getText().toString(), currentUser.getCalorieGoal()));
            }
            if (hasContent(etCarbo)) {
                updated.setCarbGoal(parseFloat(etCarbo.getText().toString(), currentUser.getCarbGoal()));
            }
            if (hasContent(etProteine)) {
                updated.setProteinGoal(parseFloat(etProteine.getText().toString(), currentUser.getProteinGoal()));
            }
            if (hasContent(etGrassi)) {
                updated.setFatGoal(parseFloat(etGrassi.getText().toString(), currentUser.getFatGoal()));
            }
            saveGoals(updated);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showWeightEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_MuzFit_Dialog);
        builder.setTitle(R.string.quick_action_weight);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etWeight = createStyledEditText(activity.getString(R.string.weight_hint), 
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etWeight);

        builder.setView(layout);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
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
            } catch (NumberFormatException ignored) {}
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
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

    private EditText createStyledEditText(String hint, int inputType) {
        EditText editText = new EditText(activity);
        editText.setHint(hint);
        editText.setInputType(inputType);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(lp);
        return editText;
    }

    private boolean hasContent(EditText editText) {
        return editText.getText() != null && editText.getText().toString().trim().length() > 0;
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

    private static User copyUser(User source) {
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
}
