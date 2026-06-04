package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "WeightEntry",
        primaryKeys = {"dateMillis", "uid"},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "uid",
                childColumns = "uid"
        ),
        indices = @Index("uid")
)
public class WeightEntry {

    private long dateMillis;
    private float weight;
    @NonNull
    private String uid = "";

    public WeightEntry() {
    }

    @Ignore
    public WeightEntry(long dateMillis, float weight, String uid) {
        this.dateMillis = dateMillis;
        this.weight = weight;
        this.uid = uid != null ? uid : "";
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
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
