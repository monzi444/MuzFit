package com.example.muzfit.source.dashboard;

import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.Workout;
import com.example.muzfit.service.MuzFitApiService;
import com.example.muzfit.service.dto.AllenamentoDto;
import com.example.muzfit.service.dto.ApiMapper;
import com.example.muzfit.service.dto.PastoUtenteDto;
import com.example.muzfit.service.dto.UtenteDto;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;

import java.util.List;

public class DashboardApiDataSource extends BaseDashboardDataSource {

    private final MuzFitApiService apiService;

    public DashboardApiDataSource(MuzFitApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void fetchUsers(DataSourceCallback<List<User>> callback) {
        RetrofitCallbacks.enqueue(apiService.getUtenti(), new DataSourceCallback<List<UtenteDto>>() {
            @Override
            public void onSuccess(List<UtenteDto> data) {
                callback.onSuccess(ApiMapper.toUsers(data));
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

    @Override
    public void fetchWorkouts(DataSourceCallback<List<Workout>> callback) {
        RetrofitCallbacks.enqueue(apiService.getAllenamenti(), new DataSourceCallback<List<AllenamentoDto>>() {
            @Override
            public void onSuccess(List<AllenamentoDto> data) {
                callback.onSuccess(ApiMapper.toWorkouts(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
