package com.example.muzfit.service.dto.openfoodfacts;

import com.google.gson.annotations.SerializedName;

public class OpenFoodFactsProductDto {

    @SerializedName("product_name")
    private String productName;

    @SerializedName("product_name_fr")
    private String productNameFr;

    @SerializedName("abbreviated_product_name")
    private String abbreviatedProductName;

    @SerializedName("generic_name")
    private String genericName;

    @SerializedName("nutriments")
    private OpenFoodFactsNutrimentsDto nutriments;

    public String getProductName() {
        return productName;
    }

    public String getProductNameFr() {
        return productNameFr;
    }

    public String getAbbreviatedProductName() {
        return abbreviatedProductName;
    }

    public String getGenericName() {
        return genericName;
    }

    public OpenFoodFactsNutrimentsDto getNutriments() {
        return nutriments;
    }
}
