package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "WorkoutExercise",
        primaryKeys = {
                "workoutId",
                "username",
                "exerciseId"
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Workout.class,
                        parentColumns = {"id", "username"},
                        childColumns = {"workoutId", "username"}
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId"
                )
        },
        indices = {
                @Index({"workoutId", "username"}),
                @Index("exerciseId")
        }
)
public class WorkoutExercise {

    private int calories;
    private int workoutId;
    @NonNull
    private String username = "";
    @NonNull
    private String exerciseId = "";

    public WorkoutExercise() {
    }

    @Ignore
    public WorkoutExercise(int calories, int workoutId, String username, int exerciseId) {
        this(calories, workoutId, username, String.valueOf(exerciseId));
    }

    @Ignore
    public WorkoutExercise(int calories, int workoutId, String username, String exerciseId) {
        this.calories = calories;
        this.workoutId = workoutId;
        this.username = username != null ? username : "";
        this.exerciseId = exerciseId != null ? exerciseId : "";
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
