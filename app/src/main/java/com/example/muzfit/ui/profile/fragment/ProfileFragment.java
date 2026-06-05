package com.example.muzfit.ui.profile.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.muzfit.ui.auth.LoginActivity;
import com.example.muzfit.R;
import com.example.muzfit.model.Gender;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.repository.auth.IAuthRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.ui.auth.viewmodel.AuthViewModel;
import com.example.muzfit.ui.auth.viewmodel.AuthViewModelFactory;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;

    private ShapeableImageView ivAvatar;
    private TextView tvNomeUtente;
    private TextView tvEmailUtente;
    private TextView tvPeso;
    private TextView tvAltezza;
    private TextView tvEta;
    private TextView tvObiettivoKcal;
    private TextView tvCarbo;
    private TextView tvProteine;
    private TextView tvGrassi;

    private User currentUser;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    persistReadPermission(uri);
                    loadAvatar(uri.toString());
                    if (currentUser != null) {
                        User updated = copyUser(currentUser);
                        updated.setProfileImageUri(uri.toString());
                        saveProfile(updated, R.string.profile_update_success);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ServiceLocator serviceLocator = ServiceLocator.getInstance(requireActivity().getApplication());
        IProfileRepository profileRepository = serviceLocator.getProfileRepository();
        IAuthRepository authRepository = serviceLocator.getAuthRepository();

        profileViewModel = new ViewModelProvider(this, new ProfileViewModelFactory(profileRepository))
                .get(ProfileViewModel.class);
        authViewModel = new ViewModelProvider(this, new AuthViewModelFactory(authRepository))
                .get(AuthViewModel.class);

        bindViews(view);
        setupClickListeners(view);
        observeProfileData();

        return view;
    }

    private void bindViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNomeUtente = view.findViewById(R.id.tv_nome_utente);
        tvEmailUtente = view.findViewById(R.id.tv_email_utente);
        tvPeso = view.findViewById(R.id.tv_peso);
        tvAltezza = view.findViewById(R.id.tv_altezza);
        tvEta = view.findViewById(R.id.tv_eta);
        tvObiettivoKcal = view.findViewById(R.id.tv_obiettivo_kcal);
        tvCarbo = view.findViewById(R.id.tv_carbo);
        tvProteine = view.findViewById(R.id.tv_proteine);
        tvGrassi = view.findViewById(R.id.tv_grassi);
        bindLoadingPlaceholders();
    }

    private void bindLoadingPlaceholders() {
        tvNomeUtente.setText(R.string.profile_name_fallback);
        tvEmailUtente.setText(R.string.profile_stat_placeholder);
        tvPeso.setText(getString(R.string.profile_weight_format, 0f));
        tvAltezza.setText(getString(R.string.profile_height_format, 0f));
        tvEta.setText(R.string.profile_stat_placeholder);
        tvObiettivoKcal.setText(getString(R.string.profile_kcal_display, 0));
        tvCarbo.setText(getString(R.string.profile_macro_carbs, 0f));
        tvProteine.setText(getString(R.string.profile_macro_protein, 0f));
        tvGrassi.setText(getString(R.string.profile_macro_fat, 0f));
    }

    private void setupClickListeners(View view) {
        ivAvatar.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        Button btnModificaProfilo = view.findViewById(R.id.btn_modifica_profilo);
        Button btnObiettivi = view.findViewById(R.id.btn_obiettivi);
        Button btnLogout = view.findViewById(R.id.btn_logout);
        MaterialSwitch switchTheme = view.findViewById(R.id.switch_theme);

        btnModificaProfilo.setOnClickListener(v -> showEditDialog());
        btnObiettivi.setOnClickListener(v -> showObiettiviDialog());
        btnLogout.setOnClickListener(v -> logout());

        setupThemeSwitch(switchTheme);
    }

    private void setupThemeSwitch(MaterialSwitch switchTheme) {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("muzfit_prefs", Context.MODE_PRIVATE);
        boolean isNightMode = sharedPref.getBoolean("night_mode", 
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        
        switchTheme.setChecked(isNightMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.edit().putBoolean("night_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void observeProfileData() {
        profileViewModel.getUser().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                String message = ((Result.Error<User>) result).getMessage();
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser = ((Result.Success<User>) result).getData();
            bindUser(currentUser);
        });

        authViewModel.getCurrentUserEmail().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                return;
            }
            String email = ((Result.Success<String>) result).getData();
            if (email != null && !email.isEmpty()) {
                tvEmailUtente.setText(email);
            } else if (currentUser != null) {
                tvEmailUtente.setText(formatUidHandle(currentUser.getUid()));
            }
        });
    }

    private void bindUser(User user) {
        if (user == null) {
            return;
        }
        if (user.getProfileImageUri() != null && !user.getProfileImageUri().isEmpty()) {
            loadAvatar(user.getProfileImageUri());
        }
        String displayName = user.getName() != null && !user.getName().trim().isEmpty()
                ? user.getName().trim()
                : getString(R.string.profile_name_fallback);
        tvNomeUtente.setText(displayName);
        if (tvEmailUtente.getText().toString().isEmpty()
                || tvEmailUtente.getText().toString().equals("utente@example.com")) {
            tvEmailUtente.setText(formatUidHandle(user.getUid()));
        }
        tvPeso.setText(getString(R.string.profile_weight_format, user.getWeight()));
        tvAltezza.setText(getString(R.string.profile_height_format, user.getHeight()));
        tvEta.setText(user.getGender() == Gender.MALE
                ? getString(R.string.profile_gender_male)
                : getString(R.string.profile_gender_female));
        tvObiettivoKcal.setText(getString(R.string.profile_kcal_display, user.getCalorieGoal()));
        tvCarbo.setText(getString(R.string.profile_macro_carbs, user.getCarbGoal()));
        tvProteine.setText(getString(R.string.profile_macro_protein, user.getProteinGoal()));
        tvGrassi.setText(getString(R.string.profile_macro_fat, user.getFatGoal()));
    }

    private void logout() {
        authViewModel.signOut().observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                String message = ((Result.Error<Void>) result).getMessage();
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void showEditDialog() {
        if (currentUser == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.profile_edit_title);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        EditText etNome = createStyledEditText(getString(R.string.profile_name_hint),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etNome.setText(currentUser.getName());
        layout.addView(etNome);

        EditText etPeso = createStyledEditText(getString(R.string.profile_weight_hint),
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPeso.setText(String.valueOf(currentUser.getWeight()));
        layout.addView(etPeso);

        EditText etAltezza = createStyledEditText(getString(R.string.profile_height_hint),
                InputType.TYPE_CLASS_NUMBER);
        etAltezza.setText(String.valueOf((int) currentUser.getHeight()));
        layout.addView(etAltezza);

        builder.setView(layout);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            if (!hasContent(etNome)) {
                return;
            }
            User updated = copyUser(currentUser);
            updated.setName(etNome.getText().toString().trim());
            if (hasContent(etPeso)) {
                updated.setWeight(parseFloat(etPeso.getText().toString(), currentUser.getWeight()));
            }
            if (hasContent(etAltezza)) {
                updated.setHeight(parseFloat(etAltezza.getText().toString(), currentUser.getHeight()));
            }
            saveProfile(updated, R.string.profile_update_success);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showObiettiviDialog() {
        if (currentUser == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.profile_goals_title);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        EditText etKcal = createStyledEditText(getString(R.string.profile_kcal_hint), InputType.TYPE_CLASS_NUMBER);
        etKcal.setText(String.valueOf(currentUser.getCalorieGoal()));
        layout.addView(etKcal);

        EditText etCarbo = createStyledEditText(getString(R.string.profile_carbs_hint), InputType.TYPE_CLASS_NUMBER);
        etCarbo.setText(String.valueOf((int) currentUser.getCarbGoal()));
        layout.addView(etCarbo);

        EditText etProteine = createStyledEditText(getString(R.string.profile_protein_hint), InputType.TYPE_CLASS_NUMBER);
        etProteine.setText(String.valueOf((int) currentUser.getProteinGoal()));
        layout.addView(etProteine);

        EditText etGrassi = createStyledEditText(getString(R.string.profile_fat_hint), InputType.TYPE_CLASS_NUMBER);
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
            saveGoals(updated, R.string.profile_goals_update_success);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveProfile(User updated, int successMessageRes) {
        profileViewModel.updateUser(updated).observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                Toast.makeText(requireContext(),
                        ((Result.Error<Void>) result).getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser = updated;
            bindUser(currentUser);
            Toast.makeText(requireContext(), successMessageRes, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveGoals(User updated, int successMessageRes) {
        profileViewModel.updateGoals(updated).observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                Toast.makeText(requireContext(),
                        ((Result.Error<Void>) result).getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser = updated;
            bindUser(currentUser);
            Toast.makeText(requireContext(), successMessageRes, Toast.LENGTH_SHORT).show();
        });
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

    private EditText createStyledEditText(String hint, int inputType) {
        EditText editText = new EditText(requireContext());
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

    private String formatUidHandle(String uid) {
        return getString(R.string.profile_username_display, "@" + uid);
    }

    private void loadAvatar(String imageUri) {
        Glide.with(this)
                .load(Uri.parse(imageUri))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivAvatar);
    }

    private void persistReadPermission(Uri uri) {
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (RuntimeException ignored) {
        }
    }
}
