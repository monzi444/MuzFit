package com.example.muzfit.ui.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.utils.Constants;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final IProfileRepository repository;

    public ProfileViewModel(IProfileRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<User>> getUser(String username) {
        return repository.getUser(username);
    }

    public LiveData<Result<User>> getDefaultUser() {
        return repository.getUser(Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<Void>> updateUser(User user) {
        return repository.updateUser(user);
    }

    public LiveData<Result<Void>> updateGoals(User user) {
        return repository.updateGoals(user);
    }

    public LiveData<Result<List<WeightEntry>>> getWeightHistory(String username) {
        return repository.getWeightHistory(username);
    }

    public LiveData<Result<List<WeightEntry>>> getDefaultUserWeightHistory() {
        return repository.getWeightHistory(Constants.DEFAULT_USERNAME);
    }

    public LiveData<Result<Void>> addWeightEntry(WeightEntry weightEntry) {
        return repository.addWeightEntry(weightEntry);
    }
}
