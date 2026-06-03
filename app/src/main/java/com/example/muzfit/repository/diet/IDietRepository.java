package com.example.muzfit.repository.diet;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;

import java.util.List;

public interface IDietRepository {

    LiveData<Result<List<UserMeal>>> getUserMealsForDay(String username, long dateMillis);

    LiveData<Result<List<Meal>>> getMealCatalog();

    LiveData<Result<Meal>> addMealToCatalog(Meal meal);

    LiveData<Result<Void>> deleteMealFromCatalog(Meal meal);

    LiveData<Result<Void>> logMeal(Meal meal, MealCategory category, String username, long dateMillis);

    LiveData<Result<Void>> deleteLoggedMeal(UserMeal userMeal);
}
