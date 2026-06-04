package com.example.muzfit.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.muzfit.R;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.ui.MainActivity;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileSetupActivity extends AppCompatActivity {

    private ProfileViewModel profileViewModel;
    private ShapeableImageView avatarView;
    private TextInputEditText nameInput;
    private TextInputEditText weightInput;
    private TextInputEditText heightInput;
    private TextInputEditText kcalInput;
    private TextInputEditText carbsInput;
    private TextInputEditText proteinInput;
    private TextInputEditText fatInput;
    private RadioGroup genderGroup;
    private MaterialButton saveButton;
    private String profileImageUri = "";

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    return;
                }
                profileImageUri = uri.toString();
                persistReadPermission(uri);
                loadAvatar(profileImageUri);
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_setup_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ServiceLocator serviceLocator = ServiceLocator.getInstance(getApplication());
        IProfileRepository profileRepository = serviceLocator.getProfileRepository();
        profileViewModel = new ViewModelProvider(this, new ProfileViewModelFactory(profileRepository))
                .get(ProfileViewModel.class);

        bindViews();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            nameInput.setText(currentUser.getDisplayName());
        }
    }

    private void bindViews() {
        avatarView = findViewById(R.id.profile_setup_avatar);
        nameInput = findViewById(R.id.profile_setup_name);
        weightInput = findViewById(R.id.profile_setup_weight);
        heightInput = findViewById(R.id.profile_setup_height);
        kcalInput = findViewById(R.id.profile_setup_kcal);
        carbsInput = findViewById(R.id.profile_setup_carbs);
        proteinInput = findViewById(R.id.profile_setup_protein);
        fatInput = findViewById(R.id.profile_setup_fat);
        genderGroup = findViewById(R.id.profile_setup_gender);
        saveButton = findViewById(R.id.profile_setup_save);

        findViewById(R.id.profile_setup_image_button).setOnClickListener(this::pickProfileImage);
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void pickProfileImage(View ignored) {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void saveProfile() {
        if (!hasRequiredFields()) {
            Toast.makeText(this, R.string.profile_setup_required_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser != null ? firebaseUser.getUid() : "";
        User user = new User();
        user.setUid(uid);
        user.setName(textOf(nameInput));
        user.setProfileImageUri(profileImageUri);
        user.setWeight(parseFloat(weightInput, 0f));
        user.setHeight(parseFloat(heightInput, 0f));
        user.setGenderCode(genderGroup.getCheckedRadioButtonId() == R.id.profile_setup_gender_female ? 2 : 1);
        user.setCalorieBurnGoal(0);
        user.setCalorieGoal(parseInt(kcalInput, 0));
        user.setCarbGoal(parseFloat(carbsInput, 0f));
        user.setProteinGoal(parseFloat(proteinInput, 0f));
        user.setFatGoal(parseFloat(fatInput, 0f));

        setLoading(true);
        profileViewModel.updateUser(user).observe(this, result -> {
            if (result.isLoading()) {
                return;
            }
            setLoading(false);
            if (result.isError()) {
                Toast.makeText(this, ((Result.Error<Void>) result).getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.profile_setup_saved_toast, Toast.LENGTH_SHORT).show();
            openMainActivity();
        });
    }

    private boolean hasRequiredFields() {
        return !TextUtils.isEmpty(textOf(nameInput))
                && !TextUtils.isEmpty(textOf(weightInput))
                && !TextUtils.isEmpty(textOf(heightInput))
                && !TextUtils.isEmpty(textOf(kcalInput));
    }

    private void setLoading(boolean loading) {
        saveButton.setEnabled(!loading);
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private static float parseFloat(TextInputEditText input, float fallback) {
        try {
            return Float.parseFloat(textOf(input));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(TextInputEditText input, int fallback) {
        try {
            return Integer.parseInt(textOf(input));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void loadAvatar(String imageUri) {
        Glide.with(this)
                .load(Uri.parse(imageUri))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(avatarView);
    }

    private void persistReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (RuntimeException ignored) {
        }
    }
}
