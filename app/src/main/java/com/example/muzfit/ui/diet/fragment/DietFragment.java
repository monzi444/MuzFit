package com.example.muzfit.ui.diet.fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
import android.view.Window;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
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
import androidx.compose.ui.platform.ComposeView;
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

import static com.example.muzfit.ui.diet.fragment.DietWeekCalendarKt.setupDietWeekCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DietFragment extends Fragment {

    private DietViewModel viewModel;
    private ProfileViewModel profileViewModel;
    private TextView tvCaloriesRemaining;
    private ComposeView weekCalendarCompose;
    private TextView tvTotalCalories, tvTotalCarbs, tvTotalProtein, tvTotalFat;
    private LinearLayout containerColazione, containerPranzo, containerCena;
    private Calendar currentWeekStart;
    private int calorieGoal = 0;
    private float caloriesAssumed = 0;
    private List<UserMeal> lastUserMeals = new ArrayList<>();
    // Tracks which days (start-of-day millis) have logged meals — updated as data loads
    private final Map<Long, Boolean> daysWithMealsMap = new HashMap<>();
    
    private com.example.muzfit.ui.diet.DietDialogHelper dialogHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        weekCalendarCompose = view.findViewById(R.id.weekCalendarCompose);
        
        containerColazione = view.findViewById(R.id.containerColazione);
        containerPranzo = view.findViewById(R.id.containerPranzo);
        containerCena = view.findViewById(R.id.containerCena);
        
        MaterialButton chooseMealButton = view.findViewById(R.id.chooseMealButton);

        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        tvTotalCarbs = view.findViewById(R.id.tvTotalCarbs);
        tvTotalProtein = view.findViewById(R.id.tvTotalProtein);
        tvTotalFat = view.findViewById(R.id.tvTotalFat);
        tvCaloriesRemaining = view.findViewById(R.id.tvCaloriesRemaining);

        IDietRepository repository = ServiceLocator.getInstance().getDietRepository();
        viewModel = new ViewModelProvider(requireActivity(), new DietViewModelFactory(repository)).get(DietViewModel.class);

        IProfileRepository profileRepository = ServiceLocator.getInstance().getProfileRepository();
        profileViewModel = new ViewModelProvider(this, new ProfileViewModelFactory(profileRepository)).get(ProfileViewModel.class);

        dialogHelper = new com.example.muzfit.ui.diet.DietDialogHelper(requireActivity(), viewModel, getViewLifecycleOwner());

        currentWeekStart = Calendar.getInstance();
        currentWeekStart.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int offsetToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        currentWeekStart.add(Calendar.DAY_OF_YEAR, offsetToMonday);

        // Set up Compose calendar after currentWeekStart is initialized
        setupDietWeekCalendar(
            weekCalendarCompose,
            currentWeekStart.getTimeInMillis(),
            viewModel.getSelectedDateMillis().getValue(),
            daysWithMealsMap,
            millis -> {
                viewModel.setSelectedDate(millis);
                return kotlin.Unit.INSTANCE;
            },
            () -> {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
                setupCalendar();
                return kotlin.Unit.INSTANCE;
            },
            () -> {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
                setupCalendar();
                return kotlin.Unit.INSTANCE;
            }
        );

        setupCalendar();

        profileViewModel.getUser().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                User user = ((com.example.muzfit.model.Result.Success<User>) result).getData();
                calorieGoal = user.getCalorieGoal();
                tvGreeting.setText(getString(R.string.greeting_prefix, user.getName()));
                updateRemainingCalories();
            }
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
                // Track which day has meals — store start-of-day millis as key
                Long selDate = viewModel.getSelectedDateMillis().getValue();
                if (selDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(selDate);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long dayStart = cal.getTimeInMillis();
                    daysWithMealsMap.put(dayStart, lastUserMeals != null && !lastUserMeals.isEmpty());
                }
                refreshDietUi();
            }
        });

        viewModel.getSelectedDateMillis().observe(getViewLifecycleOwner(), date -> {
            setupCalendar();
        });

        chooseMealButton.setOnClickListener(v -> dialogHelper.showChooseMealDialog());

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
        // Update the ComposeView content with the latest week data
        setupDietWeekCalendar(
            weekCalendarCompose,
            currentWeekStart.getTimeInMillis(),
            viewModel.getSelectedDateMillis().getValue(),
            daysWithMealsMap,
            millis -> {
                viewModel.setSelectedDate(millis);
                return kotlin.Unit.INSTANCE;
            },
            () -> {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
                setupCalendar();
                return kotlin.Unit.INSTANCE;
            },
            () -> {
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
                setupCalendar();
                return kotlin.Unit.INSTANCE;
            }
        );
    }

    private String formatMealEntry(Meal meal) {
        return meal.getFoodName()
                + " ("
                + Math.round(meal.getCalories())
                + " "
                + getString(R.string.meal_entry_kcal_unit)
                + ")";
    }
}
