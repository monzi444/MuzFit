package com.example.muzfit.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class RepositorySupport {

    private RepositorySupport() {
    }

    public static <T> LiveData<Result<T>> notSupported() {
        MutableLiveData<Result<T>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Error<>(Constants.ERROR_NOT_SUPPORTED));
        return liveData;
    }

    public static String currentUidOrDefault() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            return Constants.DEFAULT_USERNAME;
        }
        return user.getUid();
    }

    public static User ensureLocalUser(MuzFitDao dao, String uid) {
        if (dao == null) {
            return null;
        }
        User existing = dao.getUser(uid);
        if (existing != null) {
            return existing;
        }

        User defaultUser = dao.getUser(Constants.DEFAULT_USERNAME);
        User user = new User();
        user.setUid(uid);
        if (defaultUser != null) {
            user.setName(defaultUser.getName());
            user.setProfileImageUri(defaultUser.getProfileImageUri());
            user.setWeight(defaultUser.getWeight());
            user.setHeight(defaultUser.getHeight());
            user.setGenderCode(defaultUser.getGenderCode());
            user.setCalorieBurnGoal(defaultUser.getCalorieBurnGoal());
            user.setCalorieGoal(defaultUser.getCalorieGoal());
            user.setCarbGoal(defaultUser.getCarbGoal());
            user.setProteinGoal(defaultUser.getProteinGoal());
            user.setFatGoal(defaultUser.getFatGoal());
        }
        dao.insertUser(user);
        return user;
    }
}
