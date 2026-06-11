package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class DescrizioneEsercizioDto {

    @SerializedName("IdEsercizio")
    private int id;

    @SerializedName("Descrizione")
    private String description;

    @SerializedName("NomeEsercizio")
    private String name;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
