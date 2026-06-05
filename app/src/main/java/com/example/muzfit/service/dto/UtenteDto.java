package com.example.muzfit.service.dto;

import com.google.gson.annotations.SerializedName;

public class UtenteDto {

    @SerializedName("Username")
    private String username;

    @SerializedName("Nome")
    private String name;

    @SerializedName("Peso")
    private float weight;

    @SerializedName("Altezza")
    private float height;

    @SerializedName("Genere")
    private int genderCode;

    @SerializedName("CalorieBruciate")
    private int calorieBurnGoal;

    @SerializedName("CalorieAssunte")
    private int calorieGoal;

    @SerializedName("Carboidrati")
    private float carbGoal;

    @SerializedName("Proteine")
    private float proteinGoal;

    @SerializedName("Grassi")
    private float fatGoal;

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public float getWeight() {
        return weight;
    }

    public float getHeight() {
        return height;
    }

    public int getGenderCode() {
        return genderCode;
    }

    public int getCalorieBurnGoal() {
        return calorieBurnGoal;
    }

    public int getCalorieGoal() {
        return calorieGoal;
    }

    public float getCarbGoal() {
        return carbGoal;
    }

    public float getProteinGoal() {
        return proteinGoal;
    }

    public float getFatGoal() {
        return fatGoal;
    }
}
