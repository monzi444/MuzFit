package com.example.muzfit.service.dto.openfoodfacts;

import com.example.muzfit.model.Meal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OpenFoodFactsMapper {

    private OpenFoodFactsMapper() {
    }

    public static List<Meal> dedupeMeals(List<Meal> meals, int limit) {
        Map<String, Meal> unique = new LinkedHashMap<>();
        if (meals != null) {
            for (Meal meal : meals) {
                if (meal == null || meal.getFoodName().isEmpty()) {
                    continue;
                }
                String key = meal.getFoodName().toLowerCase(Locale.ROOT).trim();
                unique.putIfAbsent(key, meal);
            }
        }
        List<Meal> result = new ArrayList<>(unique.values());
        if (limit > 0 && result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    public static List<Meal> toMeals(SearchALiciousSearchResponseDto response) {
        if (response == null || response.getHits() == null) {
            return new ArrayList<>();
        }
        return toMealsFromProducts(response.getHits());
    }

    public static List<Meal> toMealsFromProducts(List<OpenFoodFactsProductDto> products) {
        List<Meal> meals = new ArrayList<>();
        if (products == null) {
            return meals;
        }
        for (OpenFoodFactsProductDto product : products) {
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
        float calories = pickCalories(nutriments);
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
        String name = firstNonEmpty(
                product.getProductName(),
                product.getProductNameFr(),
                product.getAbbreviatedProductName(),
                product.getGenericName()
        );
        return name != null ? name : "";
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static float pickCalories(OpenFoodFactsNutrimentsDto nutriments) {
        float kcal = pickValue(
                nutriments.getEnergyKcalServing(),
                nutriments.getEnergyKcal100g(),
                nutriments.getEnergyKcal(),
                nutriments.getEnergyKcalValue()
        );
        if (kcal > 0f) {
            return kcal;
        }
        float energyKj = pickValue(
                null,
                nutriments.getEnergyKj100g(),
                nutriments.getEnergy100g()
        );
        if (energyKj > 0f) {
            return energyKj / 4.184f;
        }
        return 0f;
    }

    private static float pickValue(Float serving, Float per100g, Float fallback) {
        return pickValue(serving, per100g, fallback, null);
    }

    private static float pickValue(Float serving, Float per100g, Float fallback, Float extra) {
        if (serving != null && serving > 0f) {
            return serving;
        }
        if (per100g != null && per100g > 0f) {
            return per100g;
        }
        if (fallback != null && fallback > 0f) {
            return fallback;
        }
        if (extra != null && extra > 0f) {
            return extra;
        }
        return 0f;
    }

    public static String formatSearchSubtitle(Meal meal) {
        return String.format(
                Locale.getDefault(),
                "%.0f kcal · P %.0fg · C %.0fg · F %.0fg",
                meal.getCalories(),
                meal.getProtein(),
                meal.getCarbs(),
                meal.getFat()
        );
    }
}
