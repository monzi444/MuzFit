package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "UserMeal",
        primaryKeys = {"mealId", "username", "dateMillis"},
        foreignKeys = {
                @ForeignKey(
                        entity = Meal.class,
                        parentColumns = "id",
                        childColumns = "mealId"
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "username",
                        childColumns = "username"
                )
        },
        indices = {@Index("mealId"), @Index("username")}
)
public class UserMeal {

    private int mealId;
    @NonNull
    private String username = "";
    private long dateMillis;

    public UserMeal() {
    }

    @Ignore
    public UserMeal(int mealId, String username, long dateMillis) {
        this.mealId = mealId;
        this.username = username != null ? username : "";
        this.dateMillis = dateMillis;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    @NonNull
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
