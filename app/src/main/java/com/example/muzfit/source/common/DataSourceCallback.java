package com.example.muzfit.source.common;

public interface DataSourceCallback<T> {

    void onSuccess(T data);

    void onError(String message);
}
