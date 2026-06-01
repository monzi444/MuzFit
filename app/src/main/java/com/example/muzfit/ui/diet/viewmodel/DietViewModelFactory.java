package com.example.muzfit.ui.diet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.repository.diet.IDietRepository;

public class DietViewModelFactory implements ViewModelProvider.Factory {

    private final IDietRepository repository;

    public DietViewModelFactory(IDietRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DietViewModel.class)) {
            return (T) new DietViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
