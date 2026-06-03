package com.example.muzfit.service.dto.openfoodfacts;

import com.google.gson.annotations.SerializedName;

public class OpenFoodFactsProductDto {

    @SerializedName("product_name")
    private String productName;

    @SerializedName("generic_name")
    private String genericName;

    @SerializedName("nutriments")
    private OpenFoodFactsNutrimentsDto nutriments;

    public String getProductName() {
        return productName;
    }

    public String getGenericName() {
        return genericName;
    }

    public OpenFoodFactsNutrimentsDto getNutriments() {
        return nutriments;
    }
}
