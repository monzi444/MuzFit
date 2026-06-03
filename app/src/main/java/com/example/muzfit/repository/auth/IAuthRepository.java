package com.example.muzfit.repository.auth;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Result;

public interface IAuthRepository {

    LiveData<Result<Void>> signOut();

    LiveData<Result<String>> getCurrentUserEmail();
}
