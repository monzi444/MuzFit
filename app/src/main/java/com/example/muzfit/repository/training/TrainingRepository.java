package com.example.muzfit.repository.training;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.training.BaseTrainingDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrainingRepository implements ITrainingRepository {

    private final BaseTrainingDataSource trainingDataSource;

    public TrainingRepository(BaseTrainingDataSource trainingDataSource) {
        this.trainingDataSource = trainingDataSource;
    }

    @Override
    public LiveData<Result<List<Workout>>> getWorkouts(String username) {
        MutableLiveData<Result<List<Workout>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                List<Workout> filtered = new ArrayList<>();
                for (Workout workout : data) {
                    if (username.equals(workout.getUsername())) {
                        filtered.add(workout);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Workout>> getWorkout(int workoutId, String username) {
        MutableLiveData<Result<Workout>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                for (Workout workout : data) {
                    if (workout.getId() == workoutId && username.equals(workout.getUsername())) {
                        liveData.postValue(new Result.Success<>(workout));
                        return;
                    }
                }
                liveData.postValue(new Result.Error<>(Constants.ERROR_WORKOUT_NOT_FOUND));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> saveWorkout(Workout workout) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<Void>> deleteWorkout(int workoutId, String username) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<List<Exercise>>> getExercises() {
        MutableLiveData<Result<List<Exercise>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingDataSource.fetchExercises(new DataSourceCallback<List<Exercise>>() {
            @Override
            public void onSuccess(List<Exercise> data) {
                liveData.postValue(new Result.Success<>(data));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<Exercise>>> searchExercises(String query) {
        MutableLiveData<Result<List<Exercise>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingDataSource.fetchExercises(new DataSourceCallback<List<Exercise>>() {
            @Override
            public void onSuccess(List<Exercise> data) {
                if (query == null || query.trim().isEmpty()) {
                    liveData.postValue(new Result.Success<>(data));
                    return;
                }
                String normalizedQuery = query.trim().toLowerCase(Locale.getDefault());
                List<Exercise> filtered = new ArrayList<>();
                for (Exercise exercise : data) {
                    if (exercise.getName().toLowerCase(Locale.getDefault()).contains(normalizedQuery)) {
                        filtered.add(exercise);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<WorkoutExercise>>> getWorkoutExercises(int workoutId, String username) {
        MutableLiveData<Result<List<WorkoutExercise>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingDataSource.fetchWorkoutExercises(new DataSourceCallback<List<WorkoutExercise>>() {
            @Override
            public void onSuccess(List<WorkoutExercise> data) {
                List<WorkoutExercise> filtered = new ArrayList<>();
                for (WorkoutExercise workoutExercise : data) {
                    if (workoutExercise.getWorkoutId() == workoutId
                            && username.equals(workoutExercise.getUsername())) {
                        filtered.add(workoutExercise);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> addWorkoutExercise(WorkoutExercise workoutExercise) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<List<ExerciseSet>>> getExerciseSets(int workoutId, String username, int exerciseId) {
        MutableLiveData<Result<List<ExerciseSet>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingDataSource.fetchExerciseSets(new DataSourceCallback<List<ExerciseSet>>() {
            @Override
            public void onSuccess(List<ExerciseSet> data) {
                List<ExerciseSet> filtered = new ArrayList<>();
                for (ExerciseSet exerciseSet : data) {
                    if (exerciseSet.getWorkoutId() == workoutId
                            && username.equals(exerciseSet.getUsername())
                            && exerciseSet.getExerciseId() == exerciseId) {
                        filtered.add(exerciseSet);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> saveExerciseSet(ExerciseSet exerciseSet) {
        return RepositorySupport.notSupported();
    }
}
