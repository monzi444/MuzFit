package com.example.muzfit.source.common;

import com.example.muzfit.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class RetrofitCallbacks {

    private RetrofitCallbacks() {
    }

    public static <T> void enqueue(Call<T> call, DataSourceCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    T body = response.body();
                    if (body != null) {
                        callback.onSuccess(body);
                    } else {
                        callback.onError(Constants.ERROR_NETWORK);
                    }
                } else {
                    callback.onError(Constants.ERROR_NETWORK);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onError(Constants.ERROR_NETWORK);
            }
        });
    }
}
