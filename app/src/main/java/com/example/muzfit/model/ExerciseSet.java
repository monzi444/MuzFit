package com.example.muzfit.model;

public class ExerciseSet {

    private int id;
    private int repetitions;
    private int weight;
    private int workoutId;
    private String username = "";
    private int exerciseId;

    public ExerciseSet() {
    }

    public ExerciseSet(int id, int repetitions, int weight,
                       int workoutId, String username, int exerciseId) {
        this.id = id;
        this.repetitions = repetitions;
        this.weight = weight;
        this.workoutId = workoutId;
        this.username = username;
        this.exerciseId = exerciseId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
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
