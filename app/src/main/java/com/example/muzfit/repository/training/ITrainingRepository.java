package com.example.muzfit.repository.training;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;

import java.util.List;

public interface ITrainingRepository {

    LiveData<Result<List<Workout>>> getWorkouts(String username);

    LiveData<Result<Workout>> getWorkout(int workoutId, String username);

    LiveData<Result<Void>> saveWorkout(Workout workout);

    LiveData<Result<Void>> deleteWorkout(int workoutId, String username);

    LiveData<Result<List<Exercise>>> getExercises();

    LiveData<Result<List<Exercise>>> searchExercises(String query);

    LiveData<Result<List<WorkoutExercise>>> getWorkoutExercises(int workoutId, String username);

    LiveData<Result<Void>> addWorkoutExercise(WorkoutExercise workoutExercise);

    LiveData<Result<List<ExerciseSet>>> getExerciseSets(int workoutId, String username, int exerciseId);

    LiveData<Result<Void>> saveExerciseSet(ExerciseSet exerciseSet);
}
