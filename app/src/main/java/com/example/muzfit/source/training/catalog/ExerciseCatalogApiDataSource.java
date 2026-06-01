package com.example.muzfit.source.training.catalog;

import com.example.muzfit.model.ExerciseDB;
import com.example.muzfit.model.ExerciseResponse;
import com.example.muzfit.service.ExerciseApiService;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;
import java.util.ArrayList;
import java.util.List;

public class ExerciseCatalogApiDataSource extends BaseExerciseCatalogDataSource {

    private final ExerciseApiService exerciseApiService;

    public ExerciseCatalogApiDataSource(ExerciseApiService exerciseApiService) {
        this.exerciseApiService = exerciseApiService;
    }

    @Override
    public void searchExercises(String name, String bodyPart, int limit,
                                DataSourceCallback<List<ExerciseDB>> callback) {
        String normalizedName = name != null ? name.toLowerCase().trim() : "";
        RetrofitCallbacks.enqueue(
                exerciseApiService.getExercisesByName(normalizedName, bodyPart, limit),
                new DataSourceCallback<ExerciseResponse>() {
                    @Override
                    public void onSuccess(ExerciseResponse data) {
                        if (data != null && data.getData() != null) {
                            callback.onSuccess(data.getData());
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }
        );
    }
}
