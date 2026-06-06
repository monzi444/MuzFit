package com.example.muzfit.repository.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.firebase.FirestoreSyncDataSource;
import com.example.muzfit.source.profile.BaseProfileDataSource;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.RepositorySupport;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProfileRepository implements IProfileRepository {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final BaseProfileDataSource profileDataSource;
    private final FirestoreSyncDataSource firestoreSyncDataSource;
    private MuzFitDao localDao;
    private Future<?> seedFuture;

    public ProfileRepository(BaseProfileDataSource profileDataSource,
                             FirestoreSyncDataSource firestoreSyncDataSource) {
        this.profileDataSource = profileDataSource;
        this.firestoreSyncDataSource = firestoreSyncDataSource;
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
    public LiveData<Result<User>> getUser() {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<User>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        loadLocalUser(currentUid, Constants.ERROR_USER_NOT_FOUND, liveData);
        fetchRemoteUser(currentUid, liveData);
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> updateUser(User user) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        // In a real app we would update remote first then local
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                try {
                    awaitSeedIfNeeded();
                    user.setUid(currentUid);
                    RepositorySupport.ensureLocalUser(localDao, currentUid);
                    localDao.updateUser(user);
                    firestoreSyncDataSource.saveUser(user);
                    liveData.postValue(new Result.Success<>(null));
                } catch (Exception e) {
                    liveData.postValue(new Result.Error<>(errorMessage(e)));
                }
            });
        } else {
            liveData.postValue(new Result.Success<>(null));
        }
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> updateGoals(User user) {
        return updateUser(user);
    }

    @Override
    public LiveData<Result<List<WeightEntry>>> getWeightHistory() {
        if (localDao == null) {
            MutableLiveData<Result<List<WeightEntry>>> liveData = new MutableLiveData<>();
            liveData.setValue(new Result.Error<>(Constants.ERROR_DATABASE));
            return liveData;
        }
        String currentUid = RepositorySupport.currentUidOrDefault();
        return androidx.lifecycle.Transformations.map(localDao.getWeightEntries(currentUid), Result.Success::new);
    }

    @Override
    public LiveData<Result<Void>> addWeightEntry(WeightEntry weightEntry) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                try {
                    awaitSeedIfNeeded();
                    RepositorySupport.ensureLocalUser(localDao, currentUid);
                    weightEntry.setUid(currentUid);
                    localDao.insertWeightEntry(weightEntry);
                    firestoreSyncDataSource.saveWeightEntry(weightEntry);
                    liveData.postValue(new Result.Success<>(null));
                } catch (Exception e) {
                    liveData.postValue(new Result.Error<>(errorMessage(e)));
                }
            });
        } else {
            liveData.postValue(new Result.Success<>(null));
        }
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> deleteWeightEntry(WeightEntry weightEntry) {
        String currentUid = RepositorySupport.currentUidOrDefault();
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao != null) {
            EXECUTOR.execute(() -> {
                try {
                    awaitSeedIfNeeded();
                    weightEntry.setUid(currentUid);
                    localDao.deleteWeightEntry(weightEntry);
                    firestoreSyncDataSource.deleteWeightEntry(weightEntry);
                    liveData.postValue(new Result.Success<>(null));
                } catch (Exception e) {
                    liveData.postValue(new Result.Error<>(errorMessage(e)));
                }
            });
        } else {
            liveData.postValue(new Result.Success<>(null));
        }
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

    private void loadLocalUser(String uid, String fallbackMessage, MutableLiveData<Result<User>> liveData) {
        if (localDao == null) {
            liveData.postValue(new Result.Error<>(fallbackMessage));
            return;
        }
        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                User user = RepositorySupport.ensureLocalUser(localDao, uid);
                liveData.postValue(new Result.Success<>(user));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(errorMessage(e)));
            }
        });
    }

    private void fetchRemoteUser(String uid, MutableLiveData<Result<User>> liveData) {
        if (!firestoreSyncDataSource.canSync(uid)) {
            return;
        }
        firestoreSyncDataSource.fetchUser(uid, new DataSourceCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (localDao != null) {
                    EXECUTOR.execute(() -> localDao.insertUser(user));
                }
                liveData.postValue(new Result.Success<>(user));
            }

            @Override
            public void onError(String message) {
            }
        });
    }
}
