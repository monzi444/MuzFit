package com.example.muzfit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StoicApiService {
    @GET("stoic-quote")
    Call<StoicQuoteResponse> getQuote();
}
