package com.example.muzfit.ui.diet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.utils.Constants;

import java.util.List;

public class DietViewModel extends ViewModel {

    private final IDietRepository repository;

    public DietViewModel(IDietRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<List<Meal>>> getMeals() {
        return repository.getMeals();
    }

    public LiveData<Result<List<UserMeal>>> getUserMeals(String username, long dateMillis) {
        return repository.getUserMeals(username, dateMillis);
    }

    public LiveData<Result<List<UserMeal>>> getUserMealsForToday() {
        return repository.getUserMeals(Constants.DEFAULT_USERNAME, System.currentTimeMillis());
    }

    public LiveData<Result<Void>> logMeal(UserMeal userMeal) {
        return repository.logMeal(userMeal);
    }

    public LiveData<Result<Meal>> addMeal(Meal meal) {
        return repository.addMeal(meal);
    }
}
