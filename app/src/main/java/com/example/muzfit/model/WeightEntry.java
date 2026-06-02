package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "WeightEntry",
        primaryKeys = {"dateMillis", "username"},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "username",
                childColumns = "username"
        ),
        indices = @Index("username")
)
public class WeightEntry {

    private long dateMillis;
    private float weight;
    @NonNull
    private String username = "";

    public WeightEntry() {
    }

    @Ignore
    public WeightEntry(long dateMillis, float weight, String username) {
        this.dateMillis = dateMillis;
        this.weight = weight;
        this.username = username != null ? username : "";
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
