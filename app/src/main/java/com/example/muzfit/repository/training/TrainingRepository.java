package com.example.muzfit.repository.training;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseDB;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.training.BaseTrainingDataSource;
import com.example.muzfit.source.training.catalog.BaseExerciseCatalogDataSource;
import com.example.muzfit.source.training.firebase.BaseTrainingFirebaseDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TrainingRepository implements ITrainingRepository {

    private final BaseTrainingDataSource trainingApiDataSource;
    private final BaseExerciseCatalogDataSource exerciseCatalogDataSource;
    private final BaseTrainingFirebaseDataSource trainingFirebaseDataSource;

    public TrainingRepository(BaseTrainingDataSource trainingApiDataSource,
                              BaseExerciseCatalogDataSource exerciseCatalogDataSource,
                              BaseTrainingFirebaseDataSource trainingFirebaseDataSource) {
        this.trainingApiDataSource = trainingApiDataSource;
        this.exerciseCatalogDataSource = exerciseCatalogDataSource;
        this.trainingFirebaseDataSource = trainingFirebaseDataSource;
    }

    @Override
    public LiveData<Result<List<ExerciseDB>>> searchExerciseCatalog(String query, String bodyPart) {
        MutableLiveData<Result<List<ExerciseDB>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        String normalizedQuery = query != null ? query.toLowerCase(Locale.getDefault()).trim() : "";
        exerciseCatalogDataSource.searchExercises(
                normalizedQuery,
                bodyPart,
                Constants.EXERCISE_CATALOG_SEARCH_LIMIT,
                new DataSourceCallback<List<ExerciseDB>>() {
                    @Override
                    public void onSuccess(List<ExerciseDB> data) {
                        liveData.postValue(new Result.Success<>(sortByRelevance(data, normalizedQuery)));
                    }

                    @Override
                    public void onError(String message) {
                        liveData.postValue(new Result.Error<>(message));
                    }
                }
        );
        return liveData;
    }

    @Override
    public LiveData<Result<List<WorkoutRoutine>>> getRoutines(String username) {
        MutableLiveData<Result<List<WorkoutRoutine>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingFirebaseDataSource.fetchRoutines(username, new DataSourceCallback<List<WorkoutRoutine>>() {
            @Override
            public void onSuccess(List<WorkoutRoutine> data) {
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
    public LiveData<Result<Void>> saveRoutine(WorkoutRoutine routine, String username) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingFirebaseDataSource.saveRoutine(routine, username, new DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                liveData.postValue(new Result.Success<>(null));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> deleteRoutine(String routineName, String username) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingFirebaseDataSource.deleteRoutine(routineName, username, new DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                liveData.postValue(new Result.Success<>(null));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<Workout>>> getWorkouts(String username) {
        MutableLiveData<Result<List<Workout>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingApiDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
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
        trainingApiDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
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
        trainingApiDataSource.fetchExercises(new DataSourceCallback<List<Exercise>>() {
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
        trainingApiDataSource.fetchExercises(new DataSourceCallback<List<Exercise>>() {
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
        trainingApiDataSource.fetchWorkoutExercises(new DataSourceCallback<List<WorkoutExercise>>() {
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
        trainingApiDataSource.fetchExerciseSets(new DataSourceCallback<List<ExerciseSet>>() {
            @Override
            public void onSuccess(List<ExerciseSet> data) {
                List<ExerciseSet> filtered = new ArrayList<>();
                for (ExerciseSet exerciseSet : data) {
                    if (exerciseSet.getWorkoutId() == workoutId
                            && username.equals(exerciseSet.getUsername())
                            && String.valueOf(exerciseId).equals(exerciseSet.getExerciseId())) {
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

    private List<ExerciseDB> sortByRelevance(List<ExerciseDB> exercises, String normalizedQuery) {
        if (exercises == null) {
            return new ArrayList<>();
        }
        List<ExerciseDB> sorted = new ArrayList<>(exercises);
        if (normalizedQuery.isEmpty()) {
            return sorted;
        }
        Collections.sort(sorted, (e1, e2) -> {
            String name1 = e1.getName().toLowerCase(Locale.getDefault());
            String name2 = e2.getName().toLowerCase(Locale.getDefault());

            boolean starts1 = name1.startsWith(normalizedQuery);
            boolean starts2 = name2.startsWith(normalizedQuery);
            if (starts1 && !starts2) {
                return -1;
            }
            if (!starts1 && starts2) {
                return 1;
            }

            boolean contains1 = name1.contains(normalizedQuery);
            boolean contains2 = name2.contains(normalizedQuery);
            if (contains1 && !contains2) {
                return -1;
            }
            if (!contains1 && contains2) {
                return 1;
            }

            return name1.compareTo(name2);
        });
        return sorted;
    }
}
