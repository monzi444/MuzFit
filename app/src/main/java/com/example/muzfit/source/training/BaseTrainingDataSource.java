package com.example.muzfit.source.training;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseTrainingDataSource {

    public abstract void fetchWorkouts(DataSourceCallback<List<Workout>> callback);

    public abstract void fetchExercises(DataSourceCallback<List<Exercise>> callback);

    public abstract void fetchWorkoutExercises(DataSourceCallback<List<WorkoutExercise>> callback);

    public abstract void fetchExerciseSets(DataSourceCallback<List<ExerciseSet>> callback);
}
