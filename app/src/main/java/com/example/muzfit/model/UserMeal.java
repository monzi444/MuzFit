package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "Pasto_has_Utente",
        primaryKeys = {"Pasto_idPasto", "Utente_Username", "Data"},
        foreignKeys = {
                @ForeignKey(
                        entity = Meal.class,
                        parentColumns = "idPasto",
                        childColumns = "Pasto_idPasto"
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "Username",
                        childColumns = "Utente_Username"
                )
        },
        indices = {@Index("Pasto_idPasto"), @Index("Utente_Username")}
)
public class UserMeal {

    @ColumnInfo(name = "Pasto_idPasto")
    private int mealId;
    @ColumnInfo(name = "Utente_Username")
    @NonNull
    private String username = "";
    @ColumnInfo(name = "Data")
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
