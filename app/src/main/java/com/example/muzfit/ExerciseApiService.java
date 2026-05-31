package com.example.muzfit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExerciseApiService {
    @GET("exercises")
    Call<ExerciseResponse> getAllExercises(
            @Query("limit") int limit
    );

    @GET("exercises")
    Call<ExerciseResponse> getExercisesByName(
            @Query("name") String name,
            @Query("bodyParts") String bodyParts,
            @Query("limit") int limit
    );
}
