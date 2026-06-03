package com.example.muzfit.repository.diet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.utils.Constants;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DietRepository implements IDietRepository {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private MuzFitDao localDao;
    private Future<?> seedFuture;
    private final MutableLiveData<Result<List<UserMeal>>> userMealsForDayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result<List<Meal>>> mealCatalogLiveData = new MutableLiveData<>();
    private String observedUsername;
    private long observedDateMillis;

    public DietRepository() {
    }

    public void setLocalDatabase(MuzFitDatabase database) {
        if (database != null) {
            localDao = database.muzFitDao();
        }
    }

    public void setSeedFuture(Future<?> seedFuture) {
        this.seedFuture = seedFuture;
    }

    @Override
    public LiveData<Result<List<UserMeal>>> getUserMealsForDay(String username, long dateMillis) {
        observedUsername = username;
        observedDateMillis = dateMillis;
        userMealsForDayLiveData.postValue(new Result.Loading<>());
        EXECUTOR.execute(this::loadObservedUserMealsForDay);
        return userMealsForDayLiveData;
    }

    @Override
    public LiveData<Result<List<Meal>>> getMealCatalog() {
        mealCatalogLiveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                if (localDao == null) {
                    mealCatalogLiveData.postValue(new Result.Error<>("Local database is not initialized"));
                    return;
                }
                mealCatalogLiveData.postValue(new Result.Success<>(localDao.getMeals()));
            } catch (Exception e) {
                mealCatalogLiveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return mealCatalogLiveData;
    }

    @Override
    public LiveData<Result<Meal>> addMealToCatalog(Meal meal) {
        MutableLiveData<Result<Meal>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                if (localDao == null) {
                    liveData.postValue(new Result.Error<>("Local database is not initialized"));
                    return;
                }
                Meal stored = resolveOrInsertMeal(meal);
                liveData.postValue(new Result.Success<>(stored));
                refreshMealCatalog();
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> logMeal(Meal meal, MealCategory category, String username) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                if (localDao == null) {
                    liveData.postValue(new Result.Error<>("Local database is not initialized"));
                    return;
                }
                int mealId = resolveOrInsertMeal(meal).getId();
                UserMeal userMeal = new UserMeal(mealId, username, System.currentTimeMillis(), category);
                localDao.insertUserMeal(userMeal);
                liveData.postValue(new Result.Success<>(null));
                refreshUserMealsForDay();
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> deleteLoggedMeal(UserMeal userMeal) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                if (localDao == null) {
                    liveData.postValue(new Result.Error<>("Local database is not initialized"));
                    return;
                }
                localDao.deleteUserMeal(userMeal);
                liveData.postValue(new Result.Success<>(null));
                refreshUserMealsForDay();
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    private void refreshUserMealsForDay() {
        if (observedUsername == null) {
            return;
        }
        EXECUTOR.execute(this::loadObservedUserMealsForDay);
    }

    private void loadObservedUserMealsForDay() {
        if (observedUsername == null) {
            userMealsForDayLiveData.postValue(new Result.Error<>("No day is being observed"));
            return;
        }
        try {
            awaitSeedIfNeeded();
            if (localDao == null) {
                userMealsForDayLiveData.postValue(new Result.Error<>("Local database is not initialized"));
                return;
            }
            long startOfDay = getStartOfDayMillis(observedDateMillis);
            long endOfDay = getEndOfDayMillis(startOfDay);
            List<UserMeal> userMeals = localDao.getUserMealsForDay(
                    observedUsername,
                    startOfDay,
                    endOfDay
            );
            userMealsForDayLiveData.postValue(new Result.Success<>(userMeals));
        } catch (Exception e) {
            userMealsForDayLiveData.postValue(new Result.Error<>(errorMessage(e)));
        }
    }

    private void refreshMealCatalog() {
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                if (localDao != null) {
                    mealCatalogLiveData.postValue(new Result.Success<>(localDao.getMeals()));
                }
            } catch (Exception ignored) {
                // Keep the last catalog value on refresh failure.
            }
        });
    }

    private Meal resolveOrInsertMeal(Meal meal) {
        if (meal.getId() > 0) {
            Meal existing = localDao.getMeal(meal.getId());
            if (existing != null) {
                return existing;
            }
            localDao.insertMeal(meal);
            return meal;
        }
        for (Meal existing : localDao.getMeals()) {
            if (sameMealDefinition(existing, meal)) {
                return existing;
            }
        }
        int nextId = 1;
        for (Meal existing : localDao.getMeals()) {
            nextId = Math.max(nextId, existing.getId() + 1);
        }
        Meal catalogMeal = new Meal(
                nextId,
                meal.getFoodName(),
                meal.getCalories(),
                meal.getCarbs(),
                meal.getProtein(),
                meal.getFat()
        );
        localDao.insertMeal(catalogMeal);
        return catalogMeal;
    }

    private static boolean sameMealDefinition(Meal left, Meal right) {
        return left.getFoodName().equals(right.getFoodName())
                && left.getCalories() == right.getCalories()
                && left.getCarbs() == right.getCarbs()
                && left.getProtein() == right.getProtein()
                && left.getFat() == right.getFat();
    }

    private void awaitSeedIfNeeded() throws Exception {
        if (seedFuture != null) {
            seedFuture.get();
        }
    }

    private static long getStartOfDayMillis(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long getEndOfDayMillis(long startOfDayMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfDayMillis);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    private static String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : Constants.ERROR_DATABASE;
    }
}
