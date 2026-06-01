package com.example.muzfit.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.Workout;
import com.example.muzfit.repository.dashboard.IDashboardRepository;
import com.example.muzfit.utils.Constants;

import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final IDashboardRepository repository;

    public DashboardViewModel(IDashboardRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<User>> getUserGoals(String username) {
        return repository.getUserGoals(username);
    }

    public LiveData<Result<User>> getDefaultUserGoals() {
        return repository.getUserGoals(Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<List<UserMeal>>> getMealsLoggedOnDate(String username, long dateMillis) {
        return repository.getMealsLoggedOnDate(username, dateMillis);
    }

    public LiveData<Result<List<Workout>>> getWorkoutsOnDate(String username, long dateMillis) {
        return repository.getWorkoutsOnDate(username, dateMillis);
    }

    public LiveData<Result<List<UserMeal>>> getMealsLoggedInMonth(String username, int year, int month) {
        return repository.getMealsLoggedInMonth(username, year, month);
    }

    public LiveData<Result<List<Workout>>> getWorkoutsInMonth(String username, int year, int month) {
        return repository.getWorkoutsInMonth(username, year, month);
    }

    public LiveData<Result<List<UserMeal>>> getDefaultUserMealsToday() {
        return repository.getMealsLoggedOnDate(Constants.DEFAULT_USERNAME, System.currentTimeMillis());
    }

    public LiveData<Result<List<Workout>>> getDefaultUserWorkoutsToday() {
        return repository.getWorkoutsOnDate(Constants.DEFAULT_USERNAME, System.currentTimeMillis());
    }
}
