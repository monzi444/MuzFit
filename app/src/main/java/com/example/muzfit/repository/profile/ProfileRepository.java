package com.example.muzfit.repository.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.profile.BaseProfileDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.List;

public class ProfileRepository implements IProfileRepository {

    private final BaseProfileDataSource profileDataSource;

    public ProfileRepository(BaseProfileDataSource profileDataSource) {
        this.profileDataSource = profileDataSource;
    }

    @Override
    public LiveData<Result<User>> getUser(String username) {
        MutableLiveData<Result<User>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        profileDataSource.fetchUsers(new DataSourceCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                for (User user : data) {
                    if (username.equals(user.getUsername())) {
                        liveData.postValue(new Result.Success<>(user));
                        return;
                    }
                }
                liveData.postValue(new Result.Error<>(Constants.ERROR_USER_NOT_FOUND));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> updateUser(User user) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<Void>> updateGoals(User user) {
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<List<WeightEntry>>> getWeightHistory(String username) {
        MutableLiveData<Result<List<WeightEntry>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        profileDataSource.fetchWeightEntries(new DataSourceCallback<List<WeightEntry>>() {
            @Override
            public void onSuccess(List<WeightEntry> data) {
                List<WeightEntry> filtered = new ArrayList<>();
                for (WeightEntry entry : data) {
                    if (username.equals(entry.getUsername())) {
                        filtered.add(entry);
                    }
                }
                liveData.postValue(new Result.Success<>(filtered));
            }

            @Override
            public void onError(String message) {
                liveData.postValue(new Result.Error<>(message));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> addWeightEntry(WeightEntry weightEntry) {
        return RepositorySupport.notSupported();
    }
}
