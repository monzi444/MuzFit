package com.example.muzfit.ui.diet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Food;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DietViewModel extends ViewModel {

    private final IDietRepository repository;
    private final MutableLiveData<List<Food>> foodList = new MutableLiveData<>(new ArrayList<>());
    
    // Shared list of custom meals across fragments
    private static final MutableLiveData<List<Meal>> sharedCustomMeals = new MutableLiveData<>(new ArrayList<>());

    public DietViewModel(IDietRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Food>> getFoodList() {
        return foodList;
    }

    public void addFood(Food food) {
        List<Food> currentList = foodList.getValue();
        if (currentList != null) {
            List<Food> updatedList = new ArrayList<>(currentList);
            updatedList.add(food);
            foodList.setValue(updatedList);
        }
    }

    public LiveData<List<Meal>> getCustomMeals() {
        return sharedCustomMeals;
    }

    public void addCustomMeal(Meal meal) {
        List<Meal> currentList = sharedCustomMeals.getValue();
        List<Meal> updatedList = new ArrayList<>();
        if (currentList != null) {
            updatedList.addAll(currentList);
        }
        updatedList.add(meal);
        sharedCustomMeals.setValue(updatedList);
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

    public LiveData<Result<Void>> deleteMeal(int id) {
        return repository.deleteMeal(id);
    }
}
