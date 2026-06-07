package com.example.muzfit.repository.training;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.training.BaseTrainingDataSource;
import com.example.muzfit.source.training.catalog.BaseExerciseCatalogDataSource;
import com.example.muzfit.source.training.firebase.BaseTrainingFirebaseDataSource;
import com.example.muzfit.source.firebase.FirestoreSyncDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrainingRepository implements ITrainingRepository {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final BaseTrainingDataSource trainingApiDataSource;
    private final BaseExerciseCatalogDataSource exerciseCatalogDataSource;
    private final BaseTrainingFirebaseDataSource trainingFirebaseDataSource;

    private MuzFitDao localDao;
    private Future<?> seedFuture;

    public TrainingRepository(BaseTrainingDataSource trainingApiDataSource,
                              BaseExerciseCatalogDataSource exerciseCatalogDataSource,
                              BaseTrainingFirebaseDataSource trainingFirebaseDataSource) {
        this.trainingApiDataSource = trainingApiDataSource;
        this.exerciseCatalogDataSource = exerciseCatalogDataSource;
        this.trainingFirebaseDataSource = trainingFirebaseDataSource;
    }

    public void setLocalDatabase(MuzFitDatabase database) {
        if (database != null) {
            localDao = database.muzFitDao();
        }
    }

    public void setSeedFuture(Future<?> seedFuture) {
        this.seedFuture = seedFuture;
    }

    private void awaitSeedIfNeeded() {
        if (seedFuture != null) {
            try {
                seedFuture.get();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public LiveData<Result<List<Exercise>>> searchExerciseCatalog(String query, String bodyPart) {
        MutableLiveData<Result<List<Exercise>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        String normalizedQuery = query != null ? query.toLowerCase(Locale.getDefault()).trim() : "";
        
        // CHIAMATA ALL'API ONLINE (ExerciseDB)
        exerciseCatalogDataSource.searchExercises(
                normalizedQuery,
                bodyPart,
                Constants.EXERCISE_CATALOG_SEARCH_LIMIT,
                new DataSourceCallback<List<Exercise>>() {
                    @Override
                    public void onSuccess(List<Exercise> data) {
                        if (data != null) {
                            for (Exercise e : data) {
                                e.syncLists();
                            }
                        }
                        liveData.postValue(new Result.Success<>(sortByRelevance(data, normalizedQuery)));
                    }

                    @Override
                    public void onError(String message) {
                        // In caso di errore dell'API, non facciamo fallback sul DB locale 
                        // per la ricerca del catalogo, per evitare confusione.
                        liveData.postValue(new Result.Error<>(message));
                    }
                }
        );
        return liveData;
    }

    @Override
    public LiveData<Result<List<WorkoutRoutine>>> getRoutines() {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<List<WorkoutRoutine>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());

        // First load from local DB to be fast
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                awaitSeedIfNeeded();
                liveData.postValue(new Result.Success<>(buildRoutinesFromDb(currentUid)));
            });
        } else {
            liveData.postValue(new Result.Error<>(Constants.ERROR_DATABASE));
        }

        // Then sync from Firebase in background
        trainingFirebaseDataSource.fetchRoutines(currentUid, new DataSourceCallback<List<WorkoutRoutine>>() {
            @Override
            public void onSuccess(List<WorkoutRoutine> firebaseRoutines) {
                if (firebaseRoutines != null) {
                    EXECUTOR.execute(() -> {
                        if (localDao != null) {
                            List<Workout> localWorkouts = localDao.getWorkouts(currentUid);
                            boolean updated = false;

                            // 1. Sync routines from Firebase to local DB
                            for (WorkoutRoutine fr : firebaseRoutines) {
                                Workout targetLocal = null;
                                for (Workout lw : localWorkouts) {
                                    if (fr.getName().equals(lw.getDescription())) {
                                        targetLocal = lw;
                                        break;
                                    }
                                }
                                
                                if (targetLocal == null) {
                                    // Add new routine from cloud
                                    int workoutId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                                    localDao.insertExercises(fr.getExercises());
                                    Workout workout = new Workout(workoutId, System.currentTimeMillis(), fr.getName(), currentUid);
                                    localDao.insertWorkout(workout);
                                    List<WorkoutExercise> wes = new ArrayList<>();
                                    for (Exercise e : fr.getExercises()) {
                                        wes.add(new WorkoutExercise(0, workoutId, currentUid, e.getId()));
                                    }
                                    localDao.insertWorkoutExercises(wes);
                                    updated = true;
                                } else {
                                    // Check if existing routine has different exercises
                                    List<WorkoutExercise> localWes = localDao.getWorkoutExercises(targetLocal.getId(), currentUid);
                                    if (areExercisesDifferent(localWes, fr.getExercises())) {
                                        // Update local exercises for this routine
                                        localDao.deleteExerciseSets(targetLocal.getId(), currentUid);
                                        localDao.deleteWorkoutExercises(targetLocal.getId(), currentUid);
                                        
                                        localDao.insertExercises(fr.getExercises());
                                        List<WorkoutExercise> newWes = new ArrayList<>();
                                        for (Exercise e : fr.getExercises()) {
                                            newWes.add(new WorkoutExercise(0, targetLocal.getId(), currentUid, e.getId()));
                                        }
                                        localDao.insertWorkoutExercises(newWes);
                                        updated = true;
                                    }
                                }
                            }

                            // 2. Remove routines from local DB that are NOT in Firebase
                            for (Workout lw : localWorkouts) {
                                boolean foundInFirebase = false;
                                for (WorkoutRoutine fr : firebaseRoutines) {
                                    if (lw.getDescription() != null && lw.getDescription().equals(fr.getName())) {
                                        foundInFirebase = true;
                                        break;
                                    }
                                }
                                
                                // Only delete if it's not a local-only routine (like those from seeder if applicable)
                                // or simply follow the "Firebase is the source of truth" rule.
                                if (!foundInFirebase) {
                                    localDao.deleteExerciseSets(lw.getId(), currentUid);
                                    localDao.deleteWorkoutExercises(lw.getId(), currentUid);
                                    localDao.deleteWorkout(lw);
                                    updated = true;
                                }
                            }

                            if (updated) {
                                liveData.postValue(new Result.Success<>(buildRoutinesFromDb(currentUid)));
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                // Ignore background sync error for now
            }
        });

        return liveData;
    }

    private void loadRoutinesFromDb(MutableLiveData<Result<List<WorkoutRoutine>>> liveData) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                awaitSeedIfNeeded();
                liveData.postValue(new Result.Success<>(buildRoutinesFromDb(currentUid)));
            });
        } else {
            liveData.postValue(new Result.Error<>(Constants.ERROR_DATABASE));
        }
    }

    private List<WorkoutRoutine> buildRoutinesFromDb(String uid) {
        List<WorkoutRoutine> routines = new ArrayList<>();
        List<Workout> workouts = localDao.getWorkouts(uid);
        for (Workout workout : workouts) {
            List<WorkoutExercise> wes = localDao.getWorkoutExercises(workout.getId(), uid);
            List<Exercise> exercises = new ArrayList<>();
            for (WorkoutExercise we : wes) {
                Exercise e = localDao.getExercise(we.getExerciseId());
                if (e != null) exercises.add(e);
            }
            String name = workout.getDescription() != null && !workout.getDescription().isEmpty()
                    ? workout.getDescription() : "Allenamento " + workout.getId();
            routines.add(new WorkoutRoutine(name, exercises));
        }
        return routines;
    }

    @Override
    public LiveData<Result<Void>> saveRoutine(WorkoutRoutine routine, String oldName) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                try {
                    awaitSeedIfNeeded();
                    RepositorySupport.ensureLocalUser(localDao, currentUid);
                    
                    // 1. Identify the workout to update
                    List<Workout> existingWorkouts = localDao.getWorkouts(currentUid);
                    Workout targetWorkout = null;
                    
                    // Use oldName if provided to find the existing entry
                    String lookupName = (oldName != null && !oldName.isEmpty()) ? oldName : routine.getName();
                    
                    for (Workout w : existingWorkouts) {
                        if (lookupName.equals(w.getDescription())) {
                            targetWorkout = w;
                            break;
                        }
                    }

                    int workoutId;
                    if (targetWorkout != null) {
                        // Edit mode: reuse existing ID, clear old exercises, and update description (name)
                        workoutId = targetWorkout.getId();
                        
                        // Update the name in the Workout table if it changed
                        if (!routine.getName().equals(targetWorkout.getDescription())) {
                            targetWorkout.setDescription(routine.getName());
                            localDao.insertWorkout(targetWorkout); // Update existing record
                        }
                        
                        localDao.deleteExerciseSets(workoutId, currentUid);
                        localDao.deleteWorkoutExercises(workoutId, currentUid);
                    } else {
                        // Create mode: new ID
                        workoutId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                        Workout workout = new Workout(workoutId, System.currentTimeMillis(), routine.getName(), currentUid);
                        localDao.insertWorkout(workout);
                    }
                    
                    // 2. Save/Update exercises details
                    localDao.insertExercises(routine.getExercises());

                    // 3. Link exercises to workout
                    List<WorkoutExercise> wes = new ArrayList<>();
                    for (Exercise e : routine.getExercises()) {
                        wes.add(new WorkoutExercise(0, workoutId, currentUid, e.getId()));
                    }
                    localDao.insertWorkoutExercises(wes);

                    // Sync with Firebase
                    if (oldName != null && !oldName.isEmpty() && !oldName.equals(routine.getName())) {
                        // Name changed: delete old and save new on Firebase
                        trainingFirebaseDataSource.deleteRoutine(oldName, currentUid, new DataSourceCallback<Void>() {
                            @Override public void onSuccess(Void data) {}
                            @Override public void onError(String message) {}
                        });
                    }

                    trainingFirebaseDataSource.saveRoutine(routine, currentUid, new DataSourceCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                    
                    liveData.postValue(new Result.Success<>(null));
                } catch (Exception e) {
                    liveData.postValue(new Result.Error<>(e.getMessage()));
                }
            });
        } else {
            liveData.postValue(new Result.Error<>(Constants.ERROR_DATABASE));
        }
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> deleteRoutine(String routineName) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                if (localDao != null) {
                    List<Workout> workouts = localDao.getWorkouts(currentUid);
                    for (Workout w : workouts) {
                        if (routineName.equals(w.getDescription())) {
                            localDao.deleteExerciseSets(w.getId(), currentUid);
                            localDao.deleteWorkoutExercises(w.getId(), currentUid);
                            localDao.deleteWorkout(w);
                        }
                    }
                }
                
                // Also call Firebase if needed
                trainingFirebaseDataSource.deleteRoutine(routineName, currentUid, new DataSourceCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        liveData.postValue(new Result.Success<>(null));
                    }

                    @Override
                    public void onError(String message) {
                        // Even if firebase fails, we consider it a success if local worked, 
                        // or we can report error. Let's at least post success for now 
                        // as local is the primary source for the user.
                        liveData.postValue(new Result.Success<>(null));
                    }
                });
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });

        return liveData;
    }

    @Override
    public LiveData<Result<List<Workout>>> getWorkouts() {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<List<Workout>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());

        // First load from local DB
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                awaitSeedIfNeeded();
                liveData.postValue(new Result.Success<>(localDao.getWorkouts(currentUid)));
            });
        }

        // Sync from Firebase
        trainingFirebaseDataSource.fetchWorkouts(currentUid, new DataSourceCallback<List<FirestoreSyncDataSource.WorkoutWithDetails>>() {
            @Override
            public void onSuccess(List<FirestoreSyncDataSource.WorkoutWithDetails> firebaseWorkouts) {
                if (firebaseWorkouts != null) {
                    EXECUTOR.execute(() -> {
                        if (localDao != null) {
                            for (FirestoreSyncDataSource.WorkoutWithDetails fw : firebaseWorkouts) {
                                // Simple sync: insert/update local records
                                localDao.insertWorkout(fw.workout);
                                
                                // Exercises and sets
                                List<WorkoutExercise> wes = new ArrayList<>();
                                for (FirestoreSyncDataSource.ExerciseWithSets ews : fw.exercises) {
                                    localDao.insertExercise(ews.exercise);
                                    wes.add(new WorkoutExercise(0, fw.workout.getId(), currentUid, ews.exercise.getId()));
                                    
                                    localDao.deleteExerciseSets(fw.workout.getId(), currentUid, ews.exercise.getId());
                                    localDao.insertExerciseSets(ews.sets);
                                }
                                localDao.deleteWorkoutExercises(fw.workout.getId(), currentUid);
                                localDao.insertWorkoutExercises(wes);
                            }
                            liveData.postValue(new Result.Success<>(localDao.getWorkouts(currentUid)));
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
            }
        });

        return liveData;
    }

    @Override
    public LiveData<Result<Workout>> getWorkout(int workoutId) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<Workout>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingApiDataSource.fetchWorkouts(new DataSourceCallback<List<Workout>>() {
            @Override
            public void onSuccess(List<Workout> data) {
                for (Workout workout : data) {
                    if (workout.getId() == workoutId && currentUid.equals(workout.getUid())) {
                        liveData.postValue(new Result.Success<>(workout));
                        return;
                    }
                }
                liveData.postValue(new Result.Error<>(Constants.ERROR_WORKOUT_NOT_FOUND));
            }

            @Override
            public void onError(String message) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> {
                        awaitSeedIfNeeded();
                        Workout workout = localDao.getWorkout(workoutId, currentUid);
                        if (workout != null) {
                            liveData.postValue(new Result.Success<>(workout));
                        } else {
                            liveData.postValue(new Result.Error<>(message));
                        }
                    });
                } else {
                    liveData.postValue(new Result.Error<>(message));
                }
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> saveWorkout(Workout workout) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<Void>> deleteWorkout(int workoutId) {
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
                if (localDao != null) {
                    EXECUTOR.execute(() -> {
                        awaitSeedIfNeeded();
                        liveData.postValue(new Result.Success<>(localDao.getExercises()));
                    });
                } else {
                    liveData.postValue(new Result.Error<>(message));
                }
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<Exercise>>> searchExercises(String query) {
        // Forza l'uso dell'API esterna ExerciseDB per la ricerca testuale
        return searchExerciseCatalog(query, null);
    }

    @Override
    public LiveData<Result<List<WorkoutExercise>>> getWorkoutExercises(int workoutId) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<List<WorkoutExercise>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingApiDataSource.fetchWorkoutExercises(new DataSourceCallback<List<WorkoutExercise>>() {
            @Override
            public void onSuccess(List<WorkoutExercise> data) {
                List<WorkoutExercise> filtered = new ArrayList<>();
                for (WorkoutExercise workoutExercise : data) {
                    if (workoutExercise.getWorkoutId() == workoutId
                            && currentUid.equals(workoutExercise.getUid())) {
                        filtered.add(workoutExercise);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> {
                        awaitSeedIfNeeded();
                        liveData.postValue(new Result.Success<>(
                                localDao.getWorkoutExercises(workoutId, currentUid)
                        ));
                    });
                } else {
                    liveData.postValue(new Result.Error<>(message));
                }
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> addWorkoutExercise(WorkoutExercise workoutExercise) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<List<ExerciseSet>>> getExerciseSets(int workoutId, int exerciseId) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<List<ExerciseSet>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        trainingApiDataSource.fetchExerciseSets(new DataSourceCallback<List<ExerciseSet>>() {
            @Override
            public void onSuccess(List<ExerciseSet> data) {
                List<ExerciseSet> filtered = new ArrayList<>();
                for (ExerciseSet exerciseSet : data) {
                    if (exerciseSet.getWorkoutId() == workoutId
                            && currentUid.equals(exerciseSet.getUid())
                            && String.valueOf(exerciseId).equals(exerciseSet.getExerciseId())) {
                        filtered.add(exerciseSet);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> {
                        awaitSeedIfNeeded();
                        liveData.postValue(new Result.Success<>(
                                localDao.getExerciseSets(workoutId, currentUid, String.valueOf(exerciseId))
                        ));
                    });
                } else {
                    liveData.postValue(new Result.Error<>(message));
                }
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> saveExerciseSet(ExerciseSet exerciseSet) {
        return RepositorySupport.notSupported();
    }

    private boolean areExercisesDifferent(List<WorkoutExercise> localWes, List<Exercise> firebaseExercises) {
        if (localWes.size() != firebaseExercises.size()) return true;
        for (int i = 0; i < localWes.size(); i++) {
            if (!localWes.get(i).getExerciseId().equals(firebaseExercises.get(i).getId())) {
                return true;
            }
        }
        return false;
    }

    private List<Exercise> sortByRelevance(List<Exercise> exercises, String normalizedQuery) {
        if (exercises == null) {
            return new ArrayList<>();
        }
        List<Exercise> sorted = new ArrayList<>(exercises);
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
