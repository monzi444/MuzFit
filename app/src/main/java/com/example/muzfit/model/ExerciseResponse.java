package com.example.muzfit.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private List<ExerciseDB> data;

    public boolean isSuccess() {
        return success;
    }

    public List<ExerciseDB> getData() {
        return data;
    }
}
