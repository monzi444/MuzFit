package com.example.muzfit.source.training;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.service.MuzFitApiService;
import com.example.muzfit.service.dto.AllenamentoDto;
import com.example.muzfit.service.dto.AllenamentoEsercizioDto;
import com.example.muzfit.service.dto.ApiMapper;
import com.example.muzfit.service.dto.DescrizioneEsercizioDto;
import com.example.muzfit.service.dto.SerieDto;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.source.common.RetrofitCallbacks;

import java.util.List;

public class TrainingApiDataSource extends BaseTrainingDataSource {

    private final MuzFitApiService apiService;

    public TrainingApiDataSource(MuzFitApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void fetchWorkouts(DataSourceCallback<List<Workout>> callback) {
        RetrofitCallbacks.enqueue(apiService.getAllenamenti(), new DataSourceCallback<List<AllenamentoDto>>() {
            @Override
            public void onSuccess(List<AllenamentoDto> data) {
                callback.onSuccess(ApiMapper.toWorkouts(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void fetchExercises(DataSourceCallback<List<Exercise>> callback) {
        RetrofitCallbacks.enqueue(apiService.getDescrizioniEsercizio(),
                new DataSourceCallback<List<DescrizioneEsercizioDto>>() {
                    @Override
                    public void onSuccess(List<DescrizioneEsercizioDto> data) {
                        callback.onSuccess(ApiMapper.toExercises(data));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    @Override
    public void fetchWorkoutExercises(DataSourceCallback<List<WorkoutExercise>> callback) {
        RetrofitCallbacks.enqueue(apiService.getAllenamentiEsercizio(),
                new DataSourceCallback<List<AllenamentoEsercizioDto>>() {
                    @Override
                    public void onSuccess(List<AllenamentoEsercizioDto> data) {
                        callback.onSuccess(ApiMapper.toWorkoutExercises(data));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    @Override
    public void fetchExerciseSets(DataSourceCallback<List<ExerciseSet>> callback) {
        RetrofitCallbacks.enqueue(apiService.getSerie(), new DataSourceCallback<List<SerieDto>>() {
            @Override
            public void onSuccess(List<SerieDto> data) {
                callback.onSuccess(ApiMapper.toExerciseSets(data));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
