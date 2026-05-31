package com.example.muzfit.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Result;

public final class RepositorySupport {

    private RepositorySupport() {
    }

    public static <T> LiveData<Result<T>> notSupported() {
        MutableLiveData<Result<T>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Error<>(Constants.ERROR_NOT_SUPPORTED));
        return liveData;
    }
}
