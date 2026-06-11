package com.example.muzfit.source.diet.openfoodfacts;

import com.example.muzfit.model.Meal;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseOpenFoodFactsDataSource {

    public abstract void searchFoods(String query, int limit, DataSourceCallback<List<Meal>> callback);
}
