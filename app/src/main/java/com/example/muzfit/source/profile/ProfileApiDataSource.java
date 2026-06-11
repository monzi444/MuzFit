package com.example.muzfit.source.profile;

import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.service.MuzFitApiService;
import com.example.muzfit.service.dto.ApiMapper;
import com.example.muzfit.service.dto.PesoDto;
import com.example.muzfit.service.dto.UtenteDto;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;

import java.util.List;

public class ProfileApiDataSource extends BaseProfileDataSource {

    private final MuzFitApiService apiService;

    public ProfileApiDataSource(MuzFitApiService apiService) {
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
