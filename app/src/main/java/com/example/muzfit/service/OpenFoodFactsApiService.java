package com.example.muzfit.service;

import static com.example.muzfit.utils.Constants.OFF_QUERY_ACTION;
import static com.example.muzfit.utils.Constants.OFF_QUERY_JSON;
import static com.example.muzfit.utils.Constants.OFF_QUERY_PAGE_SIZE;
import static com.example.muzfit.utils.Constants.OFF_QUERY_SEARCH_SIMPLE;
import static com.example.muzfit.utils.Constants.OFF_QUERY_SEARCH_TERMS;
import static com.example.muzfit.utils.Constants.OFF_SEARCH_PATH;

import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsSearchResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenFoodFactsApiService {

    @GET(OFF_SEARCH_PATH)
    Call<OpenFoodFactsSearchResponseDto> searchProducts(
            @Query(OFF_QUERY_SEARCH_TERMS) String searchTerms,
            @Query(OFF_QUERY_SEARCH_SIMPLE) int searchSimple,
            @Query(OFF_QUERY_ACTION) String action,
            @Query(OFF_QUERY_JSON) int json,
            @Query(OFF_QUERY_PAGE_SIZE) int pageSize
    );
}
