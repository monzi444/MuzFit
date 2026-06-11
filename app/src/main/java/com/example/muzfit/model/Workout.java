package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "Workout",
        primaryKeys = {"id", "uid"},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "uid",
                childColumns = "uid"
        ),
        indices = @Index("uid")
)
public class Workout {

    private int id;
    private long dateMillis;
    private String description = "";
    @NonNull
    private String uid = "";

    public Workout() {
    }

    @Ignore
    public Workout(int id, long dateMillis, String description, String uid) {
        this.id = id;
        this.dateMillis = dateMillis;
        this.description = description;
        this.uid = uid != null ? uid : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
