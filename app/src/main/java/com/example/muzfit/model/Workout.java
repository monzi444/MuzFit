package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "Allenamento",
        primaryKeys = {"idAllenamento", "Utente_Username"},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "Username",
                childColumns = "Utente_Username"
        ),
        indices = @Index("Utente_Username")
)
public class Workout {

    @ColumnInfo(name = "idAllenamento")
    private int id;
    @ColumnInfo(name = "Data")
    private long dateMillis;
    @ColumnInfo(name = "Descrizione")
    private String description = "";
    @ColumnInfo(name = "Utente_Username")
    @NonNull
    private String username = "";

    public Workout() {
    }

    @Ignore
    public Workout(int id, long dateMillis, String description, String username) {
        this.id = id;
        this.dateMillis = dateMillis;
        this.description = description;
        this.username = username != null ? username : "";
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
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
