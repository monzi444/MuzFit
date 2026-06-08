package com.example.muzfit.ui.dashboard.fragment;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import static com.example.muzfit.ui.dashboard.fragment.MonthlyCalendarViewKt.setupMonthlyCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
            proteinBar.setProgress(0, proteinGoal, "g");
        }

        carbsBar = view.findViewById(R.id.carbs_progress);
        if (carbsBar != null) {
            carbsBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.carbs_color),
                ContextCompat.getColor(requireContext(), R.color.carbs_overflow)
            );
            carbsBar.setProgress(0, carbsGoal, "g");
        }

        fatBar = view.findViewById(R.id.fat_progress);
        if (fatBar != null) {
            fatBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.fat_color),
                ContextCompat.getColor(requireContext(), R.color.fat_overflow)
            );
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
            weightGraph.setOnClickListener(v -> showWeightHistoryDialog());
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
                weightGraph.setData(toWeightData(weights));
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
    }

    private float[] toWeightData(List<WeightEntry> weights) {
        if (weights == null || weights.isEmpty()) {
            return new float[0];
        }

        List<WeightEntry> sorted = new ArrayList<>(weights);
        Collections.sort(sorted, Comparator.comparingLong(WeightEntry::getDateMillis));

        float[] data = new float[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            data[i] = sorted.get(i).getWeight();
        }
        return data;
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

    private void showWeightHistoryDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_generic, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        LinearLayout container = dialogView.findViewById(R.id.llInputContainer);

        tvTitle.setText(R.string.quick_action_weight);

        View inputView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dialog_input, container, false);
        TextInputLayout til = inputView.findViewById(R.id.textInputLayout);
        TextInputEditText tiet = inputView.findViewById(R.id.textInputEditText);
        til.setHint(getString(R.string.weight_hint));
        tiet.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tiet.setText("");
        container.addView(inputView);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
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
                        // Refresh the weight graph data
                        viewModel.getWeights().observe(getViewLifecycleOwner(), weightResult -> {
                            if (weightResult.isSuccess() && weightGraph != null) {
                                List<WeightEntry> weights = ((Result.Success<List<WeightEntry>>) weightResult).getData();
                                weightGraph.setData(toWeightData(weights));
                            }
                        });
                    } else if (result.isError()) {
                        MuzFitToast.showError(requireContext(), ((Result.Error<?>) result).getMessage());
                    }
                });
                dialog.dismiss();
            } catch (NumberFormatException ignored) {}
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
