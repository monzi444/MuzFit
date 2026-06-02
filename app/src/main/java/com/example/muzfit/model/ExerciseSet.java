package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "ExerciseSet",
        primaryKeys = {
                "id",
                "workoutId",
                "username",
                "exerciseId"
        },
        foreignKeys = @ForeignKey(
                entity = WorkoutExercise.class,
                parentColumns = {
                        "workoutId",
                        "username",
                        "exerciseId"
                },
                childColumns = {
                        "workoutId",
                        "username",
                        "exerciseId"
                }
        ),
        indices = @Index({
                "workoutId",
                "username",
                "exerciseId"
        })
)
public class ExerciseSet {

    private int id;
    private int repetitions;
    private int weight;
    private int workoutId;
    @NonNull
    private String username = "";
    @NonNull
    private String exerciseId = "";

    public ExerciseSet() {
    }

    @Ignore
    public ExerciseSet(int id, int repetitions, int weight,
                       int workoutId, String username, int exerciseId) {
        this(id, repetitions, weight, workoutId, username, String.valueOf(exerciseId));
    }

    @Ignore
    public ExerciseSet(int id, int repetitions, int weight,
                       int workoutId, String username, String exerciseId) {
        this.id = id;
        this.repetitions = repetitions;
        this.weight = weight;
        this.workoutId = workoutId;
        this.username = username != null ? username : "";
        this.exerciseId = exerciseId != null ? exerciseId : "";
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

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NonNull
    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = String.valueOf(exerciseId);
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId != null ? exerciseId : "";
    }

    public int getNumericExerciseId() {
        try {
            return Integer.parseInt(exerciseId);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
