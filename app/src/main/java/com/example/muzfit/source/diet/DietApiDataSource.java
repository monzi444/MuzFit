package com.example.muzfit.source.diet;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.service.MuzFitApiService;
import com.example.muzfit.service.dto.ApiMapper;
import com.example.muzfit.service.dto.PastoDto;
import com.example.muzfit.service.dto.PastoUtenteDto;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;

import java.util.List;

public class DietApiDataSource extends BaseDietDataSource {

    private final MuzFitApiService apiService;

    public DietApiDataSource(MuzFitApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void fetchMeals(DataSourceCallback<List<Meal>> callback) {
        RetrofitCallbacks.enqueue(apiService.getPasti(), new DataSourceCallback<List<PastoDto>>() {
            @Override
            public void onSuccess(List<PastoDto> data) {
                callback.onSuccess(ApiMapper.toMeals(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void fetchAllUserMeals(DataSourceCallback<List<UserMeal>> callback) {
        RetrofitCallbacks.enqueue(apiService.getPastiUtente(), new DataSourceCallback<List<PastoUtenteDto>>() {
            @Override
            public void onSuccess(List<PastoUtenteDto> data) {
                callback.onSuccess(ApiMapper.toUserMeals(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
