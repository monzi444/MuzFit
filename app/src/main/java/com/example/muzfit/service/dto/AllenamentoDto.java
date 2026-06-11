package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class AllenamentoDto {

    @SerializedName("idAllenamento")
    private int id;

    @SerializedName("Data")
    private String date;

    @SerializedName("Descrizione")
    private String description;

    @SerializedName("Utente_Username")
    private String username;

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }
}
