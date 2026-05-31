package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.Workout;

import java.util.List;

public interface IDashboardRepository {

    LiveData<Result<User>> getUserGoals(String username);

    LiveData<Result<List<UserMeal>>> getMealsLoggedOnDate(String username, long dateMillis);

    LiveData<Result<List<Workout>>> getWorkoutsOnDate(String username, long dateMillis);

    LiveData<Result<List<UserMeal>>> getMealsLoggedInMonth(String username, int year, int month);

    LiveData<Result<List<Workout>>> getWorkoutsInMonth(String username, int year, int month);
}
