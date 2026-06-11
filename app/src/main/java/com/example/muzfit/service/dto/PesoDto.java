package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class PesoDto {

    @SerializedName("Data")
    private String date;

    @SerializedName("Peso")
    private float weight;

    @SerializedName("Utente_Username")
    private String username;

    public String getDate() {
        return date;
    }

    public float getWeight() {
        return weight;
    }

    public String getUsername() {
        return username;
    }
}
