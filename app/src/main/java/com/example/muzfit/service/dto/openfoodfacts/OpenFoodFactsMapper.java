package com.example.muzfit.service.dto.openfoodfacts;

import com.example.muzfit.model.Meal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OpenFoodFactsMapper {

    private OpenFoodFactsMapper() {
    }

    public static List<Meal> toMeals(OpenFoodFactsSearchResponseDto response) {
        List<Meal> meals = new ArrayList<>();
        if (response == null || response.getProducts() == null) {
            return meals;
        }
        for (OpenFoodFactsProductDto product : response.getProducts()) {
            Meal meal = toMeal(product);
            if (meal != null) {
                meals.add(meal);
            }
        }
        return meals;
    }

    public static Meal toMeal(OpenFoodFactsProductDto product) {
        if (product == null) {
            return null;
        }
        String name = resolveName(product);
        if (name.isEmpty()) {
            return null;
        }
        OpenFoodFactsNutrimentsDto nutriments = product.getNutriments();
        if (nutriments == null) {
            return null;
        }
        float calories = pickValue(
                nutriments.getEnergyKcalServing(),
                nutriments.getEnergyKcal100g(),
                nutriments.getEnergyKcal()
        );
        float protein = pickValue(
                nutriments.getProteinsServing(),
                nutriments.getProteins100g(),
                nutriments.getProteins()
        );
        float carbs = pickValue(
                nutriments.getCarbohydratesServing(),
                nutriments.getCarbohydrates100g(),
                nutriments.getCarbohydrates()
        );
        float fat = pickValue(
                nutriments.getFatServing(),
                nutriments.getFat100g(),
                nutriments.getFat()
        );
        if (calories <= 0f && protein <= 0f && carbs <= 0f && fat <= 0f) {
            return null;
        }
        return new Meal(0, name, calories, carbs, protein, fat);
    }

    private static String resolveName(OpenFoodFactsProductDto product) {
        String productName = product.getProductName();
        if (productName != null && !productName.trim().isEmpty()) {
            return productName.trim();
        }
        String genericName = product.getGenericName();
        if (genericName != null && !genericName.trim().isEmpty()) {
            return genericName.trim();
        }
        return "";
    }

    private static float pickValue(Float serving, Float per100g, Float fallback) {
        if (serving != null && serving > 0f) {
            return serving;
        }
        if (per100g != null && per100g > 0f) {
            return per100g;
        }
        if (fallback != null && fallback > 0f) {
            return fallback;
        }
        return 0f;
    }

    public static String formatSearchSubtitle(Meal meal) {
        return String.format(
                Locale.getDefault(),
                "%.0f kcal · P %.0fg · C %.0fg · G %.0fg",
                meal.getCalories(),
                meal.getProtein(),
                meal.getCarbs(),
                meal.getFat()
        );
    }
}
