package com.example.muzfit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.muzfit.service.ExerciseApiService;

public class RetrofitClient {
    private static final String BASE_URL = "https://oss.exercisedb.dev/api/v1/";
    private static final String STOIC_BASE_URL = "https://stoic.tekloon.net/";
    private static Retrofit retrofit = null;
    private static Retrofit stoicRetrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getStoicClient() {
        if (stoicRetrofit == null) {
            stoicRetrofit = new Retrofit.Builder()
                    .baseUrl(STOIC_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return stoicRetrofit;
    }

    public static ExerciseApiService getApiService() {
        return getClient().create(ExerciseApiService.class);
    }

    public static StoicApiService getStoicApiService() {
        return getStoicClient().create(StoicApiService.class);
    }
}
