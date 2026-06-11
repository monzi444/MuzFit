package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class PastoUtenteDto {

    @SerializedName("Pasto_idPasto")
    private int mealId;

    @SerializedName("Utente_Username")
    private String username;

    @SerializedName("Data")
    private String date;

    public int getMealId() {
        return mealId;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }
}
