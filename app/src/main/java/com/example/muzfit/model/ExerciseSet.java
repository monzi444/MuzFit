package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "Serie",
        primaryKeys = {
                "idSerie",
                "AllenamentoEsercizio_Allenamento_idAllenamento",
                "AllenamentoEsercizio_Allenamento_Utente_Username",
                "AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio"
        },
        foreignKeys = @ForeignKey(
                entity = WorkoutExercise.class,
                parentColumns = {
                        "Allenamento_idAllenamento",
                        "Allenamento_Utente_Username",
                        "DescrizioneEsercizio_IdEsercizio"
                },
                childColumns = {
                        "AllenamentoEsercizio_Allenamento_idAllenamento",
                        "AllenamentoEsercizio_Allenamento_Utente_Username",
                        "AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio"
                }
        ),
        indices = @Index({
                "AllenamentoEsercizio_Allenamento_idAllenamento",
                "AllenamentoEsercizio_Allenamento_Utente_Username",
                "AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio"
        })
)
public class ExerciseSet {

    @ColumnInfo(name = "idSerie")
    private int id;
    @ColumnInfo(name = "Ripetizioni")
    private int repetitions;
    @ColumnInfo(name = "Peso")
    private int weight;
    @ColumnInfo(name = "AllenamentoEsercizio_Allenamento_idAllenamento")
    private int workoutId;
    @ColumnInfo(name = "AllenamentoEsercizio_Allenamento_Utente_Username")
    @NonNull
    private String username = "";
    @ColumnInfo(name = "AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio")
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
