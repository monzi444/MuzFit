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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.muzfit.ui.auth.LoginActivity;
import com.example.muzfit.R;
import com.example.muzfit.model.Gender;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.repository.auth.IAuthRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.ui.auth.viewmodel.AuthViewModel;
import com.example.muzfit.ui.auth.viewmodel.AuthViewModelFactory;
import com.example.muzfit.ui.profile.ProfileDialogHelper;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.example.muzfit.utils.ThemeHelper;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private AuthViewModel authViewModel;
    private ProfileDialogHelper dialogHelper;

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
                        User updated = ProfileDialogHelper.copyUser(currentUser);
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
        dialogHelper = new ProfileDialogHelper(requireActivity(), profileViewModel, getViewLifecycleOwner());

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

        btnModificaProfilo.setOnClickListener(v -> dialogHelper.showEditDialog(currentUser, updated -> saveProfile(updated, R.string.profile_update_success)));
        btnObiettivi.setOnClickListener(v -> dialogHelper.showObiettiviDialog(currentUser));
        btnLogout.setOnClickListener(v -> logout());

        setupThemeSwitch(switchTheme);
    }

    private void setupThemeSwitch(MaterialSwitch switchTheme) {
        boolean isNightMode = ThemeHelper.isNightMode(requireContext());
        switchTheme.setChecked(isNightMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                ThemeHelper.setNightMode(requireContext(), isChecked));
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
                || tvEmailUtente.getText().toString().equals("user@example.com")) {
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

    private void saveProfile(User updated, int successMessageRes) {
        float oldWeight = currentUser != null ? currentUser.getWeight() : 0f;
        float newWeight = updated.getWeight();

        profileViewModel.updateUser(updated).observe(getViewLifecycleOwner(), result -> {
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                Toast.makeText(requireContext(),
                        ((Result.Error<Void>) result).getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (oldWeight != newWeight) {
                WeightEntry entry = new WeightEntry();
                entry.setUid(updated.getUid());
                entry.setWeight(newWeight);
                entry.setDateMillis(System.currentTimeMillis());
                profileViewModel.addWeightEntry(entry);
            }

            currentUser = updated;
            bindUser(currentUser);
            Toast.makeText(requireContext(), successMessageRes, Toast.LENGTH_SHORT).show();
        });
    }

    private String formatUidHandle(String uid) {
        return getString(R.string.profile_username_display, "@" + uid);
    }

    private void loadAvatar(String imageUri) {
        if (imageUri == null || imageUri.trim().isEmpty()
                || "null".equalsIgnoreCase(imageUri.trim())) {
            // No avatar stored: keep the XML placeholder visible. Do NOT
            // trigger Glide with a bogus URI — that would briefly show a
            // black square as Glide's error/placeholder drawable.
            Glide.with(this).clear(ivAvatar);
            ivAvatar.setImageResource(R.drawable.bg_profile_avatar_placeholder);
            return;
        }
        Glide.with(this)
                .load(Uri.parse(imageUri))
                .placeholder(R.drawable.bg_profile_avatar_placeholder)
                .error(R.drawable.bg_profile_avatar_placeholder)
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
