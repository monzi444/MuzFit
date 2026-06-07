package com.example.muzfit.ui.training.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.repository.training.ITrainingRepository;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrainingViewModelTest {

    @Test
    public void getWorkouts_delegatesToRepository() {
        ITrainingRepository repository = mock(ITrainingRepository.class);
        MutableLiveData<Result<List<Workout>>> expected = new MutableLiveData<>();
        when(repository.getWorkouts()).thenReturn(expected);

        TrainingViewModel viewModel = new TrainingViewModel(repository);
        LiveData<Result<List<Workout>>> actual = viewModel.getWorkouts();

        assertSame(expected, actual);
    }

    @Test
    public void searchExercises_delegatesToRepository() {
        ITrainingRepository repository = mock(ITrainingRepository.class);
        MutableLiveData<Result<List<com.example.muzfit.model.Exercise>>> expected = new MutableLiveData<>();
        when(repository.searchExercises("squat")).thenReturn(expected);

        TrainingViewModel viewModel = new TrainingViewModel(repository);
        LiveData<Result<List<com.example.muzfit.model.Exercise>>> actual = viewModel.searchExercises("squat");

        assertSame(expected, actual);
    }
}
