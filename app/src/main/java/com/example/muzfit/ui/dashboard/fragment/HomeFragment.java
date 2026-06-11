package com.example.muzfit.ui.dashboard.fragment;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import kotlin.Unit;

import com.example.muzfit.R;
import com.example.muzfit.adapter.WeightHistoryAdapter;
import com.example.muzfit.model.DashboardCalendarDay;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.ui.dashboard.viewmodel.DashboardViewModel;
import com.example.muzfit.ui.dashboard.viewmodel.DashboardViewModelFactory;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.MuzFitToast;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.button.MaterialButton;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import static com.example.muzfit.ui.dashboard.fragment.MonthlyCalendarViewKt.setupMonthlyCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final float DEFAULT_CALORIE_GOAL = 2000f;
    private static final float DEFAULT_PROTEIN_GOAL = 150f;
    private static final float DEFAULT_CARBS_GOAL = 250f;
    private static final float DEFAULT_FAT_GOAL = 70f;

    private DashboardViewModel viewModel;
    private ComposeView monthlyCalendarCompose;
    private NutrientProgressBar calorieBar;
    private NutrientProgressBar proteinBar;
    private NutrientProgressBar carbsBar;
    private NutrientProgressBar fatBar;
    private CalorieHistogramView histogram;
    private WeightGraphView weightGraph;
    private int selectedMonth;
    private int selectedYear;
    private float consumedCalories;
    private float consumedProteins;
    private float consumedCarbs;
    private float consumedFats;
    private float calorieGoal = DEFAULT_CALORIE_GOAL;
    private float proteinGoal = DEFAULT_PROTEIN_GOAL;
    private float carbsGoal = DEFAULT_CARBS_GOAL;
    private float fatGoal = DEFAULT_FAT_GOAL;
    private int todayCaloriesBurned;
    private long selectedDateMillis = System.currentTimeMillis();
    // Day-of-month → progress toward calorie target (0.0 … 1.5+)
    private final Map<Integer, Float> dayProgressMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        DashboardViewModelFactory factory = new DashboardViewModelFactory(
                ServiceLocator.getInstance().getDashboardRepository()
        );
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        setupMacros(view);
        setupCalendarControls(view);
        setupCaloriesBurned(view);
        setupWeightGraph(view);
        observeDashboardData();

        return view;
    }

    private void setupCalendarControls(View view) {
        monthlyCalendarCompose = view.findViewById(R.id.monthlyCalendarCompose);

        Calendar today = Calendar.getInstance();
        selectedMonth = today.get(Calendar.MONTH);
        selectedYear = today.get(Calendar.YEAR);

        renderSelectedMonth();
    }

    private void moveMonth(int offset) {
        selectedMonth += offset;
        if (selectedMonth < Calendar.JANUARY) {
            selectedMonth = Calendar.DECEMBER;
            selectedYear--;
        } else if (selectedMonth > Calendar.DECEMBER) {
            selectedMonth = Calendar.JANUARY;
            selectedYear++;
        }
        renderSelectedMonth();
    }

    private void renderSelectedMonth() {
        viewModel.getCalendarData(selectedYear, selectedMonth).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<DashboardCalendarDay> calendarData =
                        ((Result.Success<List<DashboardCalendarDay>>) result).getData();
                updateMonthlyCalendar(calendarData);
            }
        });
    }

    private void setupMacros(View view) {
        calorieBar = view.findViewById(R.id.calorie_progress);
        if (calorieBar != null) {
            calorieBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.muz_primary_lime),
                ContextCompat.getColor(requireContext(), R.color.protein_overflow)
            );
            calorieBar.setShowTextBox(true);
            calorieBar.setProgress(0, calorieGoal, "kcal");
        }

        proteinBar = view.findViewById(R.id.protein_progress);
        if (proteinBar != null) {
            proteinBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.protein_color),
                ContextCompat.getColor(requireContext(), R.color.protein_overflow)
            );
            proteinBar.setShowTextBox(true);
            proteinBar.setProgress(0, proteinGoal, "g");
        }

        carbsBar = view.findViewById(R.id.carbs_progress);
        if (carbsBar != null) {
            carbsBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.carbs_color),
                ContextCompat.getColor(requireContext(), R.color.carbs_overflow)
            );
            carbsBar.setShowTextBox(true);
            carbsBar.setProgress(0, carbsGoal, "g");
        }

        fatBar = view.findViewById(R.id.fat_progress);
        if (fatBar != null) {
            fatBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.fat_color),
                ContextCompat.getColor(requireContext(), R.color.fat_overflow)
            );
            fatBar.setShowTextBox(true);
            fatBar.setProgress(0, fatGoal, "g");
        }
    }

    private void setupCaloriesBurned(View view) {
        histogram = view.findViewById(R.id.weekly_calories_histogram);

        if (histogram != null) {
            histogram.setData(new int[7]);
        }
    }

    private void setupWeightGraph(View view) {
        weightGraph = view.findViewById(R.id.weight_graph);
        if (weightGraph != null) {
            weightGraph.setOnClickListener(v -> showWeightManagementDialog());
        }
    }

    private void observeDashboardData() {
        viewModel.getMacroGoals().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                User user = ((Result.Success<User>) result).getData();
                updateGoals(user);
            }
        });

        updateMacrosForDate(selectedDateMillis);

        viewModel.getWeights().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && weightGraph != null) {
                List<WeightEntry> weights = ((Result.Success<List<WeightEntry>>) result).getData();
                if (weights != null && !weights.isEmpty()) {
                    List<WeightEntry> sorted = new ArrayList<>(weights);
                    Collections.sort(sorted, Comparator.comparingLong(WeightEntry::getDateMillis));
                    float[] weightData = new float[sorted.size()];
                    long[] dateData = new long[sorted.size()];
                    for (int i = 0; i < sorted.size(); i++) {
                        weightData[i] = sorted.get(i).getWeight();
                        dateData[i] = sorted.get(i).getDateMillis();
                    }
                    weightGraph.setData(weightData, dateData);
                }
            }
        });
    }

    private void updateMacrosForDate(long dateMillis) {
        viewModel.getConsumedCalories(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && calorieBar != null) {
                Float calories = ((Result.Success<Float>) result).getData();
                consumedCalories = calories != null ? calories : 0f;
                updateDailyCalorieBar();
            }
        });

        viewModel.getConsumedProteins(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && proteinBar != null) {
                Float proteins = ((Result.Success<Float>) result).getData();
                consumedProteins = proteins != null ? proteins : 0f;
                proteinBar.setProgress(consumedProteins, proteinGoal, "g");
            }
        });

        viewModel.getConsumedCarbs(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && carbsBar != null) {
                Float carbs = ((Result.Success<Float>) result).getData();
                consumedCarbs = carbs != null ? carbs : 0f;
                carbsBar.setProgress(consumedCarbs, carbsGoal, "g");
            }
        });

        viewModel.getConsumedFats(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && fatBar != null) {
                Float fats = ((Result.Success<Float>) result).getData();
                consumedFats = fats != null ? fats : 0f;
                fatBar.setProgress(consumedFats, fatGoal, "g");
            }
        });

        viewModel.getDailyCaloriesConsumed(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && histogram != null) {
                int[] weeklyData = ((Result.Success<int[]>) result).getData();
                histogram.setData(weeklyData);
            }
        });

        viewModel.getCaloriesBurned(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                Integer burned = ((Result.Success<Integer>) result).getData();
                todayCaloriesBurned = burned != null ? burned : 0;
                updateDailyCalorieBar();
            }
        });
    }


    private void updateGoals(User user) {
        if (user == null) {
            return;
        }

        calorieGoal = user.getCalorieGoal() > 0 ? user.getCalorieGoal() : DEFAULT_CALORIE_GOAL;
        proteinGoal = user.getProteinGoal() > 0 ? user.getProteinGoal() : DEFAULT_PROTEIN_GOAL;
        carbsGoal = user.getCarbGoal() > 0 ? user.getCarbGoal() : DEFAULT_CARBS_GOAL;
        fatGoal = user.getFatGoal() > 0 ? user.getFatGoal() : DEFAULT_FAT_GOAL;

        if (calorieBar != null) {
            updateDailyCalorieBar();
        }
        if (proteinBar != null) {
            proteinBar.setProgress(consumedProteins, proteinGoal, "g");
        }
        if (carbsBar != null) {
            carbsBar.setProgress(consumedCarbs, carbsGoal, "g");
        }
        if (fatBar != null) {
            fatBar.setProgress(consumedFats, fatGoal, "g");
        }

        // Re-render the calendar now that we have real goals for progress calculation
        renderSelectedMonth();
    }

    private void updateDailyCalorieBar() {
        if (calorieBar == null) {
            return;
        }
        float netCalories = Math.max(0f, consumedCalories - todayCaloriesBurned);
        calorieBar.setProgress(netCalories, calorieGoal, "kcal");
    }

    private void updateMonthlyCalendar(List<DashboardCalendarDay> data) {
        if (data == null) return;

        // Build progress map for the visible month
        dayProgressMap.clear();
        try {
            String uid = com.example.muzfit.utils.RepositorySupport.currentUidOrDefault();
            com.example.muzfit.utils.RepositorySupport.ensureLocalUser(
                ServiceLocator.getInstance().getDatabase().muzFitDao(), uid
            );
            com.example.muzfit.database.MuzFitDao dao =
                ServiceLocator.getInstance().getDatabase().muzFitDao();
            for (DashboardCalendarDay d : data) {
                if (d.isCurrentMonth()) {
                    long dayStart = getDayStartMillis(selectedYear, selectedMonth, d.getDayNumber());
                    long dayEnd = dayStart + 24L * 60L * 60L * 1000L;
                    float consumed = dao.getConsumedCalories(uid, dayStart, dayEnd);
                    if (consumed > 0f) {
                        float goal = calorieGoal > 0 ? calorieGoal : 2000f;
                        dayProgressMap.put(d.getDayNumber(), consumed / goal);
                    }
                }
            }
        } catch (Exception ignored) {}

        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);
        int todayYear = today.get(Calendar.YEAR);

        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(selectedDateMillis);
        int selDay = selected.get(Calendar.DAY_OF_MONTH);
        int selMonth = selected.get(Calendar.MONTH);
        int selYear = selected.get(Calendar.YEAR);

        setupMonthlyCalendar(
            monthlyCalendarCompose,
            data,
            todayDay, todayMonth, todayYear,
            selDay, selMonth, selYear,
            selectedMonth, selectedYear,
            dayProgressMap,
            dayNum -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(selectedYear, selectedMonth, dayNum);
                selectedDateMillis = calendar.getTimeInMillis();
                updateMacrosForDate(selectedDateMillis);
                // Re-render with same data to reflect new selection
                updateMonthlyCalendar(data);
                return kotlin.Unit.INSTANCE;
            },
            () -> {
                moveMonth(-1);
                return kotlin.Unit.INSTANCE;
            },
            () -> {
                moveMonth(1);
                return kotlin.Unit.INSTANCE;
            }
        );
    }

    private long getDayStartMillis(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void showWeightManagementDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_generic, null);
        // Wire the BlurView for the dialog backdrop (Android 12+).
        setupProfileDialogBlur(dialogView);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        LinearLayout container = dialogView.findViewById(R.id.llInputContainer);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText(R.string.quick_action_weight);

        View inputView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dialog_input, container, false);
        TextInputLayout til = inputView.findViewById(R.id.textInputLayout);
        TextInputEditText tiet = inputView.findViewById(R.id.textInputEditText);
        til.setHint(getString(R.string.weight_hint));
        tiet.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tiet.setText("");
        container.addView(inputView);

        MaterialButton btnHistory = new MaterialButton(requireContext());
        btnHistory.setText(R.string.weight_history_button);
        btnHistory.setTextColor(ContextCompat.getColor(requireContext(), R.color.muz_on_surface_variant));
        btnHistory.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnHistory.setStrokeWidth(0);
        btnHistory.setCornerRadius(30);
        btnHistory.setAllCaps(true);
        btnHistory.setTextSize(12);
        btnHistory.setLetterSpacing(0.05f);
        btnHistory.setPadding(0, 0, 0, 0);
        btnHistory.setMinimumHeight(0);
        btnHistory.setMinHeight(0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) (32 * getResources().getDisplayMetrics().density)
        );
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, (int) (4 * getResources().getDisplayMetrics().density), 0, 0);
        btnHistory.setLayoutParams(layoutParams);

        // Add to the root layout of the dialog, after btnSave.
        // The dialog_profile_generic root is a FrameLayout, not a LinearLayout,
        // so we cast to ViewGroup and use indexOfChild/insertView semantics.
        android.view.ViewGroup rootLayout = (android.view.ViewGroup) dialogView;
        int saveIndex = rootLayout.indexOfChild(btnSave);
        rootLayout.addView(btnHistory, saveIndex + 1);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnSave.setOnClickListener(v -> {
            String weightStr = tiet.getText().toString().trim();
            if (weightStr.isEmpty()) return;

            try {
                float weight = Float.parseFloat(weightStr);
                WeightEntry entry = new WeightEntry();
                entry.setWeight(weight);
                entry.setDateMillis(System.currentTimeMillis());

                IProfileRepository profileRepository = ServiceLocator.getInstance().getProfileRepository();
                profileRepository.addWeightEntry(entry).observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        MuzFitToast.show(requireContext(), R.string.profile_update_success);
                        // The graph will auto-refresh via the observer in observeDashboardData()
                    } else if (result.isError()) {
                        MuzFitToast.showError(requireContext(), ((Result.Error<?>) result).getMessage());
                    }
                });
                dialog.dismiss();
            } catch (NumberFormatException ignored) {}
        });

        btnHistory.setOnClickListener(v -> {
            dialog.dismiss();
            showWeightHistoryListDialog();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showWeightHistoryListDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choose_meal, null);
        ListView listView = dialogView.findViewById(R.id.lvChooseMeal);
        TextView titleView = dialogView.findViewById(R.id.choose_meal_title);
        TextView emptyView = dialogView.findViewById(R.id.tvChooseMealEmpty);

        titleView.setText(R.string.weight_history_button);
        dialogView.findViewById(R.id.tilChooseMealSort).setVisibility(View.GONE);
        // Hide search field by finding its parent TextInputLayout container
        View searchField = dialogView.findViewById(R.id.etChooseMealSearch);
        if (searchField != null && searchField.getParent() != null && searchField.getParent().getParent() instanceof View) {
            ((View) searchField.getParent().getParent()).setVisibility(View.GONE);
        }
        dialogView.findViewById(R.id.btnAddFoodFromPicker).setVisibility(View.GONE);

        List<WeightEntry> weightEntries = new ArrayList<>();
        WeightHistoryAdapter adapter = new WeightHistoryAdapter(requireContext(), weightEntries, entry -> {
            viewModel.deleteWeightEntry(entry).observe(getViewLifecycleOwner(), result -> {
                if (result instanceof Result.Success) {
                    MuzFitToast.show(requireContext(), R.string.weight_removed_toast);
                }
            });
        });
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        viewModel.getWeights().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<WeightEntry> data = ((Result.Success<List<WeightEntry>>) result).getData();
                weightEntries.clear();
                if (data != null && !data.isEmpty()) {
                    weightEntries.addAll(data);
                    Collections.sort(weightEntries, (e1, e2) -> Long.compare(e2.getDateMillis(), e1.getDateMillis()));
                    emptyView.setVisibility(View.GONE);
                } else {
                    emptyView.setText(getString(R.string.weight_history_empty));
                    emptyView.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }
        });

        dialogView.findViewById(R.id.btnCancelChooseMeal).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Wires the dialog_profile_generic BlurView (Android 12+) for the
     * showWeightManagementDialog backdrop.
     */
    private void setupProfileDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.profile_generic_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /**
     * Clips the given BlurView to a 28dp rounded rectangle. The XML
     * `bg_dialog_blur_rounded` background + clipToOutline combo is
     * unreliable for BlurView (which extends ConstraintLayout) on
     * some devices/emulators, so we also set a programmatic outline
     * provider. The two are belt-and-braces and both yield the same
     * 28dp corner radius as the glass card on top.
     */
    private void applyRoundedOutline(BlurView blurView) {
        if (blurView == null) return;
        final float radiusPx = 28f * blurView.getResources().getDisplayMetrics().density;
        blurView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
        blurView.setClipToOutline(true);
    }
}
