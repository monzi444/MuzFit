package com.example.muzfit.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.muzfit.R;
import com.example.muzfit.ui.components.FloatingPillNavBridge;
import com.example.muzfit.ui.dashboard.fragment.HomeFragment;
import com.example.muzfit.ui.diet.fragment.DietFragment;
import com.example.muzfit.ui.profile.fragment.ProfileFragment;
import com.example.muzfit.ui.quick.QuickOverlayHelper;
import com.example.muzfit.ui.training.fragment.WorkoutFragment;
import com.example.muzfit.utils.ServiceLocator;
import androidx.compose.ui.platform.ComposeView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ServiceLocator.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        ComposeView bottomNavCompose = findViewById(R.id.bottom_nav_compose);
        ComposeView quickCompose = findViewById(R.id.quick_overlay_compose);
        QuickOverlayHelper.INSTANCE.init(quickCompose);

        FloatingPillNavBridge.setContent(
                bottomNavCompose,
                id -> {
                    Fragment selectedFragment = null;
                    switch (id) {
                        case "home":
                            selectedFragment = new HomeFragment();
                            QuickOverlayHelper.hide();
                            break;
                        case "diet":
                            selectedFragment = new DietFragment();
                            QuickOverlayHelper.hide();
                            break;
                        case "workout":
                            selectedFragment = new WorkoutFragment();
                            QuickOverlayHelper.hide();
                            break;
                        case "profile":
                            selectedFragment = new ProfileFragment();
                            QuickOverlayHelper.hide();
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
                    QuickOverlayHelper.toggle();
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
