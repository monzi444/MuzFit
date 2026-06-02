package com.example.muzfit.repository.diet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Food;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.diet.BaseDietDataSource;
import com.example.muzfit.utils.DateParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DietRepository implements IDietRepository {

    private final BaseDietDataSource dietDataSource;
    private final MutableLiveData<Result<List<Meal>>> mealsLiveData = new MutableLiveData<>();
    private List<Meal> cachedMeals = new ArrayList<>(Arrays.asList(
            new Meal(1, "Mela", 95, 25, 1, 0, Food.Category.COLAZIONE),
            new Meal(2, "Pasta al pomodoro", 350, 70, 10, 5, Food.Category.PRANZO),
            new Meal(3, "Petto di Pollo", 165, 0, 31, 4, Food.Category.CENA)
    ));

    public DietRepository(BaseDietDataSource dietDataSource) {
        this.dietDataSource = dietDataSource;
    }

    @Override
    public LiveData<Result<List<Meal>>> getMeals() {
        if (mealsLiveData.getValue() == null) {
            mealsLiveData.setValue(new Result.Success<>(cachedMeals));
        }
        return mealsLiveData;
    }

    private void refreshMeals() {
        dietDataSource.fetchMeals(new DataSourceCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> data) {
                // If we want to merge with API data in the future
                // For now we keep our 3 examples as requested
                // cachedMeals = new ArrayList<>(data);
                // mealsLiveData.postValue(new Result.Success<>(cachedMeals));
            }

            @Override
            public void onError(String message) {
                // Keep current cachedMeals
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
                List<UserMeal> filtered = new ArrayList<>();
                for (UserMeal userMeal : data) {
                    if (username.equals(userMeal.getUsername())
                            && DateParser.isSameDay(userMeal.getDateMillis(), dateMillis)) {
                        filtered.add(userMeal);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> logMeal(UserMeal userMeal) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Success<>(null));
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
