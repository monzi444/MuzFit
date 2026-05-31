package com.example.muzfit.model;

public class UserMeal {

    private int mealId;
    private String username = "";
    private long dateMillis;

    public UserMeal() {
    }

    public UserMeal(int mealId, String username, long dateMillis) {
        this.mealId = mealId;
        this.username = username;
        this.dateMillis = dateMillis;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }
}
