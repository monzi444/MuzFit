package com.example.muzfit.ui.training.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.repository.training.ITrainingRepository;

public class TrainingViewModelFactory implements ViewModelProvider.Factory {

    private final ITrainingRepository repository;

    public TrainingViewModelFactory(ITrainingRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TrainingViewModel.class)) {
            return (T) new TrainingViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
