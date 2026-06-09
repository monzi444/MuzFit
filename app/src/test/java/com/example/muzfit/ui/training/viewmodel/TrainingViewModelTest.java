package com.example.muzfit.ui.training.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.repository.training.ITrainingRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TrainingViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ITrainingRepository mockRepository;

    private TrainingViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new TrainingViewModel(mockRepository);
    }

    @Test
    public void searchExerciseCatalog_delegatesToRepository() {
        MutableLiveData<Result<List<Exercise>>> expected = new MutableLiveData<>();
        when(mockRepository.searchExerciseCatalog("Push", "Chest")).thenReturn(expected);

        LiveData<Result<List<Exercise>>> actual = viewModel.searchExerciseCatalog("Push", "Chest");

        verify(mockRepository).searchExerciseCatalog("Push", "Chest");
        assertSame(expected, actual);
    }

    @Test
    public void getRoutines_delegatesToRepository() {
        MutableLiveData<Result<List<WorkoutRoutine>>> expected = new MutableLiveData<>();
        when(mockRepository.getRoutines()).thenReturn(expected);

        LiveData<Result<List<WorkoutRoutine>>> actual = viewModel.getRoutines();

        verify(mockRepository).getRoutines();
        assertSame(expected, actual);
    }

    @Test
    public void saveRoutine_delegatesToRepository() {
        WorkoutRoutine routine = new WorkoutRoutine("Test Routine", new ArrayList<>());
        MutableLiveData<Result<Void>> expected = new MutableLiveData<>();
        when(mockRepository.saveRoutine(routine, "Old Routine")).thenReturn(expected);

        LiveData<Result<Void>> actual = viewModel.saveRoutine(routine, "Old Routine");

        verify(mockRepository).saveRoutine(routine, "Old Routine");
        assertSame(expected, actual);
    }

    @Test
    public void deleteRoutine_delegatesToRepository() {
        MutableLiveData<Result<Void>> expected = new MutableLiveData<>();
        when(mockRepository.deleteRoutine("Test Routine")).thenReturn(expected);

        LiveData<Result<Void>> actual = viewModel.deleteRoutine("Test Routine");

        verify(mockRepository).deleteRoutine("Test Routine");
        assertSame(expected, actual);
    }

    @Test
    public void getWorkouts_delegatesToRepository() {
        MutableLiveData<Result<List<Workout>>> expected = new MutableLiveData<>();
        when(mockRepository.getWorkouts()).thenReturn(expected);

        LiveData<Result<List<Workout>>> actual = viewModel.getWorkouts();

        verify(mockRepository).getWorkouts();
        assertSame(expected, actual);
    }

    @Test
    public void getWorkout_delegatesToRepository() {
        MutableLiveData<Result<Workout>> expected = new MutableLiveData<>();
        when(mockRepository.getWorkout(1)).thenReturn(expected);

        LiveData<Result<Workout>> actual = viewModel.getWorkout(1);

        verify(mockRepository).getWorkout(1);
        assertSame(expected, actual);
    }

    @Test
    public void saveWorkout_delegatesToRepository() {
        Workout workout = new Workout();
        MutableLiveData<Result<Void>> expected = new MutableLiveData<>();
        when(mockRepository.saveWorkout(workout)).thenReturn(expected);

        LiveData<Result<Void>> actual = viewModel.saveWorkout(workout);

        verify(mockRepository).saveWorkout(workout);
        assertSame(expected, actual);
    }

    @Test
    public void deleteWorkout_delegatesToRepository() {
        MutableLiveData<Result<Void>> expected = new MutableLiveData<>();
        when(mockRepository.deleteWorkout(1)).thenReturn(expected);

        LiveData<Result<Void>> actual = viewModel.deleteWorkout(1);

        verify(mockRepository).deleteWorkout(1);
        assertSame(expected, actual);
    }

    @Test
    public void getExercises_delegatesToRepository() {
        MutableLiveData<Result<List<Exercise>>> expected = new MutableLiveData<>();
        when(mockRepository.getExercises()).thenReturn(expected);

        LiveData<Result<List<Exercise>>> actual = viewModel.getExercises();

        verify(mockRepository).getExercises();
        assertSame(expected, actual);
    }

    @Test
    public void searchExercises_delegatesToRepository() {
        MutableLiveData<Result<List<Exercise>>> expected = new MutableLiveData<>();
        when(mockRepository.searchExercises("Squat")).thenReturn(expected);

        LiveData<Result<List<Exercise>>> actual = viewModel.searchExercises("Squat");

        verify(mockRepository).searchExercises("Squat");
        assertSame(expected, actual);
    }

    @Test
    public void getWorkoutExercises_delegatesToRepository() {
        MutableLiveData<Result<List<WorkoutExercise>>> expected = new MutableLiveData<>();
        when(mockRepository.getWorkoutExercises(1)).thenReturn(expected);

        LiveData<Result<List<WorkoutExercise>>> actual = viewModel.getWorkoutExercises(1);

        verify(mockRepository).getWorkoutExercises(1);
        assertSame(expected, actual);
    }

    @Test
    public void addWorkoutExercise_delegatesToRepository() {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        MutableLiveData<Result<Void>> expected = new MutableLiveData<>();
        when(mockRepository.addWorkoutExercise(workoutExercise)).thenReturn(expected);

        LiveData<Result<Void>> actual = viewModel.addWorkoutExercise(workoutExercise);

        verify(mockRepository).addWorkoutExercise(workoutExercise);
        assertSame(expected, actual);
    }

    @Test
    public void getExerciseSets_delegatesToRepository() {
        MutableLiveData<Result<List<ExerciseSet>>> expected = new MutableLiveData<>();
        when(mockRepository.getExerciseSets(1, 2)).thenReturn(expected);

        LiveData<Result<List<ExerciseSet>>> actual = viewModel.getExerciseSets(1, 2);

        verify(mockRepository).getExerciseSets(1, 2);
        assertSame(expected, actual);
    }

    @Test
    public void saveExerciseSet_delegatesToRepository() {
        ExerciseSet exerciseSet = new ExerciseSet();
        MutableLiveData<Result<Void>> expected = new MutableLiveData<>();
        when(mockRepository.saveExerciseSet(exerciseSet)).thenReturn(expected);

        LiveData<Result<Void>> actual = viewModel.saveExerciseSet(exerciseSet);

        verify(mockRepository).saveExerciseSet(exerciseSet);
        assertSame(expected, actual);
    }
}
