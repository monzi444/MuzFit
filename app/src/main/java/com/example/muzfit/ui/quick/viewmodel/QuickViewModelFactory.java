package com.example.muzfit.ui.quick.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.repository.quick.IQuickRepository;

public class QuickViewModelFactory implements ViewModelProvider.Factory {

    private final IQuickRepository repository;

    public QuickViewModelFactory(IQuickRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(QuickViewModel.class)) {
            return (T) new QuickViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
