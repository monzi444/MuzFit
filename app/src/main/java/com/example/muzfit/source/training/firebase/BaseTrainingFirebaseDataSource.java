package com.example.muzfit.source.training.firebase;

import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseTrainingFirebaseDataSource {

    public abstract void fetchRoutines(String username, DataSourceCallback<List<WorkoutRoutine>> callback);

    public abstract void saveRoutine(WorkoutRoutine routine, String username, DataSourceCallback<Void> callback);

    public abstract void deleteRoutine(String routineName, String username, DataSourceCallback<Void> callback);
}
