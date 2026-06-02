package com.example.muzfit.ui.dashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.DashboardCalendarDay;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.repository.dashboard.IDashboardRepository;

import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final IDashboardRepository repository;

    public DashboardViewModel(IDashboardRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<Float>> getConsumedCalories() {
        return repository.getConsumedCalories();
    }

    public LiveData<Result<Float>> getConsumedCarbs() {
        return repository.getConsumedCarbs();
    }

    public LiveData<Result<Float>> getConsumedProteins() {
        return repository.getConsumedProteins();
    }

    public LiveData<Result<Float>> getConsumedFats() {
        return repository.getConsumedFats();
    }

    public LiveData<Result<User>> getMacroGoals() {
        return repository.getMacroGoals();
    }

    public LiveData<Result<List<WeightEntry>>> getWeights() {
        return repository.getWeights();
    }

    public LiveData<Result<int[]>> getDailyCaloriesBurned() {
        return repository.getDailyCaloriesBurned();
    }

    public LiveData<Result<List<DashboardCalendarDay>>> getCalendarData(int year, int month) {
        return repository.getCalendarData(year, month);
    }
}
