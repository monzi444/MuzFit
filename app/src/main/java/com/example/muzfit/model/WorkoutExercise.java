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
                "uid",
                "exerciseId"
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Workout.class,
                        parentColumns = {"id", "uid"},
                        childColumns = {"workoutId", "uid"}
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId"
                )
        },
        indices = {
                @Index({"workoutId", "uid"}),
                @Index("exerciseId")
        }
)
public class WorkoutExercise {

    private int calories;
    private int workoutId;
    @NonNull
    private String uid = "";
    @NonNull
    private String exerciseId = "";

    public WorkoutExercise() {
    }

    @Ignore
    public WorkoutExercise(int calories, int workoutId, String uid, int exerciseId) {
        this(calories, workoutId, uid, String.valueOf(exerciseId));
    }

    @Ignore
    public WorkoutExercise(int calories, int workoutId, String uid, String exerciseId) {
        this.calories = calories;
        this.workoutId = workoutId;
        this.uid = uid != null ? uid : "";
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
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
