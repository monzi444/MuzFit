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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

    private DashboardViewModel viewModel;
    private GridLayout calendarGrid;
    private NutrientProgressBar calorieBar;
    private NutrientProgressBar proteinBar;
    private NutrientProgressBar carbsBar;
    private NutrientProgressBar fatBar;
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

    /**
     * Populates the grid with circular day indicators like a calendar.
     */
    private void setupCalendar(GridLayout grid, List<DashboardCalendarDay> data) {
        grid.removeAllViews();
        
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);
        int todayYear = today.get(Calendar.YEAR);

        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(selectedDateMillis);
        int selDay = selected.get(Calendar.DAY_OF_MONTH);
        int selMonth = selected.get(Calendar.MONTH);
        int selYear = selected.get(Calendar.YEAR);

        float density = getResources().getDisplayMetrics().density;
        int size = (int) (38 * density);
        int margin = (int) (2 * density);

        int column = 0;
        for (DashboardCalendarDay day : data) {
            TextView dayView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.columnSpec = GridLayout.spec(column, GridLayout.CENTER, 1f);
            params.setMargins(margin, margin, margin, margin);
            dayView.setLayoutParams(params);
            dayView.setGravity(Gravity.CENTER);
            dayView.setText(String.valueOf(day.getDayNumber()));
            dayView.setTextSize(14);

            boolean isActuallyToday = day.isCurrentMonth() && day.getDayNumber() == todayDay 
                    && selectedMonth == todayMonth && selectedYear == todayYear;
            
            boolean isActuallySelected = day.isCurrentMonth() && day.getDayNumber() == selDay 
                    && selectedMonth == selMonth && selectedYear == selYear;

            if (day.isCurrentMonth()) {
                applyDayCellStyle(dayView, day, isActuallySelected, isActuallyToday);

                dayView.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(selectedYear, selectedMonth, day.getDayNumber());
                    selectedDateMillis = calendar.getTimeInMillis();
                    updateMacrosForDate(selectedDateMillis);
                    
                    // Re-render to update highlights
                    setupCalendar(grid, data);
                });
            } else {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey));
                dayView.setBackground(null);
            }
            
            grid.addView(dayView);
            column++;
            if (column == 7) column = 0;
        }
    }

    private void applyDayCellStyle(
            TextView dayView,
            DashboardCalendarDay day,
            boolean isSelected,
            boolean isToday
    ) {
        float density = getResources().getDisplayMetrics().density;
        int backgroundInset = (int) (3 * density);
        int ringInset = (int) (1 * density);

        Drawable goalBackground = getDayLevelBackground(day.getLevel());
        if (goalBackground != null) {
            goalBackground = new InsetDrawable(goalBackground, backgroundInset);
        }

        Drawable selectedRing = null;
        if (isSelected) {
            int ringRes;
            switch (day.getLevel()) {
                case GOAL:
                    ringRes = R.drawable.calendar_circle_selected_green;
                    break;
                case OVERFLOW:
                    ringRes = R.drawable.calendar_circle_selected_orange;
                    break;
                case NONE:
                default:
                    ringRes = R.drawable.calendar_circle_selected_white;
                    break;
            }
            selectedRing = ContextCompat.getDrawable(requireContext(), ringRes);
            if (selectedRing != null) {
                selectedRing = new InsetDrawable(selectedRing, ringInset);
            }
        }

        if (goalBackground != null && selectedRing != null) {
            dayView.setBackground(new LayerDrawable(new Drawable[]{goalBackground, selectedRing}));
        } else if (selectedRing != null) {
            dayView.setBackground(selectedRing);
        } else if (goalBackground != null) {
            dayView.setBackground(goalBackground);
        } else {
            dayView.setBackground(null);
        }

        int defaultTextColor = ContextCompat.getColor(requireContext(), R.color.muz_on_surface);
        if (isToday) {
            dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.muz_primary_lime));
            dayView.setTypeface(null, android.graphics.Typeface.NORMAL);
            dayView.setTextSize(14);
        } else {
            dayView.setTextColor(defaultTextColor);
            dayView.setTypeface(null, android.graphics.Typeface.NORMAL);
            dayView.setTextSize(14);
        }
    }

    @Nullable
    private Drawable getDayLevelBackground(DashboardCalendarDay.ActivityLevel level) {
        int drawableRes;
        switch (level) {
            case GOAL:
                drawableRes = R.drawable.calendar_circle_goal;
                break;
            case OVERFLOW:
                drawableRes = R.drawable.calendar_circle_overflow;
                break;
            case PARTIAL:
                drawableRes = R.drawable.calendar_circle_partial;
                break;
            case NONE:
            default:
                return null;
        }
        return ContextCompat.getDrawable(requireContext(), drawableRes);
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
