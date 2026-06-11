package com.example.muzfit.source.diet;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseDietDataSource {

    public abstract void fetchMeals(DataSourceCallback<List<Meal>> callback);

    public abstract void fetchAllUserMeals(DataSourceCallback<List<UserMeal>> callback);
}
