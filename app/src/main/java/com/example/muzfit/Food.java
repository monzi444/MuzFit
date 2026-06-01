package com.example.muzfit;

public class Food {
    public enum Category {
        COLAZIONE, PRANZO, CENA
    }

    private String name;
    private int calories;
    private int carbs;
    private int protein;
    private int fat;
    private Category category;

    public Food(String name, int calories) {
        this(name, calories, 0, 0, 0, Category.PRANZO);
    }

    public Food(String name, int calories, int carbs, int protein, int fat) {
        this(name, calories, carbs, protein, fat, Category.PRANZO);
    }

    public Food(String name, int calories, int carbs, int protein, int fat, Category category) {
        this.name = name;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public int getCalories() {
        return calories;
    }

    public int getCarbs() {
        return carbs;
    }

    public int getProtein() {
        return protein;
    }

    public int getFat() {
        return fat;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return name + " (" + calories + " kcal)";
    }
}