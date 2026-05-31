package com.example.muzfit.repository.diet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.diet.BaseDietDataSource;
import com.example.muzfit.utils.DateParser;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.List;

public class DietRepository implements IDietRepository {

    private final BaseDietDataSource dietDataSource;

    public DietRepository(BaseDietDataSource dietDataSource) {
        this.dietDataSource = dietDataSource;
    }

    @Override
    public LiveData<Result<List<Meal>>> getMeals() {
        MutableLiveData<Result<List<Meal>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        dietDataSource.fetchMeals(new DataSourceCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> data) {
                liveData.postValue(new Result.Success<>(data));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
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
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<Meal>> addMeal(Meal meal) {
        return RepositorySupport.notSupported();
    }
}
