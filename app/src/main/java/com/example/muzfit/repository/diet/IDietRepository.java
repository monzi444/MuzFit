package com.example.muzfit.repository.diet;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;

import java.util.List;

public interface IDietRepository {

    LiveData<Result<List<Meal>>> getMeals();

    LiveData<Result<List<UserMeal>>> getUserMeals(String username, long dateMillis);

    LiveData<Result<Void>> logMeal(UserMeal userMeal);

    LiveData<Result<Meal>> addMeal(Meal meal);
}
