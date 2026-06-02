package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "Peso",
        primaryKeys = {"Data", "Utente_Username"},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "Username",
                childColumns = "Utente_Username"
        ),
        indices = @Index("Utente_Username")
)
public class WeightEntry {

    @ColumnInfo(name = "Data")
    private long dateMillis;
    @ColumnInfo(name = "Peso")
    private float weight;
    @ColumnInfo(name = "Utente_Username")
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
