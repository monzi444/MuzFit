package com.example.muzfit.model;

public class Workout {

    private int id;
    private long dateMillis;
    private String description = "";
    private String username = "";

    public Workout() {
    }

    public Workout(int id, long dateMillis, String description, String username) {
        this.id = id;
        this.dateMillis = dateMillis;
        this.description = description;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
