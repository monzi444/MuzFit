package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.DashboardCalendarDay;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;

import java.util.List;

public interface IDashboardRepository {

    LiveData<Result<Float>> getConsumedCalories();
    LiveData<Result<Float>> getConsumedCalories(long dateMillis);

    LiveData<Result<Float>> getConsumedCarbs();
    LiveData<Result<Float>> getConsumedCarbs(long dateMillis);

    LiveData<Result<Float>> getConsumedProteins();
    LiveData<Result<Float>> getConsumedProteins(long dateMillis);

    LiveData<Result<Float>> getConsumedFats();
    LiveData<Result<Float>> getConsumedFats(long dateMillis);

    LiveData<Result<User>> getMacroGoals();

    LiveData<Result<List<WeightEntry>>> getWeights();

    LiveData<Result<int[]>> getDailyCaloriesBurned();
    LiveData<Result<int[]>> getDailyCaloriesConsumed(long dateMillis);

    LiveData<Result<Integer>> getCaloriesBurned(long dateMillis);

    LiveData<Result<List<DashboardCalendarDay>>> getCalendarData(int year, int month);

    LiveData<Result<Void>> deleteWeightEntry(WeightEntry weightEntry);
}
