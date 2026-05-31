package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class AllenamentoEsercizioDto {

    @SerializedName("Calorie")
    private int calories;

    @SerializedName("Allenamento_idAllenamento")
    private int workoutId;

    @SerializedName("Allenamento_Utente_Username")
    private String username;

    @SerializedName("DescrizioneEsercizio_IdEsercizio")
    private int exerciseId;

    public int getCalories() {
        return calories;
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
