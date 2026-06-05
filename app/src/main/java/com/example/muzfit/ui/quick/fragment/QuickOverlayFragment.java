package com.example.muzfit.ui.quick.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.R;
import com.example.muzfit.repository.quick.IQuickRepository;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModel;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.button.MaterialButton;

public class QuickOverlayFragment extends Fragment {

    private QuickViewModel viewModel;
    private View overlayRoot;
    private View scrim;
    private View actionsPanel;
    private boolean isAnimating;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quick, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        overlayRoot = view.findViewById(R.id.quick_overlay_root);
        scrim = view.findViewById(R.id.quick_scrim);
        actionsPanel = view.findViewById(R.id.quick_actions_panel);

        IQuickRepository repository = ServiceLocator.getInstance().getQuickRepository();
        viewModel = new ViewModelProvider(requireActivity(),
                new QuickViewModelFactory(repository)).get(QuickViewModel.class);

        scrim.setOnClickListener(v -> viewModel.hide());

        MaterialButton btnQuickMeal = view.findViewById(R.id.btn_quick_meal);
        MaterialButton btnStartWorkout = view.findViewById(R.id.btn_start_workout);
        MaterialButton btnLogWeight = view.findViewById(R.id.btn_log_weight);
        MaterialButton btnUpdateGoal = view.findViewById(R.id.btn_update_goal);

        btnQuickMeal.setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_QUICK_MEAL));
        btnStartWorkout.setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_START_WORKOUT));
        btnLogWeight.setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_LOG_WEIGHT));
        btnUpdateGoal.setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_UPDATE_GOAL));

        viewModel.getOverlayVisible().observe(getViewLifecycleOwner(), visible -> {
            if (Boolean.TRUE.equals(visible)) {
                showOverlay();
            } else {
                hideOverlay();
            }
        });
    }

    private void showOverlay() {
        if (overlayRoot.getVisibility() == View.VISIBLE || isAnimating) {
            return;
        }

        overlayRoot.setVisibility(View.VISIBLE);
        isAnimating = true;

        Animation scrimIn = AnimationUtils.loadAnimation(requireContext(), R.anim.quick_fade_in);
        Animation panelIn = AnimationUtils.loadAnimation(requireContext(), R.anim.quick_slide_in_up);

        panelIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        scrim.startAnimation(scrimIn);
        actionsPanel.startAnimation(panelIn);
    }

    private void hideOverlay() {
        if (overlayRoot.getVisibility() != View.VISIBLE) {
            return;
        }

        if (isAnimating) {
            overlayRoot.setVisibility(View.GONE);
            isAnimating = false;
            return;
        }

        isAnimating = true;
        Animation scrimOut = AnimationUtils.loadAnimation(requireContext(), R.anim.quick_fade_out);
        Animation panelOut = AnimationUtils.loadAnimation(requireContext(), R.anim.quick_slide_out_down);

        panelOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                overlayRoot.setVisibility(View.GONE);
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        scrim.startAnimation(scrimOut);
        actionsPanel.startAnimation(panelOut);
    }
}
