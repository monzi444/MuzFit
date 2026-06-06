package com.example.muzfit.ui.diet.fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
import android.view.Window;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import com.example.muzfit.utils.MuzFitToast;
import com.example.muzfit.utils.ServiceLocator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private final Handler foodSearchHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private Runnable pendingFoodSearchRunnable;
    private String pendingFoodSearchQuery = "";
    @Nullable
    private List<Meal> activeSearchResults;
    @Nullable
    private FoodSearchAdapter activeSearchAdapter;
    @Nullable
    private View activeSearchLoadingContainer;

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
        
        MaterialButton chooseMealButton = view.findViewById(R.id.chooseMealButton);
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

        profileViewModel.getUser().observe(getViewLifecycleOwner(), result -> {
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
                nameTv.setText(formatMealEntry(meal));
                deleteBtn.setOnClickListener(v -> viewModel.deleteLoggedMeal(userMeal)
                        .observe(getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                MuzFitToast.show(requireContext(), R.string.food_removed_toast);
                            } else if (result.isError()) {
                                MuzFitToast.showError(
                                        requireContext(),
                                        ((com.example.muzfit.model.Result.Error<?>) result).getMessage()
                                );
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
        emptyTv.setText(R.string.diet_empty_meals);
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

        tvMonthYear.setText(String.format(
                Locale.ENGLISH,
                "%s %d",
                currentWeekStart.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH),
                currentWeekStart.get(Calendar.YEAR)
        ));
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
                dayView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle_selection_stroke));
                int textColor = (yearNum == todayYear && dayOfYear == todayDayOfYear) 
                        ? R.color.muz_primary_lime 
                        : R.color.muz_on_surface;
                dayView.setTextColor(ContextCompat.getColor(requireContext(), textColor));
                dayView.setTypeface(null, Typeface.BOLD);
            } else if (yearNum == todayYear && dayOfYear == todayDayOfYear) {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.muz_primary_lime));
                dayView.setTypeface(null, Typeface.BOLD);
            } else {
                dayView.setTextColor(ContextCompat.getColor(requireContext(), R.color.muz_on_surface));
                if (tempCalendar.after(today)) dayView.setAlpha(0.5f);
            }
            dayView.setOnClickListener(v -> {
                viewModel.setSelectedDate(timeMillis);
            });
            calendarGrid.addView(dayView); tempCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private AlertDialog.Builder styledDialogBuilder() {
        return new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog);
    }

    private void applyDialogWindowStyle(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void applyLargeDialogWindowStyle(AlertDialog dialog) {
        applyDialogWindowStyle(dialog);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.92f);
        int height = (int) (metrics.heightPixels * 0.78f);
        window.setLayout(width, height);
    }

    private enum MealCatalogSort {
        NAME_ASC,
        NAME_DESC,
        CALORIES_ASC,
        CALORIES_DESC
    }

    private void showChooseMealDialog() {
        List<Meal> masterMeals = new ArrayList<>();
        masterMeals.add(new Meal(0, "Apple", 95, 25, 1, 0));
        masterMeals.add(new Meal(0, "Pasta with tomato sauce", 350, 70, 10, 5));
        masterMeals.add(new Meal(0, "Chicken breast", 165, 0, 31, 4));

        Map<Integer, Meal> catalog = viewModel.getMealsById().getValue();
        if (catalog != null) {
            masterMeals.addAll(catalog.values());
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choose_meal, null);
        ListView listView = dialogView.findViewById(R.id.lvChooseMeal);
        TextView emptyView = dialogView.findViewById(R.id.tvChooseMealEmpty);
        EditText searchField = dialogView.findViewById(R.id.etChooseMealSearch);
        AutoCompleteTextView sortField = dialogView.findViewById(R.id.actvChooseMealSort);

        chooseMealDialog = styledDialogBuilder()
                .setView(dialogView)
                .create();
        AlertDialog dialog = chooseMealDialog;

        dialogView.findViewById(R.id.btnAddFoodFromPicker).setOnClickListener(v -> {
            dismissChooseMealDialog();
            showAddFoodDialog();
        });
        dialogView.findViewById(R.id.btnCancelChooseMeal).setOnClickListener(v -> dismissChooseMealDialog());

        final MealCatalogSort[] selectedSort = {MealCatalogSort.NAME_ASC};
        String[] sortLabels = new String[]{
                getString(R.string.choose_meal_sort_name_asc),
                getString(R.string.choose_meal_sort_name_desc),
                getString(R.string.choose_meal_sort_calories_asc),
                getString(R.string.choose_meal_sort_calories_desc)
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                sortLabels
        );
        sortField.setAdapter(sortAdapter);
        sortField.setText(sortLabels[0], false);

        ArrayAdapter<Meal>[] listAdapterRef = new ArrayAdapter[1];
        listAdapterRef[0] = new ArrayAdapter<Meal>(requireContext(), R.layout.list_item_food, new ArrayList<>()) {
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
                    nameTv.setText(formatMealEntry(meal));
                    deleteBtn.setOnClickListener(v -> {
                        viewModel.deleteMealFromCatalog(meal).observe(getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                masterMeals.remove(meal);
                                refreshChooseMealList(
                                        masterMeals,
                                        listAdapterRef[0],
                                        searchField,
                                        selectedSort[0],
                                        emptyView,
                                        listView
                                );
                                MuzFitToast.show(getContext(), R.string.catalog_food_removed_toast);
                            } else if (result.isError()) {
                                MuzFitToast.showError(
                                        getContext(),
                                        resolveCatalogDeleteError(((Result.Error<?>) result).getMessage())
                                );
                            }
                        });
                    });

                    convertView.setOnClickListener(v -> {
                        dialog.dismiss();
                        chooseMealDialog = null;
                        showCategorySelectionDialog(meal);
                    });
                }
                return convertView;
            }
        };

        sortField.setOnItemClickListener((parent, view, position, id) -> {
            selectedSort[0] = MealCatalogSort.values()[position];
            refreshChooseMealList(masterMeals, listAdapterRef[0], searchField, selectedSort[0], emptyView, listView);
        });

        listView.setAdapter(listAdapterRef[0]);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshChooseMealList(masterMeals, listAdapterRef[0], searchField, selectedSort[0], emptyView, listView);
            }
        });

        refreshChooseMealList(masterMeals, listAdapterRef[0], searchField, selectedSort[0], emptyView, listView);
        chooseMealDialog.setOnDismissListener(d -> chooseMealDialog = null);
        chooseMealDialog.show();
        applyLargeDialogWindowStyle(chooseMealDialog);
    }

    private void refreshChooseMealList(
            List<Meal> masterMeals,
            ArrayAdapter<Meal> adapter,
            EditText searchField,
            MealCatalogSort sort,
            TextView emptyView,
            ListView listView
    ) {
        String query = searchField.getText() != null
                ? searchField.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";
        List<Meal> filtered = new ArrayList<>();
        for (Meal meal : masterMeals) {
            if (meal == null || meal.getFoodName() == null) {
                continue;
            }
            if (query.isEmpty() || meal.getFoodName().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(meal);
            }
        }
        Comparator<Meal> comparator;
        if (sort == MealCatalogSort.NAME_DESC) {
            comparator = Comparator.comparing(
                    m -> m.getFoodName().toLowerCase(Locale.ROOT),
                    Comparator.reverseOrder()
            );
        } else if (sort == MealCatalogSort.CALORIES_ASC) {
            comparator = Comparator.comparing(Meal::getCalories);
        } else if (sort == MealCatalogSort.CALORIES_DESC) {
            comparator = Comparator.comparing(Meal::getCalories).reversed();
        } else {
            comparator = Comparator.comparing(m -> m.getFoodName().toLowerCase(Locale.ROOT));
        }
        Collections.sort(filtered, comparator);

        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();

        boolean isEmpty = filtered.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showCategorySelectionDialog(Meal template) {
        showCategorySelectionDialog(template, false);
    }

    private void showCategorySelectionDialog(Meal template, boolean backToAddFood) {
        dismissAddFoodDialog();

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_meal_category, null);
        MaterialCardView cardColazione = dialogView.findViewById(R.id.cardColazione);
        MaterialCardView cardPranzo = dialogView.findViewById(R.id.cardPranzo);
        MaterialCardView cardCena = dialogView.findViewById(R.id.cardCena);

        final MealCategory[] selectedCategory = {MealCategory.COLAZIONE};
        updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);

        cardColazione.setOnClickListener(v -> {
            selectedCategory[0] = MealCategory.COLAZIONE;
            updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);
        });
        cardPranzo.setOnClickListener(v -> {
            selectedCategory[0] = MealCategory.PRANZO;
            updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);
        });
        cardCena.setOnClickListener(v -> {
            selectedCategory[0] = MealCategory.CENA;
            updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);
        });

        AlertDialog categoryDialog = styledDialogBuilder()
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCategoryConfirm).setOnClickListener(v -> {
            categoryDialog.dismiss();
            logMealAndCloseDialogs(template, selectedCategory[0]);
        });
        dialogView.findViewById(R.id.btnCategoryBack).setOnClickListener(v -> {
            categoryDialog.dismiss();
            if (backToAddFood) {
                showAddFoodDialog();
            } else {
                showChooseMealDialog();
            }
        });
        applyDialogWindowStyle(categoryDialog);
        categoryDialog.show();
    }

    private void updateCategoryCardSelection(MaterialCardView cardColazione, MaterialCardView cardPranzo,
                                             MaterialCardView cardCena, MealCategory selected) {
        styleCategoryCard(cardColazione, selected == MealCategory.COLAZIONE);
        styleCategoryCard(cardPranzo, selected == MealCategory.PRANZO);
        styleCategoryCard(cardCena, selected == MealCategory.CENA);
    }

    private void styleCategoryCard(MaterialCardView card, boolean selected) {
        card.setStrokeWidth(selected ? 2 : 1);
        int color = ContextCompat.getColor(
                requireContext(),
                selected ? R.color.muz_primary_lime : R.color.muz_glass_border
        );
        card.setStrokeColor(ColorStateList.valueOf(color));
    }

    private void showAddFoodDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food_search, null);
        EditText etSearchFood = dialogView.findViewById(R.id.etSearchFood);
        RecyclerView rvFoodResults = dialogView.findViewById(R.id.rvFoodResults);
        activeSearchLoadingContainer = dialogView.findViewById(R.id.searchLoadingContainer);

        activeSearchResults = new ArrayList<>();
        activeSearchAdapter = new FoodSearchAdapter(activeSearchResults, meal ->
                showFoodConfirmDialog(meal, () -> showCategorySelectionDialog(meal, true)));
        rvFoodResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFoodResults.setAdapter(activeSearchAdapter);

        viewModel.getFoodSearchResults().observe(getViewLifecycleOwner(), foodSearchObserver);

        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleFoodSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addFoodDialog = styledDialogBuilder()
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnAddFoodManual).setOnClickListener(v -> {
            dismissAddFoodDialog();
            showManualFoodDialog();
        });
        dialogView.findViewById(R.id.btnCancelSearch).setOnClickListener(v -> dismissAddFoodDialog());

        addFoodDialog.setOnDismissListener(d -> {
            cancelPendingFoodSearch();
            viewModel.getFoodSearchResults().removeObserver(foodSearchObserver);
            activeSearchResults = null;
            activeSearchAdapter = null;
            activeSearchLoadingContainer = null;
            addFoodDialog = null;
        });
        applyDialogWindowStyle(addFoodDialog);
        addFoodDialog.show();
    }

    private final Observer<Result<List<Meal>>> foodSearchObserver = result -> {
        if (activeSearchResults == null || activeSearchAdapter == null || addFoodDialog == null || !addFoodDialog.isShowing()) {
            return;
        }
        if (result.isLoading()) {
            setFoodSearchLoading(true);
            return;
        }
        setFoodSearchLoading(false);
        activeSearchResults.clear();
        if (result.isSuccess()) {
            List<Meal> apiData = ((Result.Success<List<Meal>>) result).getData();
            if (apiData != null) {
                activeSearchResults.addAll(apiData);
            }
        }
        activeSearchAdapter.notifyDataSetChanged();
    };

    private void setFoodSearchLoading(boolean loading) {
        if (activeSearchLoadingContainer != null) {
            activeSearchLoadingContainer.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void scheduleFoodSearch(String query) {
        pendingFoodSearchQuery = query;
        if (pendingFoodSearchRunnable != null) {
            foodSearchHandler.removeCallbacks(pendingFoodSearchRunnable);
        }
        if (query.length() < Constants.OFF_FOOD_SEARCH_MIN_QUERY_LENGTH) {
            setFoodSearchLoading(false);
            if (activeSearchResults != null && activeSearchAdapter != null) {
                activeSearchResults.clear();
                activeSearchAdapter.notifyDataSetChanged();
            }
            viewModel.searchFoods("");
            return;
        }
        setFoodSearchLoading(true);
        pendingFoodSearchRunnable = () -> {
            if (query.equals(pendingFoodSearchQuery)) {
                viewModel.searchFoods(query);
            }
        };
        foodSearchHandler.postDelayed(pendingFoodSearchRunnable, Constants.OFF_FOOD_SEARCH_DEBOUNCE_MS);
    }

    private void cancelPendingFoodSearch() {
        if (pendingFoodSearchRunnable != null) {
            foodSearchHandler.removeCallbacks(pendingFoodSearchRunnable);
            pendingFoodSearchRunnable = null;
        }
        pendingFoodSearchQuery = "";
        setFoodSearchLoading(false);
        viewModel.searchFoods("");
    }

    private void showManualFoodDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food_manual, null);
        EditText nameEt = dialogView.findViewById(R.id.editTextFoodName);
        EditText calEt = dialogView.findViewById(R.id.editTextCalories);
        EditText carbEt = dialogView.findViewById(R.id.editTextCarbs);
        EditText protEt = dialogView.findViewById(R.id.editTextProtein);
        EditText fatEt = dialogView.findViewById(R.id.editTextFat);

        AlertDialog manualDialog = styledDialogBuilder()
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnSaveManualFood).setOnClickListener(v -> {
            if (saveManualFood(nameEt, calEt, carbEt, protEt, fatEt)) {
                manualDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.btnBackManualFood).setOnClickListener(v -> {
            manualDialog.dismiss();
            showAddFoodDialog();
        });
        applyDialogWindowStyle(manualDialog);
        manualDialog.show();
    }

    private void showFoodConfirmDialog(Meal meal, Runnable onConfirm) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_food_confirm, null);
        TextView tvName = dialogView.findViewById(R.id.tvConfirmFoodName);
        TextView tvDetails = dialogView.findViewById(R.id.tvConfirmFoodDetails);
        tvName.setText(meal.getFoodName());
        tvDetails.setText(OpenFoodFactsMapper.formatSearchSubtitle(meal));

        AlertDialog confirmDialog = styledDialogBuilder()
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnConfirmAdd).setOnClickListener(v -> {
            confirmDialog.dismiss();
            onConfirm.run();
        });
        dialogView.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> confirmDialog.dismiss());
        applyDialogWindowStyle(confirmDialog);
        confirmDialog.show();
    }

    private String formatMealEntry(Meal meal) {
        return meal.getFoodName()
                + " ("
                + Math.round(meal.getCalories())
                + " "
                + getString(R.string.meal_entry_kcal_unit)
                + ")";
    }

    private CharSequence resolveCatalogDeleteError(String message) {
        if (message != null && Constants.ERROR_MEAL_IN_USE.equals(message)) {
            return getString(R.string.catalog_meal_remove_in_use);
        }
        return message != null ? message : getString(R.string.catalog_meal_remove_in_use);
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
        } else {
            cancelPendingFoodSearch();
            viewModel.getFoodSearchResults().removeObserver(foodSearchObserver);
            activeSearchResults = null;
            activeSearchAdapter = null;
            activeSearchLoadingContainer = null;
            addFoodDialog = null;
        }
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
                MuzFitToast.showError(requireContext(), ((Result.Error<?>) result).getMessage());
                return;
            }
            viewModel.getMealCatalog();
            MuzFitToast.show(requireContext(), R.string.food_logged_toast);
        });
    }

    private boolean saveManualFood(EditText nameEt, EditText calEt, EditText carbEt, EditText protEt, EditText fatEt) {
        String name = nameEt.getText() != null ? nameEt.getText().toString().trim() : "";
        String calS = calEt.getText() != null ? calEt.getText().toString().trim() : "";
        if (name.isEmpty() || calS.isEmpty()) {
            MuzFitToast.showError(requireContext(), getString(R.string.food_name_required_toast));
            return false;
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
        return true;
    }

    private static float parseOptionalFloat(EditText editText) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            return 0f;
        }
        return Float.parseFloat(value);
    }
}
