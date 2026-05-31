package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class SerieDto {

    @SerializedName("idSerie")
    private int id;

    @SerializedName("Ripetizioni")
    private int repetitions;

    @SerializedName("Peso")
    private int weight;

    @SerializedName("AllenamentoEsercizio_Allenamento_idAllenamento")
    private int workoutId;

    @SerializedName("AllenamentoEsercizio_Allenamento_Utente_Username")
    private String username;

    @SerializedName("AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio")
    private int exerciseId;

    public int getId() {
        return id;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public int getWeight() {
        return weight;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public String getUsername() {
        return username;
    }

    public int getExerciseId() {
        return exerciseId;
    }
}
