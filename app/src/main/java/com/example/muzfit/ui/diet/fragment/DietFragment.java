package com.example.muzfit.ui.diet.fragment;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.model.Food;
import com.example.muzfit.R;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.ui.diet.viewmodel.DietViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DietFragment extends Fragment {

    private DietViewModel viewModel;
    private TextView tvMonthYear;
    private GridLayout calendarGrid;
    private TextView tvTotalCalories, tvTotalCarbs, tvTotalProtein, tvTotalFat;
    private LinearLayout containerColazione, containerPranzo, containerCena;
    private Calendar currentWeekStart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        calendarGrid = view.findViewById(R.id.calendarGrid);
        
        containerColazione = view.findViewById(R.id.containerColazione);
        containerPranzo = view.findViewById(R.id.containerPranzo);
        containerCena = view.findViewById(R.id.containerCena);
        
        Button addFoodButton = view.findViewById(R.id.addFoodButton);
        ImageView btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
        ImageView btnNextWeek = view.findViewById(R.id.btnNextWeek);

        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalCarbs = view.findViewById(R.id.tvTotalCarbs);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        tvTotalFat = view.findViewById(R.id.tvTotalFat);

        IDietRepository repository = ServiceLocator.getInstance().getDietRepository();
        viewModel = new ViewModelProvider(this, new DietViewModelFactory(repository)).get(DietViewModel.class);

        // Initialize to Monday of the current week
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int offsetToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        currentWeekStart.add(Calendar.DAY_OF_YEAR, offsetToMonday);

        tvGreeting.setText(getString(R.string.greeting_prefix, "Utente"));

        setupCalendar();

        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            setupCalendar();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            setupCalendar();
        });

        // Adapting legacy Food list logic for now. 
        // Note: Real DietViewModel uses Result<List<Meal>>
        // This is a bridge until the UI is fully converted to Meal/Result pattern
        viewModel.getMeals().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                // Mapping Meal (from repository) to Food (used by existing UI logic)
                List<Food> foods = new ArrayList<>();
                // result.getData() returns List<Meal>
                // For simplicity during migration, we'll keep the UI logic but wrap it
                updateTotalsFromMeals(((com.example.muzfit.model.Result.Success<List<com.example.muzfit.model.Meal>>) result).getData());
                populateFoodContainersFromMeals(((com.example.muzfit.model.Result.Success<List<com.example.muzfit.model.Meal>>) result).getData());
            }
        });

        addFoodButton.setOnClickListener(v -> showAddFoodDialog());

        return view;
    }

    private void updateTotalsFromMeals(List<com.example.muzfit.model.Meal> mealList) {
        float totalKcal = 0;
        float totalCarbs = 0;
        float totalProtein = 0;
        float totalFat = 0;

        if (mealList != null) {
            for (com.example.muzfit.model.Meal meal : mealList) {
                totalKcal += meal.getCalories();
                totalCarbs += meal.getCarbs();
                totalProtein += meal.getProtein();
                totalFat += meal.getFat();
            }
        }

        tvTotalCalories.setText(String.valueOf((int)totalKcal));
        tvTotalCarbs.setText(String.format(Locale.getDefault(), "%.0fg", totalCarbs));
        tvTotalProtein.setText(String.format(Locale.getDefault(), "%.0fg", totalProtein));
        tvTotalFat.setText(String.format(Locale.getDefault(), "%.0fg", totalFat));
    }

    private void populateFoodContainersFromMeals(List<com.example.muzfit.model.Meal> mealList) {
        containerColazione.removeAllViews();
        containerPranzo.removeAllViews();
        containerCena.removeAllViews();

        if (mealList == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (com.example.muzfit.model.Meal meal : mealList) {
            View itemView = inflater.inflate(R.layout.list_item_food, null);
            TextView nameTv = itemView.findViewById(R.id.foodNameTextView);
            ImageButton deleteBtn = itemView.findViewById(R.id.deleteFoodButton);

            nameTv.setText(meal.getFoodName());
            
            deleteBtn.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Elimina: " + meal.getFoodName(), Toast.LENGTH_SHORT).show();
            });

            // Mocking category since Meal doesn't have it in the snippet
            containerPranzo.addView(itemView);
        }
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        int todayDayOfYear = today.get(Calendar.DAY_OF_YEAR);
        int todayYear = today.get(Calendar.YEAR);

        String monthName = currentWeekStart.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        int year = currentWeekStart.get(Calendar.YEAR);
        tvMonthYear.setText(String.format(Locale.getDefault(), "%s %d", monthName, year));

        calendarGrid.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density);
        int margin = (int) (2 * density);

        Calendar tempCalendar = (Calendar) currentWeekStart.clone();

        for (int i = 0; i < 7; i++) {
            final int dayNum = tempCalendar.get(Calendar.DAY_OF_MONTH);
            final int monthNum = tempCalendar.get(Calendar.MONTH);
            final int yearNum = tempCalendar.get(Calendar.YEAR);
            final int dayOfYear = tempCalendar.get(Calendar.DAY_OF_YEAR);

            TextView dayView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = size;
            params.columnSpec = GridLayout.spec(i, 1f);
            params.setMargins(margin, margin, margin, margin);
            dayView.setLayoutParams(params);
            dayView.setGravity(Gravity.CENTER);
            dayView.setText(String.valueOf(dayNum));
            dayView.setTextSize(14);

            if (yearNum == todayYear && dayOfYear == todayDayOfYear) {
                dayView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle_goal));
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                dayView.setTypeface(null, Typeface.BOLD);
            } else {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                if (tempCalendar.after(today)) {
                    dayView.setAlpha(0.5f);
                }
            }

            dayView.setOnClickListener(v -> {
                String selectedDate = String.format(Locale.getDefault(), "%d/%d/%d", dayNum, monthNum + 1, yearNum);
                Toast.makeText(getContext(), "Data: " + selectedDate, Toast.LENGTH_SHORT).show();
            });

            calendarGrid.addView(dayView);
            tempCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        EditText editTextFoodName = dialogView.findViewById(R.id.editTextFoodName);
        EditText editTextCalories = dialogView.findViewById(R.id.editTextCalories);
        EditText editTextCarbs = dialogView.findViewById(R.id.editTextCarbs);
        EditText editTextProtein = dialogView.findViewById(R.id.editTextProtein);
        EditText editTextFat = dialogView.findViewById(R.id.editTextFat);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        ArrayAdapter<Food.Category> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Food.Category.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        builder.setTitle("Aggiungi Cibo")
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    String name = editTextFoodName.getText().toString();
                    String caloriesStr = editTextCalories.getText().toString();
                    String carbsStr = editTextCarbs.getText().toString();
                    String proteinStr = editTextProtein.getText().toString();
                    String fatStr = editTextFat.getText().toString();
                    Food.Category category = (Food.Category) spinnerCategory.getSelectedItem();

                    if (!name.isEmpty() && !caloriesStr.isEmpty()) {
                        int calories = Integer.parseInt(caloriesStr);
                        int carbs = carbsStr.isEmpty() ? 0 : Integer.parseInt(carbsStr);
                        int protein = proteinStr.isEmpty() ? 0 : Integer.parseInt(proteinStr);
                        int fat = fatStr.isEmpty() ? 0 : Integer.parseInt(fatStr);

                        com.example.muzfit.model.Meal newMeal = new com.example.muzfit.model.Meal(0, name, (float)calories, (float)carbs, (float)protein, (float)fat);
                        viewModel.addMeal(newMeal);
                        Toast.makeText(getContext(), "Cibo aggiunto!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Inserisci nome e calorie", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }
}
