package com.example.muzfit.repository.profile;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;

import java.util.List;

public interface IProfileRepository {

    LiveData<Result<User>> getUser(String username);

    LiveData<Result<Void>> updateUser(User user);

    LiveData<Result<Void>> updateGoals(User user);

    LiveData<Result<List<WeightEntry>>> getWeightHistory(String username);

    LiveData<Result<Void>> addWeightEntry(WeightEntry weightEntry);
}
