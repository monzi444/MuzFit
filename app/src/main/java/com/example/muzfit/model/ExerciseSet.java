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
                "uid",
                "exerciseId"
        },
        foreignKeys = @ForeignKey(
                entity = WorkoutExercise.class,
                parentColumns = {
                        "workoutId",
                        "uid",
                        "exerciseId"
                },
                childColumns = {
                        "workoutId",
                        "uid",
                        "exerciseId"
                }
        ),
        indices = @Index({
                "workoutId",
                "uid",
                "exerciseId"
        })
)
public class ExerciseSet {

    private int id;
    private int repetitions;
    private int weight;
    private int workoutId;
    @NonNull
    private String uid = "";
    @NonNull
    private String exerciseId = "";

    public ExerciseSet() {
    }

    @Ignore
    public ExerciseSet(int id, int repetitions, int weight,
                       int workoutId, String uid, int exerciseId) {
        this(id, repetitions, weight, workoutId, uid, String.valueOf(exerciseId));
    }

    @Ignore
    public ExerciseSet(int id, int repetitions, int weight,
                       int workoutId, String uid, String exerciseId) {
        this.id = id;
        this.repetitions = repetitions;
        this.weight = weight;
        this.workoutId = workoutId;
        this.uid = uid != null ? uid : "";
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
