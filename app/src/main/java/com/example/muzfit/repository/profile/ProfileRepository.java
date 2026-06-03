package com.example.muzfit.repository.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.profile.BaseProfileDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProfileRepository implements IProfileRepository {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final BaseProfileDataSource profileDataSource;
    private MuzFitDao localDao;
    private Future<?> seedFuture;

    public ProfileRepository(BaseProfileDataSource profileDataSource) {
        this.profileDataSource = profileDataSource;
    }

    public void setLocalDatabase(MuzFitDatabase database) {
        if (database != null) {
            localDao = database.muzFitDao();
        }
    }

    public void setSeedFuture(Future<?> seedFuture) {
        this.seedFuture = seedFuture;
    }

    @Override
    public LiveData<Result<User>> getUser(String username) {
        if (localDao != null) {
            return getUserFromLocal(username);
        }
        return getUserFromRemote(username);
    }

    @Override
    public LiveData<Result<Void>> updateUser(User user) {
        if (localDao != null) {
            return updateUserInLocal(user);
        }
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<Void>> updateGoals(User user) {
        if (localDao != null) {
            return updateUserInLocal(user);
        }
        return RepositorySupport.notSupported();
    }

    @Override
    public LiveData<Result<List<WeightEntry>>> getWeightHistory(String username) {
        if (localDao != null) {
            return getWeightHistoryFromLocal(username);
        }
        return getWeightHistoryFromRemote(username);
    }

    @Override
    public LiveData<Result<Void>> addWeightEntry(WeightEntry weightEntry) {
        if (localDao != null) {
            return addWeightEntryInLocal(weightEntry);
        }
        return RepositorySupport.notSupported();
    }

    private LiveData<Result<User>> getUserFromLocal(String username) {
        MutableLiveData<Result<User>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                User user = localDao.getUser(username);
                if (user == null) {
                    liveData.postValue(new Result.Error<>(Constants.ERROR_USER_NOT_FOUND));
                } else {
                    liveData.postValue(new Result.Success<>(user));
                }
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    private LiveData<Result<User>> getUserFromRemote(String username) {
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

    private LiveData<Result<Void>> updateUserInLocal(User user) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                localDao.updateUser(user);
                liveData.postValue(new Result.Success<>(null));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    private LiveData<Result<List<WeightEntry>>> getWeightHistoryFromLocal(String username) {
        MutableLiveData<Result<List<WeightEntry>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                liveData.postValue(new Result.Success<>(localDao.getWeightEntries(username)));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    private LiveData<Result<List<WeightEntry>>> getWeightHistoryFromRemote(String username) {
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

    private LiveData<Result<Void>> addWeightEntryInLocal(WeightEntry weightEntry) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                localDao.insertWeightEntry(weightEntry);
                liveData.postValue(new Result.Success<>(null));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
        return liveData;
    }

    private void awaitSeedIfNeeded() throws Exception {
        if (seedFuture != null) {
            seedFuture.get();
        }
    }

    private static String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : Constants.ERROR_DATABASE;
    }
}
