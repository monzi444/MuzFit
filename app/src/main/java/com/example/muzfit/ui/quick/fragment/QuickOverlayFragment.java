package com.example.muzfit.ui.quick.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.R;
import com.example.muzfit.repository.quick.IQuickRepository;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModel;
import com.example.muzfit.ui.quick.viewmodel.QuickViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;

public class QuickOverlayFragment extends Fragment {

    private QuickViewModel viewModel;
    private View overlayRoot;
    private View scrim;
    private View actionsPanel;
    private BlurView blurView;
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
        blurView = view.findViewById(R.id.quick_blur);

        // Set up the BlurView (only on Android 12+ where RenderEffectBlur is available).
        // On older devices the BlurView falls back to a transparent layer (no blur, no crash).
        setupBlur();

        IQuickRepository repository = ServiceLocator.getInstance().getQuickRepository();
        viewModel = new ViewModelProvider(requireActivity(),
                new QuickViewModelFactory(repository)).get(QuickViewModel.class);

        scrim.setOnClickListener(v -> viewModel.hide());
        // Also dismiss when tapping the blurred area
        blurView.setOnClickListener(v -> viewModel.hide());

        // Close button (top-right ×)
        view.findViewById(R.id.btn_quick_close).setOnClickListener(v -> viewModel.hide());

        // 2×2 grid of action cards. Each <include> is a View; we configure
        // its label + icon here, then wire the click.
        configureCard(view, R.id.btn_quick_meal,
                R.string.quick_action_meal, R.drawable.ic_lucide_utensils);
        configureCard(view, R.id.btn_start_workout,
                R.string.quick_action_workout, R.drawable.ic_lucide_dumbbell);
        configureCard(view, R.id.btn_log_weight,
                R.string.quick_action_weight, R.drawable.ic_lucide_scale);
        configureCard(view, R.id.btn_update_goal,
                R.string.quick_action_goal, R.drawable.ic_lucide_target);

        view.findViewById(R.id.btn_quick_meal).setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_QUICK_MEAL));
        view.findViewById(R.id.btn_start_workout).setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_START_WORKOUT));
        view.findViewById(R.id.btn_log_weight).setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_LOG_WEIGHT));
        view.findViewById(R.id.btn_update_goal).setOnClickListener(v ->
                viewModel.selectAction(QuickViewModel.ACTION_UPDATE_GOAL));

        viewModel.getOverlayVisible().observe(getViewLifecycleOwner(), visible -> {
            if (Boolean.TRUE.equals(visible)) {
                showOverlay();
            } else {
                hideOverlay();
            }
        });
    }

    /**
     * Wires the BlurView to capture the activity window content and apply a gaussian blur.
     * Uses RenderEffectBlur on Android 12+ (API 31+). On older devices the BlurView
     * stays transparent and the regular scrim handles the dimming.
     */
    private void setupBlur() {
        if (blurView == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // RenderEffectBlur is API 31+. On older devices, leave the BlurView
            // transparent and let the scrim do the work.
            return;
        }
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (!(rootView instanceof android.view.ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(4f)
                .setBlurAutoUpdate(true);
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

    /**
     * Sets the label and icon on an included quick action card.
     * The card is a LinearLayout (the root of include_quick_action_card.xml)
     * containing an ImageView (id=quick_action_icon) and a TextView
     * (id=quick_action_label).
     */
    private void configureCard(View root, int cardId, int labelRes, int iconRes) {
        View card = root.findViewById(cardId);
        if (card == null) return;
        ((TextView) card.findViewById(R.id.quick_action_label)).setText(labelRes);
        ((ImageView) card.findViewById(R.id.quick_action_icon)).setImageResource(iconRes);
    }
}
