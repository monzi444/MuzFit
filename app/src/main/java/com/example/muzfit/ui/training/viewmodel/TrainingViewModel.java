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

import java.util.List;

public class TrainingViewModel extends ViewModel {

    private final ITrainingRepository repository;

    public TrainingViewModel(ITrainingRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<List<Exercise>>> searchExerciseCatalog(String query, String bodyPart) {
        return repository.searchExerciseCatalog(query, bodyPart);
    }

    public LiveData<Result<List<WorkoutRoutine>>> getRoutines() {
        return repository.getRoutines();
    }

    public LiveData<Result<Void>> saveRoutine(WorkoutRoutine routine) {
        return repository.saveRoutine(routine);
    }

    public LiveData<Result<Void>> deleteRoutine(String routineName) {
        return repository.deleteRoutine(routineName);
    }

    public LiveData<Result<List<Workout>>> getWorkouts() {
        return repository.getWorkouts();
    }

    public LiveData<Result<Workout>> getWorkout(int workoutId) {
        return repository.getWorkout(workoutId);
    }

    public LiveData<Result<Void>> saveWorkout(Workout workout) {
        return repository.saveWorkout(workout);
    }

    public LiveData<Result<Void>> deleteWorkout(int workoutId) {
        return repository.deleteWorkout(workoutId);
    }

    public LiveData<Result<List<Exercise>>> getExercises() {
        return repository.getExercises();
    }

    public LiveData<Result<List<Exercise>>> searchExercises(String query) {
        return repository.searchExercises(query);
    }

    public LiveData<Result<List<WorkoutExercise>>> getWorkoutExercises(int workoutId) {
        return repository.getWorkoutExercises(workoutId);
    }

    public LiveData<Result<Void>> addWorkoutExercise(WorkoutExercise workoutExercise) {
        return repository.addWorkoutExercise(workoutExercise);
    }

    public LiveData<Result<List<ExerciseSet>>> getExerciseSets(int workoutId, int exerciseId) {
        return repository.getExerciseSets(workoutId, exerciseId);
    }

    public LiveData<Result<Void>> saveExerciseSet(ExerciseSet exerciseSet) {
        return repository.saveExerciseSet(exerciseSet);
    }
}
