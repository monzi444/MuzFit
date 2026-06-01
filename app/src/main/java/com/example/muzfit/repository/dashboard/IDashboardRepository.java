package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.WeightEntry;

import java.util.List;

public interface IDashboardRepository {

    LiveData<Result<Float>> getCosumedCalories();

    LiveData<Result<Float>> getConsumedCarbs();

    LiveData<Result<Float>> getConsumedProteins();

    LiveData<Result<Float>> getConsumedFats();

    LiveData<Result<List<WeightEntry>>> getWeights(String username);

    LiveData<Result<int[]>> getDailyCaloriesBurned();
}
