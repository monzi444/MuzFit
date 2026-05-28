package com.example.muzfit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    /**
     * Represents the status of daily activity for the heatmap and pie chart.
     * NONE: No activity (Red)
     * PARTIAL: Activity performed but goal not reached (Yellow)
     * GOAL: Goal reached (Green)
     */
    public enum ActivityLevel {
        NONE,
        PARTIAL,
        GOAL
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setupMacros(view);

        // Get activity data for the last 2 weeks (14 days)
        List<ActivityLevel> activityData = getMockActivityData();

        GridLayout heatmapGrid = view.findViewById(R.id.heatmap_grid);
        if (heatmapGrid != null) {
            setupHeatmap(heatmapGrid, activityData);
        }

        PieChartView pieChart = view.findViewById(R.id.activity_pie_chart);
        if (pieChart != null) {
            pieChart.setData(activityData);
        }

        setupCalories(view);

        return view;
    }

    /**
     * Generates dummy data for the last 2 weeks (14 days).
     * In the future, this can be replaced with real data fetching logic.
     */
    private List<ActivityLevel> getMockActivityData() {
        List<ActivityLevel> data = new ArrayList<>();
        Random random = new Random();
        ActivityLevel[] levels = ActivityLevel.values();
        // Generate 14 days of data
        for (int i = 0; i < 14; i++) {
            data.add(levels[random.nextInt(levels.length)]);
        }
        return data;
    }

    private void setupMacros(View view) {
        NutrientProgressBar calorieBar = view.findViewById(R.id.calorie_progress);
        if (calorieBar != null) {
            calorieBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.calorie_color),
                ContextCompat.getColor(requireContext(), R.color.calorie_overflow)
            );
            // Simulate exceeding the target (e.g., 2200 kcal on 2000 target)
            calorieBar.setProgress(2200, 2000);
        }

        NutrientProgressBar proteinBar = view.findViewById(R.id.protein_progress);
        if (proteinBar != null) {
            proteinBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.protein_color),
                ContextCompat.getColor(requireContext(), R.color.protein_overflow)
            );
            proteinBar.setProgress(120, 150);
        }

        NutrientProgressBar carbsBar = view.findViewById(R.id.carbs_progress);
        if (carbsBar != null) {
            carbsBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.carbs_color),
                ContextCompat.getColor(requireContext(), R.color.carbs_overflow)
            );
            // Simulate overflow (e.g., 300g on 250g)
            carbsBar.setProgress(300, 250);
        }

        NutrientProgressBar fatBar = view.findViewById(R.id.fat_progress);
        if (fatBar != null) {
            fatBar.setColors(
                ContextCompat.getColor(requireContext(), R.color.fat_color),
                ContextCompat.getColor(requireContext(), R.color.fat_overflow)
            );
            fatBar.setProgress(63, 70);
        }
    }

    /**
     * Configures today's calorie total and the weekly histogram.
     */
    private void setupCalories(View view) {
        // Today's calories setup
        TextView caloriesCount = view.findViewById(R.id.today_calories_count);
        ProgressBar caloriesProgress = view.findViewById(R.id.today_calories_progress);
        
        if (caloriesCount != null) {
            caloriesCount.setText("1,842");
        }
        if (caloriesProgress != null) {
            caloriesProgress.setMax(2500); // Daily calorie goal
            caloriesProgress.setProgress(1842);
        }

        // Weekly histogram setup
        CalorieHistogramView histogram = view.findViewById(R.id.weekly_calories_histogram);
        if (histogram != null) {
            int[] weeklyData = {420, 720, 580, 1020, 850, 600, 910};
            histogram.setData(weeklyData);
        }
    }

    /**
     * Populates the heatmap grid with colored squares based on the activity level.
     * @param grid The grid to populate
     * @param data List of activity levels (e.g., last 14 days)
     */
    private void setupHeatmap(GridLayout grid, List<ActivityLevel> data) {
        grid.removeAllViews();
        
        // Set 7 columns to represent the days of the week (2 weeks = 2 rows)
        grid.setColumnCount(7);

        // Keep the heatmap compact so the pie chart stays inside the screen.
        int size = (int) (28 * getResources().getDisplayMetrics().density);
        int margin = (int) (1 * getResources().getDisplayMetrics().density);

        for (ActivityLevel level : data) {
            View square = new View(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            square.setLayoutParams(params);

            int colorResId;
            switch (level) {
                case GOAL:
                    colorResId = R.color.activity_high; // Green
                    break;
                case PARTIAL:
                    colorResId = R.color.activity_medium; // Yellow
                    break;
                case NONE:
                default:
                    colorResId = R.color.activity_low; // Red
                    break;
            }
            
            square.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
            grid.addView(square);
        }
    }
}
