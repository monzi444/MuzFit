package com.example.muzfit.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Pasto")
public class Meal {

    @PrimaryKey
    @ColumnInfo(name = "idPasto")
    private int id;
    @ColumnInfo(name = "Alimento")
    private String foodName = "";
    @ColumnInfo(name = "Calorie")
    private float calories;
    @ColumnInfo(name = "Carboidrati")
    private float carbs;
    @ColumnInfo(name = "Proteine")
    private float protein;
    @ColumnInfo(name = "Grassi")
    private float fat;
    private Food.Category category = Food.Category.PRANZO;

    public Meal() {
    }

    @Ignore
    public Meal(int id, String foodName, float calories, float carbs, float protein, float fat) {
        this(id, foodName, calories, carbs, protein, fat, Food.Category.PRANZO);
    }

    public Meal(int id, String foodName, float calories, float carbs, float protein, float fat, Food.Category category) {
        this.id = id;
        this.foodName = foodName;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getCarbs() {
        return carbs;
    }

    public void setCarbs(float carbs) {
        this.carbs = carbs;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public Food.Category getCategory() {
        return category;
    }

    public void setCategory(Food.Category category) {
        this.category = category;
    }
}
