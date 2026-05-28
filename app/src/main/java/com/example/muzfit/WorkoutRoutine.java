package com.example.muzfit;

public class WorkoutRoutine {
    private final String name;
    private final int exerciseCount;

    public WorkoutRoutine(String name, int exerciseCount) {
        this.name = name;
        this.exerciseCount = exerciseCount;
    }

    public String getName() {
        return name;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public String getExerciseSummary() {
        if (exerciseCount == 1) {
            return exerciseCount + " exercise";
        }
        return exerciseCount + " exercises";
    }
}
