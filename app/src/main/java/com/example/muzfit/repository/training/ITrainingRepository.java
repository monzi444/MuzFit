package com.example.muzfit.repository.training;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.model.WorkoutRoutine;

import java.util.List;

public interface ITrainingRepository {

    LiveData<Result<List<Exercise>>> searchExerciseCatalog(String query, String bodyPart);

    LiveData<Result<List<WorkoutRoutine>>> getRoutines();

    LiveData<Result<Void>> saveRoutine(WorkoutRoutine routine);

    LiveData<Result<Void>> deleteRoutine(String routineName);

    LiveData<Result<List<Workout>>> getWorkouts();

    LiveData<Result<Workout>> getWorkout(int workoutId);

    LiveData<Result<Void>> saveWorkout(Workout workout);

    LiveData<Result<Void>> deleteWorkout(int workoutId);

    LiveData<Result<List<Exercise>>> getExercises();

    LiveData<Result<List<Exercise>>> searchExercises(String query);

    LiveData<Result<List<WorkoutExercise>>> getWorkoutExercises(int workoutId);

    LiveData<Result<Void>> addWorkoutExercise(WorkoutExercise workoutExercise);

    LiveData<Result<List<ExerciseSet>>> getExerciseSets(int workoutId, int exerciseId);

    LiveData<Result<Void>> saveExerciseSet(ExerciseSet exerciseSet);
}
