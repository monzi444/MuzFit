package com.example.muzfit.source.training.firebase;

import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.utils.Constants;

import java.util.List;

/**
 * Stub until Firestore/Realtime Database is implemented.
 */
public class TrainingFirebaseDataSource extends BaseTrainingFirebaseDataSource {

    @Override
    public void fetchRoutines(String uid, DataSourceCallback<List<WorkoutRoutine>> callback) {
        callback.onError(Constants.ERROR_FIREBASE_NOT_IMPLEMENTED);
    }

    @Override
    public void saveRoutine(WorkoutRoutine routine, String uid, DataSourceCallback<Void> callback) {
        callback.onError(Constants.ERROR_FIREBASE_NOT_IMPLEMENTED);
    }

    @Override
    public void deleteRoutine(String routineName, String uid, DataSourceCallback<Void> callback) {
        callback.onError(Constants.ERROR_FIREBASE_NOT_IMPLEMENTED);
    }
}
