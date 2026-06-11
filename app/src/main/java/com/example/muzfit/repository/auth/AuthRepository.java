package com.example.muzfit.repository.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.source.auth.BaseAuthDataSource;
import com.example.muzfit.source.common.DataSourceCallback;

public class AuthRepository implements IAuthRepository {

    private final BaseAuthDataSource authDataSource;

    public AuthRepository(BaseAuthDataSource authDataSource) {
        this.authDataSource = authDataSource;
    }

    @Override
    public LiveData<Result<Void>> signOut() {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        authDataSource.signOut(new DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                liveData.postValue(new Result.Success<>(null));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<String>> getCurrentUserEmail() {
        MutableLiveData<Result<String>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        authDataSource.fetchCurrentUserEmail(new DataSourceCallback<String>() {
            @Override
            public void onSuccess(String data) {
                liveData.postValue(new Result.Success<>(data));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }
}
