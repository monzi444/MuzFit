package com.example.muzfit.service;

import static com.example.muzfit.utils.Constants.SAL_QUERY_FIELDS;
import static com.example.muzfit.utils.Constants.SAL_QUERY_LANGS;
import static com.example.muzfit.utils.Constants.SAL_QUERY_PAGE_SIZE;
import static com.example.muzfit.utils.Constants.SAL_QUERY_Q;
import static com.example.muzfit.utils.Constants.SAL_SEARCH_PATH;

import com.example.muzfit.service.dto.openfoodfacts.SearchALiciousSearchResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Full-text food search via Search-a-licious (recommended by Open Food Facts).
 * Product Opener API v3 does not expose {@code /api/v3/search}; see OFF API docs.
 */
public interface SearchALiciousApiService {

    @GET(SAL_SEARCH_PATH)
    Call<SearchALiciousSearchResponseDto> search(
            @Query(SAL_QUERY_Q) String query,
            @Query(SAL_QUERY_PAGE_SIZE) int pageSize,
            @Query(SAL_QUERY_LANGS) String langs,
            @Query(SAL_QUERY_FIELDS) String fields
    );
}
