package com.example.muzfit.ui.auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.model.Result;
import com.example.muzfit.repository.auth.IAuthRepository;

public class AuthViewModel extends ViewModel {

    private final IAuthRepository repository;

    public AuthViewModel(IAuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Result<Void>> signOut() {
        return repository.signOut();
    }

    public LiveData<Result<String>> getCurrentUserEmail() {
        return repository.getCurrentUserEmail();
    }
}
