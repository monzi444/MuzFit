package com.example.muzfit.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.R;
import com.example.muzfit.model.User;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.repository.quick.IQuickRepository;
import com.example.muzfit.repository.training.ITrainingRepository;
import com.example.muzfit.ui.navbar.FloatingPillNavBridge;
import com.example.muzfit.ui.dashboard.fragment.HomeFragment;
import com.example.muzfit.ui.diet.fragment.DietFragment;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.ui.diet.viewmodel.DietViewModelFactory;
import com.example.muzfit.ui.diet.DietDialogHelper;
import com.example.muzfit.ui.profile.fragment.ProfileFragment;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModelFactory;
import com.example.muzfit.ui.profile.ProfileDialogHelper;
import com.example.muzfit.ui.quick.fragment.QuickOverlayFragment;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModel;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModelFactory;
import com.example.muzfit.ui.training.fragment.WorkoutFragment;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModel;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModelFactory;
import com.example.muzfit.ui.training.TrainingDialogHelper;
import com.example.muzfit.utils.NoiseBackground;
import com.example.muzfit.utils.ServiceLocator;
import com.example.muzfit.utils.ThemeHelper;

import androidx.compose.ui.platform.ComposeView;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.RenderEffectBlur;
// NOTE: If the IDE shows "Cannot resolve symbol 'eightbitlab'" warnings,
// run: File → Sync Project with Gradle Files, then Build → Clean Project.
// (These warnings are IDE cache issues only — the CLI build succeeds.)

public class MainActivity extends AppCompatActivity {

    private static final String QUICK_OVERLAY_TAG = "quick_overlay";
    private QuickViewModel quickViewModel;
    private User currentUser;

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
        ProfileDialogHelper profileDialogHelper = new ProfileDialogHelper(this, profileViewModel, this);

        ITrainingRepository trainingRepository = ServiceLocator.getInstance().getTrainingRepository();
        TrainingViewModel trainingViewModel = new ViewModelProvider(this, new TrainingViewModelFactory(trainingRepository))
                .get(TrainingViewModel.class);
        TrainingDialogHelper trainingDialogHelper = new TrainingDialogHelper(this, trainingViewModel, this);

        profileViewModel.getUser().observe(this, result -> {
            if (result.isSuccess()) {
                currentUser = ((com.example.muzfit.model.Result.Success<User>) result).getData();
            }
        });

        quickViewModel.getSelectedAction().observe(this, action -> {
            if (action == null) return;
            switch (action) {
                case QuickViewModel.ACTION_QUICK_MEAL:
                    dietDialogHelper.showChooseMealDialog();
                    break;
                case QuickViewModel.ACTION_UPDATE_GOAL:
                    profileDialogHelper.showObiettiviDialog(currentUser);
                    break;
                case QuickViewModel.ACTION_LOG_WEIGHT:
                    profileDialogHelper.showWeightEntryDialog();
                    break;
                case QuickViewModel.ACTION_START_WORKOUT:
                    trainingDialogHelper.showStartWorkoutDialog();
                    break;
            }
        });

        if (getSupportFragmentManager().findFragmentByTag(QUICK_OVERLAY_TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.quick_overlay_container, new QuickOverlayFragment(), QUICK_OVERLAY_TAG)
                    .commit();
        }

        ComposeView bottomNavCompose = findViewById(R.id.bottom_nav_compose);
        BlurView bottomNavBlur = findViewById(R.id.bottom_nav_blur);
        if (bottomNavBlur != null) {
            // The BlurView needs a "root" ViewGroup to capture the content behind.
            // The decor view IS a ViewGroup (FrameLayout subclass) — no cast needed.
            android.view.ViewGroup rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            float blurRadius = 15f; // 0..25 — 25% less than the previous 20f
            BlurAlgorithm algorithm = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                    ? new RenderEffectBlur()
                    : new eightbitlab.com.blurview.RenderScriptBlur(this);
            bottomNavBlur.setupWith(rootView, algorithm)
                    .setBlurRadius(blurRadius)
                    .setBlurAutoUpdate(true);

            // Resize the BlurView to match the actual pill size once the Compose pill has been measured.
            // The pill uses widthIn(max=380dp) and shrinks to its content — we read its real width/height
            // after layout so the blur doesn't extend past the visible pill shape.
            // We use OnGlobalLayoutListener (instead of a single post()) because the Compose pill
            // measures itself across several frames — the first post() may still see 0×0.
            android.view.ViewTreeObserver.OnGlobalLayoutListener layoutListener = new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int w = bottomNavCompose.getWidth();
                    int h = bottomNavCompose.getHeight();
                    if (w > 0 && h > 0) {
                        // Inset the BlurView by 24dp on each side — keeps the blur narrower than
                        // the pill, matching the visual inset of the navbar content.
                        float density = getResources().getDisplayMetrics().density;
                        int sideInsetPx = (int) (24 * density);
                        int bottomInsetPx = (int) (12 * density);
                        android.widget.FrameLayout.LayoutParams lp =
                                (android.widget.FrameLayout.LayoutParams) bottomNavBlur.getLayoutParams();
                        lp.width = Math.max(0, w - 2 * sideInsetPx);
                        lp.height = h;
                        lp.bottomMargin = bottomInsetPx;
                        lp.gravity = android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.BOTTOM;
                        bottomNavBlur.setLayoutParams(lp);
                        // Re-setup blur with the new dimensions so the captured bitmap is re-cropped.
                        bottomNavBlur.setupWith(rootView, algorithm)
                                .setBlurRadius(blurRadius)
                                .setBlurAutoUpdate(true);
                        bottomNavCompose.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            };
            bottomNavCompose.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        }
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

        // Subtle film-grain texture over the whole app background.
        NoiseBackground.apply(this);
    }
}
