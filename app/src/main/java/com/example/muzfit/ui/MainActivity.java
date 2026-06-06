package com.example.muzfit.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.R;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.repository.quick.IQuickRepository;
import com.example.muzfit.ui.diet.DietDialogHelper;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.ui.diet.viewmodel.DietViewModelFactory;
import com.example.muzfit.ui.navbar.FloatingPillNavBridge;
import com.example.muzfit.ui.dashboard.fragment.HomeFragment;
import com.example.muzfit.ui.diet.fragment.DietFragment;
import com.example.muzfit.ui.profile.fragment.ProfileFragment;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModelFactory;
import com.example.muzfit.ui.quick.fragment.QuickOverlayFragment;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModel;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModelFactory;
import com.example.muzfit.ui.training.fragment.WorkoutFragment;
import com.example.muzfit.utils.MuzFitToast;
import com.example.muzfit.utils.ServiceLocator;
import com.example.muzfit.utils.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    private static final String QUICK_OVERLAY_TAG = "quick_overlay";
    private QuickViewModel quickViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ServiceLocator.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IQuickRepository quickRepository = ServiceLocator.getInstance().getQuickRepository();
        quickViewModel = new ViewModelProvider(this, new QuickViewModelFactory(quickRepository))
                .get(QuickViewModel.class);

        IDietRepository dietRepository = ServiceLocator.getInstance().getDietRepository();
        DietViewModel dietViewModel = new ViewModelProvider(this, new DietViewModelFactory(dietRepository))
                .get(DietViewModel.class);
        DietDialogHelper dietDialogHelper = new DietDialogHelper(this, dietViewModel, this);

        IProfileRepository profileRepository = ServiceLocator.getInstance().getProfileRepository();
        ProfileViewModel profileViewModel = new ViewModelProvider(this, new ProfileViewModelFactory(profileRepository))
                .get(ProfileViewModel.class);

        quickViewModel.getSelectedAction().observe(this, action -> {
            if (action == null) return;
            if (QuickViewModel.ACTION_QUICK_MEAL.equals(action)) {
                dietDialogHelper.showChooseMealDialog();
            } else if (QuickViewModel.ACTION_UPDATE_GOAL.equals(action) || QuickViewModel.ACTION_LOG_WEIGHT.equals(action)) {
                showWeightEntryDialog(profileViewModel);
            }
        });

        if (getSupportFragmentManager().findFragmentByTag(QUICK_OVERLAY_TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.quick_overlay_container, new QuickOverlayFragment(), QUICK_OVERLAY_TAG)
                    .commit();
        }

        ComposeView bottomNavCompose = findViewById(R.id.bottom_nav_compose);
        FloatingPillNavBridge.setContent(
                bottomNavCompose,
                id -> {
                    Fragment selectedFragment = null;
                    switch (id) {
                        case "home":
                            selectedFragment = new HomeFragment();
                            quickViewModel.hide();
                            break;
                        case "diet":
                            selectedFragment = new DietFragment();
                            quickViewModel.hide();
                            break;
                        case "workout":
                            selectedFragment = new WorkoutFragment();
                            quickViewModel.hide();
                            break;
                        case "profile":
                            selectedFragment = new ProfileFragment();
                            quickViewModel.hide();
                            break;
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }
                    return kotlin.Unit.INSTANCE;
                },
                () -> {
                    quickViewModel.toggle();
                    return kotlin.Unit.INSTANCE;
                }
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void showWeightEntryDialog(ProfileViewModel viewModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_MuzFit_Dialog);
        builder.setTitle(R.string.quick_action_weight);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etWeight = new EditText(this);
        etWeight.setHint(R.string.weight_hint);
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etWeight.setLayoutParams(lp);
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

                viewModel.addWeightEntry(entry).observe(this, result -> {
                    if (result.isSuccess()) {
                        MuzFitToast.show(this, R.string.profile_update_success);
                    } else if (result.isError()) {
                        MuzFitToast.showError(this, ((com.example.muzfit.model.Result.Error<?>) result).getMessage());
                    }
                });
            } catch (NumberFormatException ignored) {}
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
