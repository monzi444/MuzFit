package com.example.muzfit.source.training.firebase;

import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseTrainingFirebaseDataSource {

    public abstract void fetchRoutines(String uid, DataSourceCallback<List<WorkoutRoutine>> callback);

    public abstract void saveRoutine(WorkoutRoutine routine, String uid, DataSourceCallback<Void> callback);

    public abstract void deleteRoutine(String routineName, String uid, DataSourceCallback<Void> callback);
}
