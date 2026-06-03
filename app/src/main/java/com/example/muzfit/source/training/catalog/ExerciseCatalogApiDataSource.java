package com.example.muzfit.source.training.catalog;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseResponse;
import com.example.muzfit.service.ExerciseApiService;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseCatalogApiDataSource extends BaseExerciseCatalogDataSource {

    private final ExerciseApiService exerciseApiService;

    public ExerciseCatalogApiDataSource(ExerciseApiService exerciseApiService) {
        this.exerciseApiService = exerciseApiService;
    }

    @Override
    public void searchExercises(String name, String bodyPart, int limit,
                                DataSourceCallback<List<Exercise>> callback) {
        Call<ExerciseResponse> call;
        
        // La API oss.exercisedb.dev utilizza parametri di query per filtrare
        if (name != null && !name.isEmpty()) {
            call = exerciseApiService.getExercisesByName(name.toLowerCase().trim(), limit);
        } else if (bodyPart != null && !bodyPart.isEmpty()) {
            call = exerciseApiService.getExercisesByBodyPart(bodyPart.toLowerCase().trim(), limit);
        } else {
            call = exerciseApiService.getAllExercises(limit);
        }

        call.enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Exercise> data = response.body().getData();
                    if (data == null) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Se abbiamo filtrato per nome, ma l'utente ha selezionato anche una parte del corpo
                    // filtriamo ulteriormente in memoria
                    if (bodyPart != null && !bodyPart.isEmpty() && name != null && !name.isEmpty()) {
                        List<Exercise> filtered = new ArrayList<>();
                        String bpLower = bodyPart.toLowerCase().trim();
                        for (Exercise e : data) {
                            String currentBp = e.getBodyPart();
                            if ((currentBp != null && bpLower.equals(currentBp.toLowerCase())) || 
                                (e.getBodyParts() != null && e.getBodyParts().contains(bpLower))) {
                                filtered.add(e);
                            }
                        }
                        callback.onSuccess(filtered);
                    } else {
                        callback.onSuccess(data);
                    }
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                callback.onSuccess(new ArrayList<>());
            }
        });
    }
}
