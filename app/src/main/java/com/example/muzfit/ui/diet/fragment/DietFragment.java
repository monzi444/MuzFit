package com.example.muzfit.ui.diet.fragment;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.adapter.FoodSearchAdapter;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsMapper;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.ui.diet.viewmodel.DietViewModelFactory;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModel;
import com.example.muzfit.ui.profile.viewmodel.ProfileViewModelFactory;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.ServiceLocator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DietFragment extends Fragment {

    private DietViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private TextView tvMonthYear, tvCaloriesRemaining;
    private GridLayout calendarGrid;
    private TextView tvTotalCalories, tvTotalCarbs, tvTotalProtein, tvTotalFat;
    private LinearLayout containerColazione, containerPranzo, containerCena;
    private Calendar currentWeekStart;
    private int calorieGoal = 0;
    private float caloriesAssumed = 0;
    private List<UserMeal> lastUserMeals = new ArrayList<>();
    @Nullable
    private AlertDialog chooseMealDialog;
    @Nullable
    private AlertDialog addFoodDialog;

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
        
        Button chooseMealButton = view.findViewById(R.id.chooseMealButton);
        ImageView btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
        ImageView btnNextWeek = view.findViewById(R.id.btnNextWeek);

        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalCarbs = view.findViewById(R.id.tvTotalCarbs);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        tvTotalFat = view.findViewById(R.id.tvTotalFat);
        tvCaloriesRemaining = view.findViewById(R.id.tvCaloriesRemaining);

        IDietRepository repository = ServiceLocator.getInstance().getDietRepository();
        viewModel = new ViewModelProvider(requireActivity(), new DietViewModelFactory(repository)).get(DietViewModel.class);

        IProfileRepository profileRepository = ServiceLocator.getInstance().getProfileRepository();
        profileViewModel = new ViewModelProvider(this, new ProfileViewModelFactory(profileRepository)).get(ProfileViewModel.class);

        currentWeekStart = Calendar.getInstance();
        currentWeekStart.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int offsetToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        currentWeekStart.add(Calendar.DAY_OF_YEAR, offsetToMonday);

        setupCalendar();

        profileViewModel.getDefaultUser().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                User user = ((com.example.muzfit.model.Result.Success<User>) result).getData();
                calorieGoal = user.getCalorieGoal();
                tvGreeting.setText(getString(R.string.greeting_prefix, user.getName()));
                updateRemainingCalories();
            }
        });

        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            setupCalendar();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            setupCalendar();
        });

        viewModel.getMealCatalog().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                viewModel.updateMealsById(((com.example.muzfit.model.Result.Success<List<Meal>>) result).getData());
                refreshDietUi();
            }
        });

        viewModel.getUserMealsForSelectedDay().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                List<UserMeal> userMeals = ((com.example.muzfit.model.Result.Success<List<UserMeal>>) result).getData();
                lastUserMeals = userMeals != null ? userMeals : new ArrayList<>();
                refreshDietUi();
            }
        });

        viewModel.getSelectedDateMillis().observe(getViewLifecycleOwner(), date -> {
            setupCalendar();
        });

        chooseMealButton.setOnClickListener(v -> showChooseMealDialog());

        return view;
    }

    private void refreshDietUi() {
        updateTotalsFromUserMeals(lastUserMeals);
        populateFoodContainersFromUserMeals(lastUserMeals);
    }

    private void updateTotalsFromUserMeals(List<UserMeal> userMeals) {
        float totalKcal = 0, totalCarbs = 0, totalProtein = 0, totalFat = 0;
        if (userMeals != null) {
            for (UserMeal userMeal : userMeals) {
                Meal meal = viewModel.getMealFor(userMeal);
                if (meal == null) {
                    continue;
                }
                totalKcal += meal.getCalories();
                totalCarbs += meal.getCarbs();
                totalProtein += meal.getProtein();
                totalFat += meal.getFat();
            }
        }
        tvTotalCalories.setText(String.valueOf((int)totalKcal));
        caloriesAssumed = totalKcal;
        updateRemainingCalories();
        tvTotalCarbs.setText(String.format(Locale.getDefault(), "%.0fg", totalCarbs));
        tvTotalProtein.setText(String.format(Locale.getDefault(), "%.0fg", totalProtein));
        tvTotalFat.setText(String.format(Locale.getDefault(), "%.0fg", totalFat));
    }

    private void updateRemainingCalories() {
        if (tvCaloriesRemaining != null) {
            int remaining = calorieGoal - (int) caloriesAssumed;
            tvCaloriesRemaining.setText(String.valueOf(remaining));
            tvCaloriesRemaining.setTextColor(ContextCompat.getColor(requireContext(), remaining < 0 ? R.color.fat_color : R.color.muz_primary_lime));
        }
    }

    private void populateFoodContainersFromUserMeals(List<UserMeal> userMeals) {
        containerColazione.removeAllViews();
        containerPranzo.removeAllViews();
        containerCena.removeAllViews();
        
        int countColazione = 0, countPranzo = 0, countCena = 0;
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        if (userMeals != null) {
            for (UserMeal userMeal : userMeals) {
                Meal meal = viewModel.getMealFor(userMeal);
                if (meal == null) {
                    continue;
                }
                View itemView = inflater.inflate(R.layout.list_item_food, null);
                TextView nameTv = itemView.findViewById(R.id.foodNameTextView);
                ImageButton deleteBtn = itemView.findViewById(R.id.deleteFoodButton);
                nameTv.setText(String.format(Locale.getDefault(), "%s (%.0f kcal)", meal.getFoodName(), meal.getCalories()));
                deleteBtn.setOnClickListener(v -> viewModel.deleteLoggedMeal(userMeal)
                        .observe(getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                Toast.makeText(requireContext(), "Togliere il cibo dal pasto!", Toast.LENGTH_SHORT).show();
                            } else if (result.isError()) {
                                Toast.makeText(
                                        requireContext(),
                                        ((com.example.muzfit.model.Result.Error<?>) result).getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }));

                if (userMeal.getCategory() == MealCategory.COLAZIONE) {
                    containerColazione.addView(itemView);
                    countColazione++;
                } else if (userMeal.getCategory() == MealCategory.PRANZO) {
                    containerPranzo.addView(itemView);
                    countPranzo++;
                } else if (userMeal.getCategory() == MealCategory.CENA) {
                    containerCena.addView(itemView);
                    countCena++;
                }
            }
        }

        if (countColazione == 0) addEmptyStateText(containerColazione);
        if (countPranzo == 0) addEmptyStateText(containerPranzo);
        if (countCena == 0) addEmptyStateText(containerCena);
    }

    private void addEmptyStateText(LinearLayout container) {
        TextView emptyTv = new TextView(requireContext());
        emptyTv.setText("Nessun pasto registrato per questa data");
        emptyTv.setPadding(16, 8, 16, 24);
        emptyTv.setAlpha(0.6f);
        emptyTv.setTypeface(null, Typeface.ITALIC);
        container.addView(emptyTv);
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        int todayDayOfYear = today.get(Calendar.DAY_OF_YEAR), todayYear = today.get(Calendar.YEAR);
        
        Long selectedDateMillis = viewModel.getSelectedDateMillis().getValue();
        Calendar selectedDate = Calendar.getInstance();
        if (selectedDateMillis != null) {
            selectedDate.setTimeInMillis(selectedDateMillis);
        }
        int selectedDayOfYear = selectedDate.get(Calendar.DAY_OF_YEAR);
        int selectedYear = selectedDate.get(Calendar.YEAR);

        tvMonthYear.setText(String.format(Locale.getDefault(), "%s %d", currentWeekStart.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), currentWeekStart.get(Calendar.YEAR)));
        calendarGrid.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (40 * density), margin = (int) (2 * density);
        Calendar tempCalendar = (Calendar) currentWeekStart.clone();
        for (int i = 0; i < 7; i++) {
            final long timeMillis = tempCalendar.getTimeInMillis();
            final int dayNum = tempCalendar.get(Calendar.DAY_OF_MONTH), monthNum = tempCalendar.get(Calendar.MONTH), yearNum = tempCalendar.get(Calendar.YEAR), dayOfYear = tempCalendar.get(Calendar.DAY_OF_YEAR);
            TextView dayView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; params.height = size; params.columnSpec = GridLayout.spec(i, 1f); params.setMargins(margin, margin, margin, margin);
            dayView.setLayoutParams(params); dayView.setGravity(Gravity.CENTER); dayView.setText(String.valueOf(dayNum)); dayView.setTextSize(14);
            
            if (yearNum == selectedYear && dayOfYear == selectedDayOfYear) {
                dayView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle_goal));
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)); dayView.setTypeface(null, Typeface.BOLD);
            } else if (yearNum == todayYear && dayOfYear == todayDayOfYear) {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.muz_primary_lime));
                dayView.setTypeface(null, Typeface.BOLD);
            } else {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                if (tempCalendar.after(today)) dayView.setAlpha(0.5f);
            }
            dayView.setOnClickListener(v -> {
                viewModel.setSelectedDate(timeMillis);
            });
            calendarGrid.addView(dayView); tempCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void showChooseMealDialog() {
        List<Meal> availableMeals = new ArrayList<>();
        availableMeals.add(new Meal(0, "Mela", 95, 25, 1, 0));
        availableMeals.add(new Meal(0, "Pasta al pomodoro", 350, 70, 10, 5));
        availableMeals.add(new Meal(0, "Petto di Pollo", 165, 0, 31, 4));

        Map<Integer, Meal> catalog = viewModel.getMealsById().getValue();
        if (catalog != null) {
            availableMeals.addAll(catalog.values());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Scegli un pasto rapido");

        ListView listView = new ListView(requireContext());

        // We create the dialog first so we can dismiss it from inside the adapter
        chooseMealDialog = builder
                .setView(listView)
                .setPositiveButton("Add food", (d, id) -> {
                    dismissChooseMealDialog();
                    showAddFoodDialog();
                })
                .setNegativeButton("Annulla", (d, id) -> dismissChooseMealDialog())
                .create();
        AlertDialog dialog = chooseMealDialog;

        ArrayAdapter<Meal> adapter = new ArrayAdapter<Meal>(requireContext(), R.layout.list_item_food, availableMeals) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_food, parent, false);
                }
                Meal meal = getItem(position);
                TextView nameTv = convertView.findViewById(R.id.foodNameTextView);
                ImageButton deleteBtn = convertView.findViewById(R.id.deleteFoodButton);

                if (meal != null) {
                    nameTv.setText(String.format(Locale.getDefault(), "%s (%.0f kcal)", meal.getFoodName(), meal.getCalories()));
                    deleteBtn.setOnClickListener(v -> {
                        viewModel.deleteMealFromCatalog(meal).observe(getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                remove(meal);
                                notifyDataSetChanged();
                                Toast.makeText(getContext(), "Togliere il cibo dal pasto!", Toast.LENGTH_SHORT).show();
                            } else if (result.isError()) {
                                Toast.makeText(getContext(), "Togliere il cibo dal pasto!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    // Set click listener on the whole row to select the meal
                    convertView.setOnClickListener(v -> {
                        dialog.dismiss();
                        showCategorySelectionDialog(meal);
                    });
                }
                return convertView;
            }
        };

        listView.setAdapter(adapter);
        dialog.show();
    }

    private void showCategorySelectionDialog(Meal template) {
        showCategorySelectionDialog(template, false);
    }

    private void showCategorySelectionDialog(Meal template, boolean backToAddFood) {
        dismissAddFoodDialog();

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food, null);
        dialogView.findViewById(R.id.editTextFoodName).setVisibility(View.GONE);
        dialogView.findViewById(R.id.editTextCalories).setVisibility(View.GONE);
        dialogView.findViewById(R.id.editTextCarbs).setVisibility(View.GONE);
        dialogView.findViewById(R.id.editTextProtein).setVisibility(View.GONE);
        dialogView.findViewById(R.id.editTextFat).setVisibility(View.GONE);

        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);
        spinner.setVisibility(View.VISIBLE);
        setupMealCategorySpinner(spinner);

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleziona categoria")
                .setView(dialogView)
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    MealCategory category = categoryFromSpinner(spinner);
                    logMealAndCloseDialogs(template, category);
                })
                .setNegativeButton("Indietro", (dialog, id) -> {
                    if (backToAddFood) {
                        showAddFoodDialog();
                    } else {
                        showChooseMealDialog();
                    }
                })
                .show();
    }

    private void showAddFoodDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food_search, null);
        EditText etSearchFood = dialogView.findViewById(R.id.etSearchFood);
        RecyclerView rvFoodResults = dialogView.findViewById(R.id.rvFoodResults);
        EditText nameEt = dialogView.findViewById(R.id.editTextFoodName);
        EditText calEt = dialogView.findViewById(R.id.editTextCalories);
        EditText carbEt = dialogView.findViewById(R.id.editTextCarbs);
        EditText protEt = dialogView.findViewById(R.id.editTextProtein);
        EditText fatEt = dialogView.findViewById(R.id.editTextFat);

        List<Meal> searchResults = new ArrayList<>();
        FoodSearchAdapter searchAdapter = new FoodSearchAdapter(searchResults, meal ->
                showFoodConfirmDialog(meal, () -> showCategorySelectionDialog(meal, true)));
        rvFoodResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFoodResults.setAdapter(searchAdapter);

        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= Constants.OFF_FOOD_SEARCH_MIN_QUERY_LENGTH) {
                    searchFoods(query, searchResults, searchAdapter);
                } else {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addFoodDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_food_search_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, id) ->
                        saveManualFood(nameEt, calEt, carbEt, protEt, fatEt))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dismissAddFoodDialog())
                .create();
        addFoodDialog.setOnDismissListener(d -> addFoodDialog = null);
        addFoodDialog.show();
    }

    private void searchFoods(String query, List<Meal> results, FoodSearchAdapter adapter) {
        viewModel.searchFoods(query).observe(getViewLifecycleOwner(), result -> {
            if (!isAdded()) {
                return;
            }
            if (result.isLoading()) {
                return;
            }
            if (result.isError()) {
                String message = ((Result.Error<List<Meal>>) result).getMessage();
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                results.clear();
                adapter.notifyDataSetChanged();
                return;
            }
            List<Meal> apiData = ((Result.Success<List<Meal>>) result).getData();
            results.clear();
            if (apiData != null) {
                results.addAll(apiData);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showFoodConfirmDialog(Meal meal, Runnable onConfirm) {
        AlertDialog confirmDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.food_search_confirm_title)
                .setMessage(getString(
                        R.string.food_search_confirm_message,
                        meal.getFoodName(),
                        OpenFoodFactsMapper.formatSearchSubtitle(meal)
                ))
                .setPositiveButton(R.string.food_search_confirm_add, (dialog, which) -> onConfirm.run())
                .setNegativeButton(R.string.cancel, null)
                .create();
        confirmDialog.show();
    }

    private void setupMealCategorySpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Colazione", "Pranzo", "Cena"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private static MealCategory categoryFromSpinner(Spinner spinner) {
        int position = spinner.getSelectedItemPosition();
        if (position == 1) {
            return MealCategory.PRANZO;
        }
        if (position == 2) {
            return MealCategory.CENA;
        }
        return MealCategory.COLAZIONE;
    }

    private void dismissChooseMealDialog() {
        if (chooseMealDialog != null && chooseMealDialog.isShowing()) {
            chooseMealDialog.dismiss();
        }
        chooseMealDialog = null;
    }

    private void dismissAddFoodDialog() {
        if (addFoodDialog != null && addFoodDialog.isShowing()) {
            addFoodDialog.dismiss();
        }
        addFoodDialog = null;
    }

    private void dismissAllMealDialogs() {
        dismissAddFoodDialog();
        dismissChooseMealDialog();
    }

    private void logMealAndCloseDialogs(Meal meal, MealCategory category) {
        dismissAllMealDialogs();
        viewModel.logMealForSelectedDay(meal, category).observe(getViewLifecycleOwner(), result -> {
            if (!isAdded()) {
                return;
            }
            if (result.isError()) {
                Toast.makeText(
                        requireContext(),
                        ((Result.Error<?>) result).getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            viewModel.getMealCatalog();
            Toast.makeText(requireContext(), R.string.food_logged_toast, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveManualFood(EditText nameEt, EditText calEt, EditText carbEt, EditText protEt, EditText fatEt) {
        String name = nameEt.getText().toString().trim();
        String calS = calEt.getText().toString().trim();
        if (name.isEmpty() || calS.isEmpty()) {
            Toast.makeText(requireContext(), R.string.food_name_required_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        Meal meal = new Meal(
                0,
                name,
                Float.parseFloat(calS),
                parseOptionalFloat(carbEt),
                parseOptionalFloat(protEt),
                parseOptionalFloat(fatEt)
        );
        showCategorySelectionDialog(meal, true);
    }

    private static float parseOptionalFloat(EditText editText) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            return 0f;
        }
        return Float.parseFloat(value);
    }
}
