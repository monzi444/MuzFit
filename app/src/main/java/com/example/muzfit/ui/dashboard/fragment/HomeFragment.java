package com.example.muzfit.ui.dashboard.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.R;
import com.example.muzfit.model.DashboardCalendarDay;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.ui.dashboard.viewmodel.DashboardViewModel;
import com.example.muzfit.ui.dashboard.viewmodel.DashboardViewModelFactory;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.ServiceLocator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private static final int YEAR_WINDOW_RADIUS = 50;
    private static final float DEFAULT_CALORIE_GOAL = 2000f;
    private static final float DEFAULT_PROTEIN_GOAL = 150f;
    private static final float DEFAULT_CARBS_GOAL = 250f;
    private static final float DEFAULT_FAT_GOAL = 70f;
    private static final int DEFAULT_CALORIES_BURNED_GOAL = 2500;

    private DashboardViewModel viewModel;
    private GridLayout calendarGrid;
    private LinearLayout activityGoalSummaryBar;
    private TextView activityGoalPercent;
    private TextView activityPartialPercent;
    private TextView activityMissedPercent;
    private NutrientProgressBar calorieBar;
    private NutrientProgressBar proteinBar;
    private NutrientProgressBar carbsBar;
    private NutrientProgressBar fatBar;
    private TextView caloriesCount;
    private TextView intakeLabel;
    private ProgressBar caloriesProgress;
    private CalorieHistogramView histogram;
    private WeightGraphView weightGraph;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private int selectedMonth;
    private int selectedYear;
    private int firstSelectableYear;
    private int lastSelectableYear;
    private float consumedCalories;
    private float consumedProteins;
    private float consumedCarbs;
    private float consumedFats;
    private float calorieGoal = DEFAULT_CALORIE_GOAL;
    private float proteinGoal = DEFAULT_PROTEIN_GOAL;
    private float carbsGoal = DEFAULT_CARBS_GOAL;
    private float fatGoal = DEFAULT_FAT_GOAL;
    private int caloriesBurnedGoal = DEFAULT_CALORIES_BURNED_GOAL;
    private int todayCaloriesBurned;
    private boolean isUpdatingCalendarSelection;
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
        calendarGrid = view.findViewById(R.id.calendar_grid);
        activityGoalSummaryBar = view.findViewById(R.id.activity_goal_summary_bar);
        activityGoalPercent = view.findViewById(R.id.activity_goal_percent);
        activityPartialPercent = view.findViewById(R.id.activity_partial_percent);
        activityMissedPercent = view.findViewById(R.id.activity_missed_percent);
        monthSpinner = view.findViewById(R.id.month_spinner);
        yearSpinner = view.findViewById(R.id.year_spinner);
        ImageView previousMonth = view.findViewById(R.id.prev_month);
        ImageView nextMonth = view.findViewById(R.id.next_month);

        Calendar today = Calendar.getInstance();
        selectedMonth = today.get(Calendar.MONTH);
        selectedYear = today.get(Calendar.YEAR);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                MONTH_NAMES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);
        setupYearSpinner(selectedYear);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                if (isUpdatingCalendarSelection) {
                    return;
                }
                selectedMonth = position;
                renderSelectedMonth();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Keep the current month visible.
            }
        });
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                if (isUpdatingCalendarSelection) {
                    return;
                }
                selectedYear = firstSelectableYear + position;
                renderSelectedMonth();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Keep the current year visible.
            }
        });

        previousMonth.setOnClickListener(v -> moveMonth(-1));
        nextMonth.setOnClickListener(v -> moveMonth(1));

        renderSelectedMonth();
    }

    private void setupYearSpinner(int centerYear) {
        firstSelectableYear = centerYear - YEAR_WINDOW_RADIUS;
        lastSelectableYear = centerYear + YEAR_WINDOW_RADIUS;
        String[] years = new String[lastSelectableYear - firstSelectableYear + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf(firstSelectableYear + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                years
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
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
        if (monthSpinner == null || yearSpinner == null || calendarGrid == null) {
            return;
        }

        isUpdatingCalendarSelection = true;
        monthSpinner.setSelection(selectedMonth);
        if (selectedYear < firstSelectableYear || selectedYear > lastSelectableYear) {
            setupYearSpinner(selectedYear);
        }
        yearSpinner.setSelection(selectedYear - firstSelectableYear);
        isUpdatingCalendarSelection = false;
        viewModel.getCalendarData(selectedYear, selectedMonth).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<DashboardCalendarDay> calendarData =
                        ((Result.Success<List<DashboardCalendarDay>>) result).getData();
                setupCalendar(calendarGrid, calendarData);
                setupActivityGoalSummary(calendarData);
            }
        });
    }

    private void setupMacros(View view) {
        calorieBar = view.findViewById(R.id.calorie_progress);
        if (calorieBar != null) {
            calorieBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.calorie_color),
                ContextCompat.getColor(requireContext(), R.color.calorie_overflow)
            );
            calorieBar.setProgress(0, calorieGoal);
        }

        proteinBar = view.findViewById(R.id.protein_progress);
        if (proteinBar != null) {
            proteinBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.protein_color),
                ContextCompat.getColor(requireContext(), R.color.protein_overflow)
            );
            proteinBar.setProgress(0, proteinGoal);
        }

        carbsBar = view.findViewById(R.id.carbs_progress);
        if (carbsBar != null) {
            carbsBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.carbs_color),
                ContextCompat.getColor(requireContext(), R.color.carbs_overflow)
            );
            carbsBar.setProgress(0, carbsGoal);
        }

        fatBar = view.findViewById(R.id.fat_progress);
        if (fatBar != null) {
            fatBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.fat_color),
                ContextCompat.getColor(requireContext(), R.color.fat_overflow)
            );
            fatBar.setProgress(0, fatGoal);
        }
    }

    private void setupCaloriesBurned(View view) {
        caloriesCount = view.findViewById(R.id.today_calories_count);
        intakeLabel = view.findViewById(R.id.intake_label);
        caloriesProgress = view.findViewById(R.id.today_calories_progress);
        histogram = view.findViewById(R.id.weekly_calories_histogram);

        if (caloriesCount != null) {
            caloriesCount.setText("0");
        }
        if (caloriesProgress != null) {
            caloriesProgress.setMax((int) calorieGoal);
            caloriesProgress.setProgress(0);
        }
        if (histogram != null) {
            histogram.setData(new int[7]);
        }
    }

    private void setupWeightGraph(View view) {
        weightGraph = view.findViewById(R.id.weight_graph);
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
                updateIntakeBox(dateMillis);
            }
        });

        viewModel.getConsumedProteins(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && proteinBar != null) {
                Float proteins = ((Result.Success<Float>) result).getData();
                consumedProteins = proteins != null ? proteins : 0f;
                proteinBar.setProgress(consumedProteins, proteinGoal);
            }
        });

        viewModel.getConsumedCarbs(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && carbsBar != null) {
                Float carbs = ((Result.Success<Float>) result).getData();
                consumedCarbs = carbs != null ? carbs : 0f;
                carbsBar.setProgress(consumedCarbs, carbsGoal);
            }
        });

        viewModel.getConsumedFats(dateMillis).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && fatBar != null) {
                Float fats = ((Result.Success<Float>) result).getData();
                consumedFats = fats != null ? fats : 0f;
                fatBar.setProgress(consumedFats, fatGoal);
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

    private void updateIntakeBox(long dateMillis) {
        if (caloriesCount != null) {
            caloriesCount.setText(String.format(Locale.getDefault(), "%,.0f", consumedCalories));
        }
        if (caloriesProgress != null) {
            caloriesProgress.setMax((int) calorieGoal);
            caloriesProgress.setProgress(Math.min((int) consumedCalories, (int) calorieGoal));
        }
        if (intakeLabel != null) {
            Calendar today = Calendar.getInstance();
            Calendar selected = Calendar.getInstance();
            selected.setTimeInMillis(dateMillis);

            if (today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR)) {
                intakeLabel.setText("Oggi");
            } else {
                String monthName = selected.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                intakeLabel.setText(String.format(Locale.getDefault(), "%d %s",
                        selected.get(Calendar.DAY_OF_MONTH), monthName));
            }
        }
    }

    private void updateGoals(User user) {
        if (user == null) {
            return;
        }

        calorieGoal = user.getCalorieGoal() > 0 ? user.getCalorieGoal() : DEFAULT_CALORIE_GOAL;
        proteinGoal = user.getProteinGoal() > 0 ? user.getProteinGoal() : DEFAULT_PROTEIN_GOAL;
        carbsGoal = user.getCarbGoal() > 0 ? user.getCarbGoal() : DEFAULT_CARBS_GOAL;
        fatGoal = user.getFatGoal() > 0 ? user.getFatGoal() : DEFAULT_FAT_GOAL;
        caloriesBurnedGoal = user.getCalorieBurnGoal() > 0
                ? user.getCalorieBurnGoal()
                : DEFAULT_CALORIES_BURNED_GOAL;

        if (calorieBar != null) {
            updateDailyCalorieBar();
        }
        if (proteinBar != null) {
            proteinBar.setProgress(consumedProteins, proteinGoal);
        }
        if (carbsBar != null) {
            carbsBar.setProgress(consumedCarbs, carbsGoal);
        }
        if (fatBar != null) {
            fatBar.setProgress(consumedFats, fatGoal);
        }
        if (caloriesProgress != null) {
            caloriesProgress.setMax((int) calorieGoal);
            caloriesProgress.setProgress(Math.min((int) consumedCalories, (int) calorieGoal));
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
        calorieBar.setProgress(netCalories, calorieGoal);
    }

    private void setupActivityGoalSummary(List<DashboardCalendarDay> calendarData) {
        if (activityGoalSummaryBar == null || activityGoalPercent == null
                || activityPartialPercent == null || activityMissedPercent == null) {
            return;
        }

        int goalCount = 0;
        int partialCount = 0;
        int missedCount = 0;

        for (DashboardCalendarDay day : calendarData) {
            if (!day.isCurrentMonth()) {
                continue;
            }

            switch (day.getLevel()) {
                case GOAL:
                    goalCount++;
                    break;
                case PARTIAL:
                    partialCount++;
                    break;
                case NONE:
                    missedCount++;
                    break;
                case EMPTY:
                default:
                    break;
            }
        }

        int total = goalCount + partialCount + missedCount;
        if (total == 0) {
            activityGoalSummaryBar.setVisibility(View.GONE);
            return;
        }

        activityGoalSummaryBar.setVisibility(View.VISIBLE);
        int goalPercent = Math.round(goalCount * 100f / total);
        int partialPercent = Math.round(partialCount * 100f / total);
        int missedPercent = 100 - goalPercent - partialPercent;

        updateSummarySegment(activityGoalPercent, goalCount, goalPercent, R.color.activity_high);
        updateSummarySegment(activityPartialPercent, partialCount, partialPercent, R.color.activity_medium);
        updateSummarySegment(activityMissedPercent, missedCount, missedPercent, R.color.activity_low);
    }

    private void updateSummarySegment(TextView segment, int count, int percent, int colorResId) {
        if (count == 0) {
            segment.setVisibility(View.GONE);
            return;
        }

        segment.setVisibility(View.VISIBLE);
        segment.setText(String.format(Locale.getDefault(), "%d%%", percent));
        segment.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
        segment.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                count
        ));
    }

    /**
     * Populates the grid with circular day indicators like a calendar.
     */
    private void setupCalendar(GridLayout grid, List<DashboardCalendarDay> data) {
        grid.removeAllViews();
        
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (2 * density);

        int column = 0;
        for (DashboardCalendarDay day : data) {
            TextView dayView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = size;
            params.columnSpec = GridLayout.spec(column, 1f);
            params.setMargins(margin, margin, margin, margin);
            dayView.setLayoutParams(params);
            dayView.setGravity(Gravity.CENTER);
            dayView.setText(String.valueOf(day.getDayNumber()));
            dayView.setTextSize(14);
            dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.muz_on_surface));

            if (day.isCurrentMonth()) {
                dayView.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(selectedYear, selectedMonth, day.getDayNumber());
                    selectedDateMillis = calendar.getTimeInMillis();
                    updateMacrosForDate(selectedDateMillis);
                    
                    Toast.makeText(
                        requireContext(),
                        String.format(Locale.getDefault(), "Data selezionata: %d %s %d", day.getDayNumber(), MONTH_NAMES[selectedMonth], selectedYear),
                        Toast.LENGTH_SHORT
                    ).show();
                });
            }

            switch (day.getLevel()) {
                case GOAL:
                    dayView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle_goal));
                    break;
                case PARTIAL:
                    dayView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle_partial));
                    break;
                case NONE:
                    dayView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle_none));
                    break;
                case EMPTY:
                default:
                    dayView.setBackground(null);
                    break;
            }
            
            grid.addView(dayView);
            column++;
            if (column == 7) {
                column = 0;
            }
        }
    }
}
