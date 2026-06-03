package com.example.muzfit.ui.training.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.repository.training.ITrainingRepository;
import com.example.muzfit.utils.Constants;

import java.util.List;

public class TrainingViewModel extends ViewModel {

    private final ITrainingRepository repository;

    public TrainingViewModel(ITrainingRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<List<Exercise>>> searchExerciseCatalog(String query, String bodyPart) {
        return repository.searchExerciseCatalog(query, bodyPart);
    }

    public LiveData<Result<List<WorkoutRoutine>>> getRoutines(String username) {
        return repository.getRoutines(username);
    }

    public LiveData<Result<Void>> saveRoutine(WorkoutRoutine routine, String username) {
        return repository.saveRoutine(routine, username);
    }

    public LiveData<Result<Void>> deleteRoutine(String routineName, String username) {
        return repository.deleteRoutine(routineName, username);
    }

    public LiveData<Result<List<WorkoutRoutine>>> getRoutinesForDefaultUser() {
        return repository.getRoutines(Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<Void>> saveRoutineForDefaultUser(WorkoutRoutine routine) {
        return repository.saveRoutine(routine, Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<Void>> deleteRoutineForDefaultUser(String routineName) {
        return repository.deleteRoutine(routineName, Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<List<Workout>>> getWorkouts(String username) {
        return repository.getWorkouts(username);
    }

    public LiveData<Result<List<Workout>>> getWorkoutsForDefaultUser() {
        return repository.getWorkouts(Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<Workout>> getWorkout(int workoutId, String username) {
        return repository.getWorkout(workoutId, username);
    }

    public LiveData<Result<Void>> saveWorkout(Workout workout) {
        return repository.saveWorkout(workout);
    }

    public LiveData<Result<Void>> deleteWorkout(int workoutId, String username) {
        return repository.deleteWorkout(workoutId, username);
    }

    public LiveData<Result<List<Exercise>>> getExercises() {
        return repository.getExercises();
    }

    public LiveData<Result<List<Exercise>>> searchExercises(String query) {
        return repository.searchExercises(query);
    }

    public LiveData<Result<List<WorkoutExercise>>> getWorkoutExercises(int workoutId, String username) {
        return repository.getWorkoutExercises(workoutId, username);
    }

    public LiveData<Result<Void>> addWorkoutExercise(WorkoutExercise workoutExercise) {
        return repository.addWorkoutExercise(workoutExercise);
    }

    public LiveData<Result<List<ExerciseSet>>> getExerciseSets(int workoutId, String username, int exerciseId) {
        return repository.getExerciseSets(workoutId, username, exerciseId);
    }

    public LiveData<Result<Void>> saveExerciseSet(ExerciseSet exerciseSet) {
        return repository.saveExerciseSet(exerciseSet);
    }
}
