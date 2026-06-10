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
import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsMapper;
import com.example.muzfit.ui.diet.DietDialogHelper;
import com.example.muzfit.ui.diet.fragment.MealSectionsBridge;
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
    private ComposeView mealSectionsCompose;
    private TextView tvTotalCalories, tvTotalCarbs, tvTotalProtein, tvTotalFat;
    private Calendar currentWeekStart;
    private int calorieGoal = 0;
    private float caloriesAssumed = 0;
    private List<UserMeal> lastUserMeals = new ArrayList<>();
    // Day start millis → progress toward calorie goal (0.0 … 1.5+)
    private final Map<Long, Float> dayProgressMap = new HashMap<>();
    
    private com.example.muzfit.ui.diet.DietDialogHelper dialogHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        weekCalendarCompose = view.findViewById(R.id.weekCalendarCompose);
        mealSectionsCompose = view.findViewById(R.id.mealSectionsCompose);

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
            dayProgressMap,
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
                // Re-run progress calculations now that we have the real calorie goal
                setupCalendar();
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
                    if (lastUserMeals != null && !lastUserMeals.isEmpty()) {
                        float totalKcal = 0;
                        for (UserMeal um : lastUserMeals) {
                            Meal m = viewModel.getMealFor(um);
                            if (m != null) totalKcal += m.getCalories();
                        }
                        float progress = calorieGoal > 0 ? totalKcal / calorieGoal : 0f;
                        dayProgressMap.put(dayStart, progress);
                    }
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
        renderMealSections(lastUserMeals);
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
        tvTotalCarbs.setText(String.format(Locale.getDefault(), "%.0f", totalCarbs));
        tvTotalProtein.setText(String.format(Locale.getDefault(), "%.0f", totalProtein));
        tvTotalFat.setText(String.format(Locale.getDefault(), "%.0f", totalFat));
    }

    private void updateRemainingCalories() {
        if (tvCaloriesRemaining != null) {
            int remaining = calorieGoal - (int) caloriesAssumed;
            tvCaloriesRemaining.setText(String.valueOf(remaining));
            tvCaloriesRemaining.setTextColor(ContextCompat.getColor(requireContext(), remaining < 0 ? R.color.fat_color : R.color.muz_primary_lime));
        }
    }

    private void renderMealSections(List<UserMeal> userMeals) {
        java.util.List<UserMeal> colazione = new ArrayList<>();
        java.util.List<UserMeal> pranzo = new ArrayList<>();
        java.util.List<UserMeal> cena = new ArrayList<>();

        if (userMeals != null) {
            for (UserMeal um : userMeals) {
                if (um.getCategory() == MealCategory.COLAZIONE) {
                    colazione.add(um);
                } else if (um.getCategory() == MealCategory.PRANZO) {
                    pranzo.add(um);
                } else if (um.getCategory() == MealCategory.CENA) {
                    cena.add(um);
                }
            }
        }

        MealSectionsBridge.setContent(
                mealSectionsCompose,
                colazione, pranzo, cena,
                (UserMeal um) -> viewModel.getMealFor(um),
                (UserMeal um) -> { deleteLoggedMeal(um); return kotlin.Unit.INSTANCE; },
                () -> { dialogHelper.showChooseMealDialog(MealCategory.COLAZIONE); return kotlin.Unit.INSTANCE; },
                () -> { dialogHelper.showChooseMealDialog(MealCategory.PRANZO); return kotlin.Unit.INSTANCE; },
                () -> { dialogHelper.showChooseMealDialog(MealCategory.CENA); return kotlin.Unit.INSTANCE; }
        );
    }

    private void deleteLoggedMeal(UserMeal um) {
        viewModel.deleteLoggedMeal(um).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                com.example.muzfit.utils.MuzFitToast.show(requireContext(), R.string.food_removed_toast);
            } else if (result.isError()) {
                com.example.muzfit.utils.MuzFitToast.showError(
                        requireContext(),
                        ((com.example.muzfit.model.Result.Error<?>) result).getMessage()
                );
            }
        });
    }

    private void setupCalendar() {
        // Load progress data for all days in the visible week
        loadWeekProgress();

        // Update the ComposeView content
        setupDietWeekCalendar(
            weekCalendarCompose,
            currentWeekStart.getTimeInMillis(),
            viewModel.getSelectedDateMillis().getValue(),
            dayProgressMap,
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

    private void loadWeekProgress() {
        try {
            MuzFitDatabase db = ServiceLocator.getInstance().getDatabase();
            if (db == null) return;
            MuzFitDao dao = db.muzFitDao();
            String uid = com.example.muzfit.utils.RepositorySupport.currentUidOrDefault();
            if (uid == null) return;

            Calendar cal = (Calendar) currentWeekStart.clone();
            for (int i = 0; i < 7; i++) {
                long dayStart = startOfDayMillis(cal.getTimeInMillis());
                long dayEnd = dayStart + 24L * 60L * 60L * 1000L;
                float consumed = dao.getConsumedCalories(uid, dayStart, dayEnd);
                if (consumed > 0f) {
                    float progress = calorieGoal > 0 ? consumed / calorieGoal : 0f;
                    dayProgressMap.put(dayStart, progress);
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception ignored) {
            // Gracefully degrade if database or auth isn't ready yet
        }
    }

    private long startOfDayMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
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
