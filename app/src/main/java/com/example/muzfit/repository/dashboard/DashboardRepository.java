package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.Workout;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.dashboard.BaseDashboardDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.DateParser;

import java.util.ArrayList;
import java.util.List;

public class DashboardRepository implements IDashboardRepository {

    private final BaseDashboardDataSource dashboardDataSource;

    public DashboardRepository(BaseDashboardDataSource dashboardDataSource) {
        this.dashboardDataSource = dashboardDataSource;
    }

    @Override
    public LiveData<Result<User>> getUserGoals(String username) {
        MutableLiveData<Result<User>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchUsers(new DataSourceCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                for (User user : data) {
                    if (username.equals(user.getUsername())) {
                        liveData.postValue(new Result.Success<>(user));
                        return;
                    }
                }
                liveData.postValue(new Result.Error<>(Constants.ERROR_USER_NOT_FOUND));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<UserMeal>>> getMealsLoggedOnDate(String username, long dateMillis) {
        MutableLiveData<Result<List<UserMeal>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchAllUserMeals(new DataSourceCallback<List<UserMeal>>() {
            @Override
            public void onSuccess(List<UserMeal> data) {
                liveData.postValue(new Result.Success<>(filterUserMeals(data, username, dateMillis, -1, -1)));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<Workout>>> getWorkoutsOnDate(String username, long dateMillis) {
        MutableLiveData<Result<List<Workout>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                liveData.postValue(new Result.Success<>(filterWorkouts(data, username, dateMillis, -1, -1)));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<UserMeal>>> getMealsLoggedInMonth(String username, int year, int month) {
        MutableLiveData<Result<List<UserMeal>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchAllUserMeals(new DataSourceCallback<List<UserMeal>>() {
            @Override
            public void onSuccess(List<UserMeal> data) {
                liveData.postValue(new Result.Success<>(filterUserMeals(data, username, 0L, year, month)));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<Workout>>> getWorkoutsInMonth(String username, int year, int month) {
        MutableLiveData<Result<List<Workout>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dashboardDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                liveData.postValue(new Result.Success<>(filterWorkouts(data, username, 0L, year, month)));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    private List<UserMeal> filterUserMeals(List<UserMeal> data, String username,
                                           long dateMillis, int year, int month) {
        List<UserMeal> filtered = new ArrayList<>();
        for (UserMeal userMeal : data) {
            if (!username.equals(userMeal.getUsername())) {
                continue;
            }
            if (year >= 0 && month >= 0) {
                if (DateParser.isSameMonth(userMeal.getDateMillis(), year, month)) {
                    filtered.add(userMeal);
                }
            } else if (DateParser.isSameDay(userMeal.getDateMillis(), dateMillis)) {
                filtered.add(userMeal);
            }
        }
        return filtered;
    }

    private List<Workout> filterWorkouts(List<Workout> data, String username,
                                         long dateMillis, int year, int month) {
        List<Workout> filtered = new ArrayList<>();
        for (Workout workout : data) {
            if (!username.equals(workout.getUsername())) {
                continue;
            }
            if (year >= 0 && month >= 0) {
                if (DateParser.isSameMonth(workout.getDateMillis(), year, month)) {
                    filtered.add(workout);
                }
            } else if (DateParser.isSameDay(workout.getDateMillis(), dateMillis)) {
                filtered.add(workout);
            }
        }
        return filtered;
    }
}
