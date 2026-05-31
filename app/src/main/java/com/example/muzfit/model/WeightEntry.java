package com.example.muzfit.model;

public class WeightEntry {

    private long dateMillis;
    private float weight;
    private String username = "";

    public WeightEntry() {
    }

    public WeightEntry(long dateMillis, float weight, String username) {
        this.dateMillis = dateMillis;
        this.weight = weight;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
