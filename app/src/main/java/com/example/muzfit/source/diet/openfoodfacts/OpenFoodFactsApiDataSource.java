package com.example.muzfit.source.diet.openfoodfacts;

import static com.example.muzfit.utils.Constants.ERROR_FOOD_SEARCH_NO_RESULTS;
import static com.example.muzfit.utils.Constants.OFF_SEARCH_ACTION_PROCESS;
import static com.example.muzfit.utils.Constants.OFF_SEARCH_JSON;
import static com.example.muzfit.utils.Constants.OFF_SEARCH_SIMPLE;

import com.example.muzfit.model.Meal;
import com.example.muzfit.service.OpenFoodFactsApiService;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsMapper;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsSearchResponseDto;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;

import java.util.List;

public class OpenFoodFactsApiDataSource extends BaseOpenFoodFactsDataSource {

    private final OpenFoodFactsApiService apiService;

    public OpenFoodFactsApiDataSource(OpenFoodFactsApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void searchFoods(String query, int limit, DataSourceCallback<List<Meal>> callback) {
        String normalizedQuery = query != null ? query.trim() : "";
        if (normalizedQuery.isEmpty()) {
            callback.onSuccess(List.of());
            return;
        }
        RetrofitCallbacks.enqueue(
                apiService.searchProducts(
                        normalizedQuery,
                        OFF_SEARCH_SIMPLE,
                        OFF_SEARCH_ACTION_PROCESS,
                        OFF_SEARCH_JSON,
                        limit
                ),
                new DataSourceCallback<OpenFoodFactsSearchResponseDto>() {
                    @Override
                    public void onSuccess(OpenFoodFactsSearchResponseDto data) {
                        List<Meal> meals = OpenFoodFactsMapper.toMeals(data);
                        if (meals.isEmpty()) {
                            callback.onError(ERROR_FOOD_SEARCH_NO_RESULTS);
                        } else {
                            callback.onSuccess(meals);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }
        );
    }
}
