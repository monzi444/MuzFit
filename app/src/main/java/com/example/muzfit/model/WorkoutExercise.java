package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "AllenamentoEsercizio",
        primaryKeys = {
                "Allenamento_idAllenamento",
                "Allenamento_Utente_Username",
                "DescrizioneEsercizio_IdEsercizio"
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Workout.class,
                        parentColumns = {"idAllenamento", "Utente_Username"},
                        childColumns = {"Allenamento_idAllenamento", "Allenamento_Utente_Username"}
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "IdEsercizio",
                        childColumns = "DescrizioneEsercizio_IdEsercizio"
                )
        },
        indices = {
                @Index({"Allenamento_idAllenamento", "Allenamento_Utente_Username"}),
                @Index("DescrizioneEsercizio_IdEsercizio")
        }
)
public class WorkoutExercise {

    @ColumnInfo(name = "Calorie")
    private int calories;
    @ColumnInfo(name = "Allenamento_idAllenamento")
    private int workoutId;
    @ColumnInfo(name = "Allenamento_Utente_Username")
    @NonNull
    private String username = "";
    @ColumnInfo(name = "DescrizioneEsercizio_IdEsercizio")
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
