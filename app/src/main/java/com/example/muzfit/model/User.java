package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {

    @PrimaryKey
    @NonNull
    private String uid = "";
    private String name = "";
    private String profileImageUri = "";
    private float weight;
    private float height;
    private int genderCode;
    private int calorieBurnGoal;
    private int calorieGoal;
    private float carbGoal;
    private float proteinGoal;
    private float fatGoal;

    public User() {
    }

    @Ignore
    public User(String uid, String name, String profileImageUri, float weight, float height,
                int genderCode, int calorieBurnGoal, int calorieGoal,
                float carbGoal, float proteinGoal, float fatGoal) {
        this.uid = uid != null ? uid : "";
        this.name = name;
        this.profileImageUri = profileImageUri != null ? profileImageUri : "";
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
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri != null ? profileImageUri : "";
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
