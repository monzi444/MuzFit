package com.example.muzfit.ui.diet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DietViewModel extends ViewModel {

    private final IDietRepository repository;
    private final MutableLiveData<Map<Integer, Meal>> mealsById = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Long> selectedDateMillis = new MutableLiveData<>(System.currentTimeMillis());

    public DietViewModel(IDietRepository repository) {
        this.repository = repository;
    }

    public LiveData<Map<Integer, Meal>> getMealsById() {
        return mealsById;
    }

    public void updateMealsById(List<Meal> meals) {
        Map<Integer, Meal> map = new HashMap<>();
        if (meals != null) {
            for (Meal meal : meals) {
                map.put(meal.getId(), meal);
            }
        }
        mealsById.setValue(map);
    }

    public Meal getMealFor(UserMeal userMeal) {
        Map<Integer, Meal> map = mealsById.getValue();
        if (map == null || userMeal == null) {
            return null;
        }
        return map.get(userMeal.getMealId());
    }

    public void setSelectedDate(long dateMillis) {
        selectedDateMillis.setValue(dateMillis);
    }

    public LiveData<Long> getSelectedDateMillis() {
        return selectedDateMillis;
    }

    public LiveData<Result<List<UserMeal>>> getUserMealsForSelectedDay() {
        return Transformations.switchMap(selectedDateMillis, date -> 
            repository.getUserMealsForDay(Constants.DEFAULT_USERNAME, date)
        );
    }

    public LiveData<Result<List<Meal>>> getMealCatalog() {
        return repository.getMealCatalog();
    }

    public LiveData<Result<Meal>> addMealToCatalog(Meal meal) {
        return repository.addMealToCatalog(meal);
    }

    public LiveData<Result<Void>> deleteMealFromCatalog(Meal meal) {
        return repository.deleteMealFromCatalog(meal);
    }

    public LiveData<Result<Void>> logMeal(Meal meal, MealCategory category, String username, long dateMillis) {
        return repository.logMeal(meal, category, username, dateMillis);
    }

    public LiveData<Result<Void>> logMealForSelectedDay(Meal meal, MealCategory category) {
        Long date = selectedDateMillis.getValue();
        return repository.logMeal(meal, category, Constants.DEFAULT_USERNAME, date != null ? date : System.currentTimeMillis());
    }

    public LiveData<Result<Void>> deleteLoggedMeal(UserMeal userMeal) {
        return repository.deleteLoggedMeal(userMeal);
    }

    public LiveData<Result<List<Meal>>> getFoodSearchResults() {
        return repository.getFoodSearchResults();
    }

    public void searchFoods(String query) {
        repository.searchFoods(query);
    }
}
