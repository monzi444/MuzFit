package com.example.muzfit.model;

public class WorkoutExercise {

    private int calories;
    private int workoutId;
    private String username = "";
    private int exerciseId;

    public WorkoutExercise() {
    }

    public WorkoutExercise(int calories, int workoutId, String username, int exerciseId) {
        this.calories = calories;
        this.workoutId = workoutId;
        this.username = username;
        this.exerciseId = exerciseId;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }
}
