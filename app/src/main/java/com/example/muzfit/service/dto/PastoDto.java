package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class PastoDto {

    @SerializedName("idPasto")
    private int id;

    @SerializedName("Alimento")
    private String foodName;

    @SerializedName("Calorie")
    private float calories;

    @SerializedName("Carboidrati")
    private float carbs;

    @SerializedName("Proteine")
    private float protein;

    public int getId() {
        return id;
    }

    public String getFoodName() {
        return foodName;
    }

    public float getCalories() {
        return calories;
    }

    public float getCarbs() {
        return carbs;
    }

    public float getProtein() {
        return protein;
    }
}
