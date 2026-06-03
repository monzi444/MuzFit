package com.example.muzfit.repository.diet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Food;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.diet.BaseDietDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.DateParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DietRepository implements IDietRepository {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final BaseDietDataSource dietDataSource;
    private MuzFitDao localDao;
    private Future<?> seedFuture;

    private final MutableLiveData<Result<List<Meal>>> mealsLiveData = new MutableLiveData<>();
    private List<Meal> cachedMeals = new ArrayList<>();

    public DietRepository(BaseDietDataSource dietDataSource) {
        this.dietDataSource = dietDataSource;
    }

    public void setLocalDatabase(MuzFitDatabase database) {
        if (database != null) {
            localDao = database.muzFitDao();
        }
    }

    public void setSeedFuture(Future<?> seedFuture) {
        this.seedFuture = seedFuture;
    }

    private void awaitSeedIfNeeded() {
        if (seedFuture != null) {
            try {
                seedFuture.get();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public LiveData<Result<List<Meal>>> getMeals() {
        if (mealsLiveData.getValue() == null) {
            mealsLiveData.setValue(new Result.Loading<>());
            refreshMeals();
        }
        return mealsLiveData;
    }

    private void refreshMeals() {
        dietDataSource.fetchMeals(new DataSourceCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> data) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> localDao.insertMeals(data));
                }
                cachedMeals = new ArrayList<>(data);
                mealsLiveData.postValue(new Result.Success<>(cachedMeals));
            }

            @Override
            public void onError(String message) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> {
                        awaitSeedIfNeeded();
                        cachedMeals = localDao.getMeals();
                        mealsLiveData.postValue(new Result.Success<>(cachedMeals));
                    });
                } else {
                    mealsLiveData.postValue(new Result.Error<>(message));
                }
            }
        });
    }

    @Override
    public LiveData<Result<List<UserMeal>>> getUserMeals(String username, long dateMillis) {
        MutableLiveData<Result<List<UserMeal>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dietDataSource.fetchAllUserMeals(new DataSourceCallback<List<UserMeal>>() {
            @Override
            public void onSuccess(List<UserMeal> data) {
                List<UserMeal> myUserMeals = new ArrayList<>();
                List<UserMeal> filtered = new ArrayList<>();
                for (UserMeal userMeal : data) {
                    if (username.equals(userMeal.getUsername())) {
                        myUserMeals.add(userMeal);
                        if (DateParser.isSameDay(userMeal.getDateMillis(), dateMillis)) {
                            filtered.add(userMeal);
                        }
                    }
                }
                if (localDao != null) {
                    EXECUTOR.execute(() -> localDao.insertUserMeals(myUserMeals));
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> {
                        awaitSeedIfNeeded();
                        List<UserMeal> allUserMeals = localDao.getUserMeals(username);
                        List<UserMeal> filtered = new ArrayList<>();
                        for (UserMeal userMeal : allUserMeals) {
                            if (DateParser.isSameDay(userMeal.getDateMillis(), dateMillis)) {
                                filtered.add(userMeal);
                            }
                        }
                        liveData.postValue(new Result.Success<>(filtered));
                    });
                } else {
                    liveData.postValue(new Result.Error<>(message));
                }
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> logMeal(UserMeal userMeal) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        // We only log to local if offline for now, or just simulate success
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                localDao.insertUserMeal(userMeal);
                liveData.postValue(new Result.Success<>(null));
            });
        } else {
            liveData.setValue(new Result.Success<>(null));
        }
        return liveData;
    }

    @Override
    public LiveData<Result<Meal>> addMeal(Meal meal) {
        MutableLiveData<Result<Meal>> liveData = new MutableLiveData<>();
        meal.setId(cachedMeals.size() + 1);
        cachedMeals.add(meal);
        mealsLiveData.setValue(new Result.Success<>(cachedMeals));
        liveData.setValue(new Result.Success<>(meal));
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> deleteMeal(int id) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        for (int i = 0; i < cachedMeals.size(); i++) {
            if (cachedMeals.get(i).getId() == id) {
                cachedMeals.remove(i);
                break;
            }
        }
        mealsLiveData.setValue(new Result.Success<>(cachedMeals));
        liveData.setValue(new Result.Success<>(null));
        return liveData;
    }
}
