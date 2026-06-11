package com.example.muzfit.source.dashboard;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.service.dto.AllenamentoEsercizioDto;
import com.example.muzfit.service.MuzFitApiService;
import com.example.muzfit.service.dto.AllenamentoDto;
import com.example.muzfit.service.dto.ApiMapper;
import com.example.muzfit.service.dto.PastoDto;
import com.example.muzfit.service.dto.PastoUtenteDto;
import com.example.muzfit.service.dto.PesoDto;
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

    @Override
    public void fetchWorkoutExercises(DataSourceCallback<List<WorkoutExercise>> callback) {
        RetrofitCallbacks.enqueue(apiService.getAllenamentiEsercizio(), new DataSourceCallback<List<AllenamentoEsercizioDto>>() {
            @Override
            public void onSuccess(List<AllenamentoEsercizioDto> data) {
                callback.onSuccess(ApiMapper.toWorkoutExercises(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void fetchWeightEntries(DataSourceCallback<List<WeightEntry>> callback) {
        RetrofitCallbacks.enqueue(apiService.getPesi(), new DataSourceCallback<List<PesoDto>>() {
            @Override
            public void onSuccess(List<PesoDto> data) {
                callback.onSuccess(ApiMapper.toWeightEntries(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
