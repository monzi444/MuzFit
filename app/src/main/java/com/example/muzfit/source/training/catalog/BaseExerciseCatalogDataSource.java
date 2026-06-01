package com.example.muzfit.source.training.catalog;

import com.example.muzfit.model.ExerciseDB;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseExerciseCatalogDataSource {

    public abstract void searchExercises(String name, String bodyPart, int limit,
                                         DataSourceCallback<List<ExerciseDB>> callback);
}
