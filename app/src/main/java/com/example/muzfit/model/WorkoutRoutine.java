package com.example.muzfit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRoutine implements Serializable {
    private final String name;
    private final List<ExerciseDB> exercises;

    public WorkoutRoutine(String name) {
        this.name = name;
        this.exercises = new ArrayList<>();
    }

    public WorkoutRoutine(String name, List<ExerciseDB> exercises) {
        this.name = name;
        this.exercises = exercises;
    }

    public String getName() {
        return name;
    }

    public List<ExerciseDB> getExercises() {
        return exercises;
    }

    public int getExerciseCount() {
        return exercises.size();
    }

    public String getExerciseSummary() {
        int count = getExerciseCount();
        if (count == 1) {
            return count + " exercise";
        }
        return count + " exercises";
    }
}
