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
import com.example.muzfit.model.Result;
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
    private static final int CALORIE_GOAL = 2000;
    private static final int PROTEIN_GOAL = 150;
    private static final int CARBS_GOAL = 250;
    private static final int FAT_GOAL = 70;
    private static final int CALORIES_BURNED_GOAL = 2500;

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
    private ProgressBar caloriesProgress;
    private CalorieHistogramView histogram;
    private WeightGraphView weightGraph;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private int selectedMonth;
    private int selectedYear;
    private int firstSelectableYear;
    private int lastSelectableYear;
    private boolean isUpdatingCalendarSelection;

    /**
     * Represents the status of daily activity for the calendar.
     * NONE: Goal not reached (Red border)
     * PARTIAL: Goal partially reached (Orange border)
     * GOAL: Goal fully reached (Green border)
     * EMPTY: No activity tracked
     */
    public enum ActivityLevel {
        NONE,
        PARTIAL,
        GOAL,
        EMPTY
    }

    private static class CalendarDay {
        int dayNumber;
        ActivityLevel level;
        boolean isCurrentMonth;

        CalendarDay(int dayNumber, ActivityLevel level, boolean isCurrentMonth) {
            this.dayNumber = dayNumber;
            this.level = level;
            this.isCurrentMonth = isCurrentMonth;
        }
    }

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
        List<CalendarDay> calendarData = getCalendarData(selectedYear, selectedMonth);
        setupCalendar(calendarGrid, calendarData);
        setupActivityGoalSummary(calendarData);
    }

    private List<CalendarDay> getCalendarData(int year, int month) {
        List<CalendarDay> data = new ArrayList<>();

        Calendar firstDay = Calendar.getInstance();
        firstDay.set(year, month, 1);
        int firstDayOffset = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar previousMonth = (Calendar) firstDay.clone();
        previousMonth.add(Calendar.MONTH, -1);
        int daysInPreviousMonth = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = firstDayOffset - 1; i >= 0; i--) {
            data.add(new CalendarDay(daysInPreviousMonth - i, ActivityLevel.EMPTY, false));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            data.add(new CalendarDay(day, getMockActivityLevel(year, month, day), true));
        }

        int nextMonthDay = 1;
        while (data.size() % 7 != 0) {
            data.add(new CalendarDay(nextMonthDay, ActivityLevel.EMPTY, false));
            nextMonthDay++;
        }

        return data;
    }

    private ActivityLevel getMockActivityLevel(int year, int month, int day) {
        if (isAfterToday(year, month, day)) {
            return ActivityLevel.EMPTY;
        }

        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        int seed = Math.abs((year * 31 + month * 17 + day * 13) % 10);
        if (seed <= 5) {
            return ActivityLevel.GOAL;
        }
        if (seed <= 7) {
            return ActivityLevel.PARTIAL;
        }
        return ActivityLevel.NONE;
    }

    private boolean isAfterToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        if (year != today.get(Calendar.YEAR)) {
            return year > today.get(Calendar.YEAR);
        }
        if (month != today.get(Calendar.MONTH)) {
            return month > today.get(Calendar.MONTH);
        }
        return day > today.get(Calendar.DAY_OF_MONTH);
    }

    private void setupMacros(View view) {
        calorieBar = view.findViewById(R.id.calorie_progress);
        if (calorieBar != null) {
            calorieBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.calorie_color),
                ContextCompat.getColor(requireContext(), R.color.calorie_overflow)
            );
            calorieBar.setProgress(0, CALORIE_GOAL);
        }

        proteinBar = view.findViewById(R.id.protein_progress);
        if (proteinBar != null) {
            proteinBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.protein_color),
                ContextCompat.getColor(requireContext(), R.color.protein_overflow)
            );
            proteinBar.setProgress(0, PROTEIN_GOAL);
        }

        carbsBar = view.findViewById(R.id.carbs_progress);
        if (carbsBar != null) {
            carbsBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.carbs_color),
                ContextCompat.getColor(requireContext(), R.color.carbs_overflow)
            );
            carbsBar.setProgress(0, CARBS_GOAL);
        }

        fatBar = view.findViewById(R.id.fat_progress);
        if (fatBar != null) {
            fatBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.fat_color),
                ContextCompat.getColor(requireContext(), R.color.fat_overflow)
            );
            fatBar.setProgress(0, FAT_GOAL);
        }
    }

    private void setupCaloriesBurned(View view) {
        caloriesCount = view.findViewById(R.id.today_calories_count);
        caloriesProgress = view.findViewById(R.id.today_calories_progress);
        histogram = view.findViewById(R.id.weekly_calories_histogram);

        if (caloriesCount != null) {
            caloriesCount.setText("0");
        }
        if (caloriesProgress != null) {
            caloriesProgress.setMax(CALORIES_BURNED_GOAL);
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
        viewModel.getCosumedCalories().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && calorieBar != null) {
                Float calories = ((Result.Success<Float>) result).getData();
                calorieBar.setProgress(calories != null ? calories : 0f, CALORIE_GOAL);
            }
        });

        viewModel.getConsumedProteins().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && proteinBar != null) {
                Float proteins = ((Result.Success<Float>) result).getData();
                proteinBar.setProgress(proteins != null ? proteins : 0f, PROTEIN_GOAL);
            }
        });

        viewModel.getConsumedCarbs().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && carbsBar != null) {
                Float carbs = ((Result.Success<Float>) result).getData();
                carbsBar.setProgress(carbs != null ? carbs : 0f, CARBS_GOAL);
            }
        });

        viewModel.getConsumedFats().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && fatBar != null) {
                Float fats = ((Result.Success<Float>) result).getData();
                fatBar.setProgress(fats != null ? fats : 0f, FAT_GOAL);
            }
        });

        viewModel.getWeights(Constants.DEFAULT_USERNAME).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success && weightGraph != null) {
                List<WeightEntry> weights = ((Result.Success<List<WeightEntry>>) result).getData();
                weightGraph.setData(toWeightData(weights));
            }
        });

        viewModel.getDailyCaloriesBurned().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                int[] weeklyData = ((Result.Success<int[]>) result).getData();
                updateCaloriesBurned(weeklyData);
            }
        });
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

    private void updateCaloriesBurned(int[] weeklyData) {
        if (weeklyData == null || weeklyData.length == 0) {
            return;
        }

        int todayCalories = weeklyData[weeklyData.length - 1];
        if (caloriesCount != null) {
            caloriesCount.setText(String.format(Locale.getDefault(), "%,d", todayCalories));
        }
        if (caloriesProgress != null) {
            caloriesProgress.setProgress(Math.min(todayCalories, CALORIES_BURNED_GOAL));
        }
        if (histogram != null) {
            histogram.setData(weeklyData);
        }
    }

    private void setupActivityGoalSummary(List<CalendarDay> calendarData) {
        if (activityGoalSummaryBar == null || activityGoalPercent == null
                || activityPartialPercent == null || activityMissedPercent == null) {
            return;
        }

        int goalCount = 0;
        int partialCount = 0;
        int missedCount = 0;

        for (CalendarDay day : calendarData) {
            if (!day.isCurrentMonth) {
                continue;
            }

            switch (day.level) {
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
    private void setupCalendar(GridLayout grid, List<CalendarDay> data) {
        grid.removeAllViews();
        
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (4 * density);

        for (CalendarDay day : data) {
            TextView dayView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            dayView.setLayoutParams(params);
            dayView.setGravity(Gravity.CENTER);
            dayView.setText(String.valueOf(day.dayNumber));
            dayView.setTextSize(14);

            if (day.isCurrentMonth) {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                dayView.setOnClickListener(v -> Toast.makeText(
                        requireContext(),
                        String.format(Locale.getDefault(), "%d %s %d", day.dayNumber, MONTH_NAMES[selectedMonth], selectedYear),
                        Toast.LENGTH_SHORT
                ).show());
            } else {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
            }

            switch (day.level) {
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
        }
    }
}
