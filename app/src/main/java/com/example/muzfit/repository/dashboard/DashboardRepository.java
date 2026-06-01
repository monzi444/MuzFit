package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.dashboard.BaseDashboardDataSource;
import com.example.muzfit.utils.DateParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardRepository implements IDashboardRepository {

    private static final int PLACEHOLDER_ITEM_COUNT = 5;
    private static final int WEEK_DAYS = 7;

    private final BaseDashboardDataSource dashboardDataSource;

    public DashboardRepository(BaseDashboardDataSource dashboardDataSource) {
        this.dashboardDataSource = dashboardDataSource;
    }

    @Override
    public LiveData<Result<Float>> getCosumedCalories() {
        return getConsumedMacro(Macro.CALORIES);
    }

    @Override
    public LiveData<Result<Float>> getConsumedCarbs() {
        return getConsumedMacro(Macro.CARBS);
    }

    @Override
    public LiveData<Result<Float>> getConsumedProteins() {
        return getConsumedMacro(Macro.PROTEINS);
    }

    @Override
    public LiveData<Result<Float>> getConsumedFats() {
        return getConsumedMacro(Macro.FATS);
    }

    @Override
    public LiveData<Result<List<WeightEntry>>> getWeights(String username) {
        MutableLiveData<Result<List<WeightEntry>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchWeightEntries(new DataSourceCallback<List<WeightEntry>>() {
            @Override
            public void onSuccess(List<WeightEntry> data) {
                List<WeightEntry> filtered = new ArrayList<>();
                for (WeightEntry entry : data) {
                    if (username.equals(entry.getUsername())) {
                        filtered.add(entry);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<int[]>> getDailyCaloriesBurned() {
        MutableLiveData<Result<int[]>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> workouts) {
                dashboardDataSource.fetchWorkoutExercises(new DataSourceCallback<List<WorkoutExercise>>() {
                    @Override
                    public void onSuccess(List<WorkoutExercise> workoutExercises) {
                        liveData.postValue(new Result.Success<>(
                                buildDailyCaloriesBurned(workouts, workoutExercises)
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        liveData.postValue(new Result.Error<>(message));
                    }
                });
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    private LiveData<Result<Float>> getConsumedMacro(Macro macro) {
        MutableLiveData<Result<Float>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchAllUserMeals(new DataSourceCallback<List<UserMeal>>() {
            @Override
            public void onSuccess(List<UserMeal> userMeals) {
                dashboardDataSource.fetchMeals(new DataSourceCallback<List<Meal>>() {
                    @Override
                    public void onSuccess(List<Meal> meals) {
                        liveData.postValue(new Result.Success<>(
                                sumMacro(selectConsumedMeals(userMeals, meals), macro)
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        liveData.postValue(new Result.Error<>(message));
                    }
                });
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    private List<Meal> selectConsumedMeals(List<UserMeal> userMeals, List<Meal> meals) {
        List<Meal> selected = new ArrayList<>();
        if (meals == null || meals.isEmpty()) {
            return selected;
        }

        long today = System.currentTimeMillis();
        Set<Integer> todayMealIds = new HashSet<>();
        if (userMeals != null) {
            for (UserMeal userMeal : userMeals) {
                if (DateParser.isSameDay(userMeal.getDateMillis(), today)) {
                    todayMealIds.add(userMeal.getMealId());
                }
            }
        }

        for (Meal meal : meals) {
            if (todayMealIds.contains(meal.getId())) {
                selected.add(meal);
            }
        }

        if (selected.isEmpty()) {
            selected.addAll(selectPlaceholderItems(meals, PLACEHOLDER_ITEM_COUNT));
        }
        return selected;
    }

    private float sumMacro(List<Meal> meals, Macro macro) {
        float total = 0f;
        for (Meal meal : meals) {
            switch (macro) {
                case CALORIES:
                    total += meal.getCalories();
                    break;
                case CARBS:
                    total += meal.getCarbs();
                    break;
                case PROTEINS:
                    total += meal.getProtein();
                    break;
                case FATS:
                    total += meal.getFat();
                    break;
                default:
                    break;
            }
        }
        return total;
    }

    private int[] buildDailyCaloriesBurned(List<Workout> workouts, List<WorkoutExercise> workoutExercises) {
        int[] dailyCalories = new int[WEEK_DAYS];
        for (int dayIndex = 0; dayIndex < WEEK_DAYS; dayIndex++) {
            List<Workout> dailyWorkouts = selectPlaceholderItems(workouts, PLACEHOLDER_ITEM_COUNT);
            Set<Integer> workoutIds = new HashSet<>();
            for (Workout workout : dailyWorkouts) {
                workoutIds.add(workout.getId());
            }

            int total = 0;
            for (WorkoutExercise workoutExercise : workoutExercises) {
                if (workoutIds.contains(workoutExercise.getWorkoutId())) {
                    total += workoutExercise.getCalories();
                }
            }

            if (total == 0) {
                total = sumPlaceholderWorkoutExercises(workoutExercises);
            }
            dailyCalories[dayIndex] = total;
        }
        return dailyCalories;
    }

    private int sumPlaceholderWorkoutExercises(List<WorkoutExercise> workoutExercises) {
        int total = 0;
        for (WorkoutExercise workoutExercise : selectPlaceholderItems(workoutExercises, PLACEHOLDER_ITEM_COUNT)) {
            total += workoutExercise.getCalories();
        }
        return total;
    }

    private <T> List<T> selectPlaceholderItems(List<T> data, int count) {
        List<T> selected = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return selected;
        }
        selected.addAll(data);
        Collections.shuffle(selected);
        return selected.subList(0, Math.min(count, selected.size()));
    }

    private enum Macro {
        CALORIES,
        CARBS,
        PROTEINS,
        FATS
    }
}
