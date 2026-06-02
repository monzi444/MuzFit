package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Utente")
public class User {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "Username")
    private String username = "";
    @ColumnInfo(name = "Nome")
    private String name = "";
    @ColumnInfo(name = "Password")
    private String password = "";
    @ColumnInfo(name = "Peso")
    private float weight;
    @ColumnInfo(name = "Altezza")
    private float height;
    @ColumnInfo(name = "Genere")
    private int genderCode;
    @ColumnInfo(name = "CalorieBruciate")
    private int calorieBurnGoal;
    @ColumnInfo(name = "CalorieAssunte")
    private int calorieGoal;
    @ColumnInfo(name = "Carboidrati")
    private float carbGoal;
    @ColumnInfo(name = "Proteine")
    private float proteinGoal;
    @ColumnInfo(name = "Grassi")
    private float fatGoal;

    public User() {
    }

    @Ignore
    public User(String username, String name, String password, float weight, float height,
                int genderCode, int calorieBurnGoal, int calorieGoal,
                float carbGoal, float proteinGoal, float fatGoal) {
        this.username = username != null ? username : "";
        this.name = name;
        this.password = password;
        this.weight = weight;
        this.height = height;
        this.genderCode = genderCode;
        this.calorieBurnGoal = calorieBurnGoal;
        this.calorieGoal = calorieGoal;
        this.carbGoal = carbGoal;
        this.proteinGoal = proteinGoal;
        this.fatGoal = fatGoal;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(int genderCode) {
        this.genderCode = genderCode;
    }

    @Ignore
    public Gender getGender() {
        return Gender.fromCode(genderCode);
    }

    public void setGender(Gender gender) {
        this.genderCode = gender.getCode();
    }

    public int getCalorieBurnGoal() {
        return calorieBurnGoal;
    }

    public void setCalorieBurnGoal(int calorieBurnGoal) {
        this.calorieBurnGoal = calorieBurnGoal;
    }

    public int getCalorieGoal() {
        return calorieGoal;
    }

    public void setCalorieGoal(int calorieGoal) {
        this.calorieGoal = calorieGoal;
    }

    public float getCarbGoal() {
        return carbGoal;
    }

    public void setCarbGoal(float carbGoal) {
        this.carbGoal = carbGoal;
    }

    public float getProteinGoal() {
        return proteinGoal;
    }

    public void setProteinGoal(float proteinGoal) {
        this.proteinGoal = proteinGoal;
    }

    public float getFatGoal() {
        return fatGoal;
    }

    public void setFatGoal(float fatGoal) {
        this.fatGoal = fatGoal;
    }
}
