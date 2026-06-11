package com.example.muzfit.service;

import com.example.muzfit.model.ExerciseResponse;

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
            @Query("limit") int limit
    );

    @GET("exercises")
    Call<ExerciseResponse> getExercisesByBodyPart(
            @Query("bodyParts") String bodyPart,
            @Query("limit") int limit
    );
}
