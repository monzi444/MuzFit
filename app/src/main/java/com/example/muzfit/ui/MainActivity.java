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

import com.example.muzfit.repository.quick.IQuickRepository;

import com.example.muzfit.ui.navbar.FloatingPillNavBridge;

import com.example.muzfit.ui.dashboard.fragment.HomeFragment;

import com.example.muzfit.ui.diet.fragment.DietFragment;

import com.example.muzfit.ui.profile.fragment.ProfileFragment;

import com.example.muzfit.ui.quick.fragment.QuickOverlayFragment;

import com.example.muzfit.ui.quick.viewmodel.QuickViewModel;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModelFactory;
import com.example.muzfit.ui.training.fragment.WorkoutFragment;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.ui.diet.viewmodel.DietViewModelFactory;
import com.example.muzfit.ui.diet.DietDialogHelper;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.utils.ServiceLocator;
import com.example.muzfit.utils.ThemeHelper;

import androidx.compose.ui.platform.ComposeView;



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

        quickViewModel.getSelectedAction().observe(this, action -> {
            if (QuickViewModel.ACTION_QUICK_MEAL.equals(action)) {
                dietDialogHelper.showChooseMealDialog();
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

}


