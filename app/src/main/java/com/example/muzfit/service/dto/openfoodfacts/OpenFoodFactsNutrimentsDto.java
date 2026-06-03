package com.example.muzfit.service.dto.openfoodfacts;

import com.google.gson.annotations.SerializedName;

public class OpenFoodFactsNutrimentsDto {

    @SerializedName("energy-kcal_100g")
    private Float energyKcal100g;

    @SerializedName("energy-kcal_serving")
    private Float energyKcalServing;

    @SerializedName("energy-kcal")
    private Float energyKcal;

    @SerializedName("proteins_100g")
    private Float proteins100g;

    @SerializedName("proteins_serving")
    private Float proteinsServing;

    @SerializedName("proteins")
    private Float proteins;

    @SerializedName("carbohydrates_100g")
    private Float carbohydrates100g;

    @SerializedName("carbohydrates_serving")
    private Float carbohydratesServing;

    @SerializedName("carbohydrates")
    private Float carbohydrates;

    @SerializedName("fat_100g")
    private Float fat100g;

    @SerializedName("fat_serving")
    private Float fatServing;

    @SerializedName("fat")
    private Float fat;

    public Float getEnergyKcal100g() {
        return energyKcal100g;
    }

    public Float getEnergyKcalServing() {
        return energyKcalServing;
    }

    public Float getEnergyKcal() {
        return energyKcal;
    }

    public Float getProteins100g() {
        return proteins100g;
    }

    public Float getProteinsServing() {
        return proteinsServing;
    }

    public Float getProteins() {
        return proteins;
    }

    public Float getCarbohydrates100g() {
        return carbohydrates100g;
    }

    public Float getCarbohydratesServing() {
        return carbohydratesServing;
    }

    public Float getCarbohydrates() {
        return carbohydrates;
    }

    public Float getFat100g() {
        return fat100g;
    }

    public Float getFatServing() {
        return fatServing;
    }

    public Float getFat() {
        return fat;
    }
}
