package com.example.muzfit;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private List<Exercise> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Exercise> getData() {
        return data;
    }
}
