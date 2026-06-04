package com.example.muzfit.source.diet.openfoodfacts;

import static com.example.muzfit.utils.Constants.SAL_SEARCH_FIELDS;
import static com.example.muzfit.utils.Constants.SAL_SEARCH_LANGS;

import com.example.muzfit.model.Meal;
import com.example.muzfit.service.SearchALiciousApiService;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsMapper;
import com.example.muzfit.service.dto.openfoodfacts.SearchALiciousSearchResponseDto;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;
import com.example.muzfit.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;

public class OpenFoodFactsApiDataSource extends BaseOpenFoodFactsDataSource {

    private final SearchALiciousApiService searchApiService;
    private final List<Call<?>> activeCalls = Collections.synchronizedList(new ArrayList<>());
    private volatile int searchRequestId;

    public OpenFoodFactsApiDataSource(SearchALiciousApiService searchApiService) {
        this.searchApiService = searchApiService;
    }

    @Override
    public void searchFoods(String query, int limit, DataSourceCallback<List<Meal>> callback) {
        String normalizedQuery = query != null ? query.trim() : "";
        if (normalizedQuery.isEmpty()) {
            callback.onSuccess(List.of());
            return;
        }

        final int requestId = ++searchRequestId;
        cancelActiveCalls();

        Call<SearchALiciousSearchResponseDto> call = searchApiService.search(
                normalizedQuery,
                limit,
                SAL_SEARCH_LANGS,
                SAL_SEARCH_FIELDS
        );
        trackCall(call);
        RetrofitCallbacks.enqueue(call, new DataSourceCallback<SearchALiciousSearchResponseDto>() {
            @Override
            public void onSuccess(SearchALiciousSearchResponseDto data) {
                untrackCall(call);
                if (requestId != searchRequestId) {
                    return;
                }
                if (data.hasErrors()) {
                    callback.onError(Constants.ERROR_FOOD_SEARCH);
                    return;
                }
                List<Meal> meals = OpenFoodFactsMapper.dedupeMeals(
                        OpenFoodFactsMapper.toMeals(data),
                        limit
                );
                if (meals.isEmpty()) {
                    callback.onError(Constants.ERROR_FOOD_SEARCH_NO_RESULTS);
                } else {
                    callback.onSuccess(meals);
                }
            }

            @Override
            public void onError(String message) {
                untrackCall(call);
                if (requestId != searchRequestId) {
                    return;
                }
                callback.onError(message);
            }
        });
    }

    private void trackCall(Call<?> call) {
        activeCalls.add(call);
    }

    private void untrackCall(Call<?> call) {
        activeCalls.remove(call);
    }

    private void cancelActiveCalls() {
        synchronized (activeCalls) {
            for (Call<?> call : activeCalls) {
                if (!call.isCanceled()) {
                    call.cancel();
                }
            }
            activeCalls.clear();
        }
    }
}
