package com.example.muzfit.source.training.firebase;

import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.firebase.FirestoreSyncDataSource;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

/**
 * Stub until Firestore/Realtime Database is implemented.
 */
public class TrainingFirebaseDataSource extends BaseTrainingFirebaseDataSource {

    private final FirestoreSyncDataSource firestoreSyncDataSource;

    public TrainingFirebaseDataSource(FirestoreSyncDataSource firestoreSyncDataSource) {
        this.firestoreSyncDataSource = firestoreSyncDataSource;
    }

    @Override
    public void fetchRoutines(String uid, DataSourceCallback<List<WorkoutRoutine>> callback) {
        firestoreSyncDataSource.fetchRoutines(uid, callback);
    }

    @Override
    public void saveRoutine(WorkoutRoutine routine, String uid, DataSourceCallback<Void> callback) {
        if (!firestoreSyncDataSource.canSync(uid)) {
            callback.onSuccess(null);
            return;
        }
        firestoreSyncDataSource.saveRoutine(uid, routine);
        callback.onSuccess(null);
    }

    @Override
    public void deleteRoutine(String routineName, String uid, DataSourceCallback<Void> callback) {
        if (!firestoreSyncDataSource.canSync(uid)) {
            callback.onSuccess(null);
            return;
        }
        firestoreSyncDataSource.deleteRoutine(uid, routineName);
        callback.onSuccess(null);
    }
}
