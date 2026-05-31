package com.example.muzfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutFragment extends Fragment {

    private ListView routineListView;
    private Button startWorkoutButton;
    private Button editRoutineButton;
    private Button deleteRoutineButton;
    private Button createRoutineButton;
    private WorkoutAdapter adapter;
    private final List<WorkoutRoutine> routineList = new ArrayList<>();
    private int selectedPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        routineListView = view.findViewById(R.id.routineListView);
        startWorkoutButton = view.findViewById(R.id.startWorkoutButton);
        editRoutineButton = view.findViewById(R.id.editRoutineButton);
        deleteRoutineButton = view.findViewById(R.id.deleteRoutineButton);
        createRoutineButton = view.findViewById(R.id.createRoutineButton);

        adapter = new WorkoutAdapter(requireContext(), routineList);
        routineListView.setAdapter(adapter);

        routineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                selectedPosition = position;
                routineListView.setItemChecked(position, true);
            }
        });

        startWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition < 0) {
                    Toast.makeText(getContext(), getString(R.string.select_routine_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                WorkoutRoutine routine = routineList.get(selectedPosition);
                if (routine.getExercises().isEmpty()) {
                    Toast.makeText(getContext(), "This routine has no exercises!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Intent intent = new Intent(getContext(), WorkoutSessionActivity.class);
                intent.putExtra("EXTRA_ROUTINE", routine);
                startActivity(intent);
            }
        });

        createRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoutineDialog(null);
            }
        });

        editRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition < 0) {
                    Toast.makeText(getContext(), getString(R.string.select_routine_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                showRoutineDialog(routineList.get(selectedPosition));
            }
        });

        deleteRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition < 0) {
                    Toast.makeText(getContext(), getString(R.string.select_routine_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                showDeleteConfirmDialog(selectedPosition);
            }
        });

        return view;
    }

    private void showDeleteConfirmDialog(int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    routineList.remove(position);
                    selectedPosition = -1;
                    routineListView.clearChoices();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), R.string.routine_deleted_toast, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRoutineDialog(@Nullable WorkoutRoutine routineToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_workout, null);
        TextInputEditText etRoutineName = dialogView.findViewById(R.id.etRoutineName);
        EditText etSearchExercise = dialogView.findViewById(R.id.etSearchExercise);
        RecyclerView rvExerciseResults = dialogView.findViewById(R.id.rvExerciseResults);
        TextView tvSelectedExercisesCount = dialogView.findViewById(R.id.tvSelectedExercisesCount);
        ChipGroup cgBodyParts = dialogView.findViewById(R.id.cgBodyParts);

        List<Exercise> selectedExercises = new ArrayList<>();
        if (routineToEdit != null) {
            etRoutineName.setText(routineToEdit.getName());
            selectedExercises.addAll(routineToEdit.getExercises());
        }

        List<Exercise> searchResults = new ArrayList<>();
        
        ExerciseSearchAdapter searchAdapter = new ExerciseSearchAdapter(searchResults, exercise -> {
            showExercisePreviewDialog(exercise, () -> {
                selectedExercises.add(exercise);
                tvSelectedExercisesCount.setText(getString(R.string.selected_exercises_count, selectedExercises.size()));
            });
        });

        rvExerciseResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExerciseResults.setAdapter(searchAdapter);

        tvSelectedExercisesCount.setText(getString(R.string.selected_exercises_count, selectedExercises.size()));
        tvSelectedExercisesCount.setOnClickListener(v -> showSelectedExercisesRecap(selectedExercises, tvSelectedExercisesCount));

        cgBodyParts.setOnCheckedChangeListener((group, checkedId) -> {
            String query = etSearchExercise.getText().toString().trim();
            searchExercises(query, getSelectedBodyPart(group), searchResults, searchAdapter);
        });

        etSearchExercise.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 3 || (!query.isEmpty() && cgBodyParts.getCheckedChipId() != View.NO_ID)) {
                    searchExercises(query, getSelectedBodyPart(cgBodyParts), searchResults, searchAdapter);
                } else if (query.isEmpty() && cgBodyParts.getCheckedChipId() == View.NO_ID) {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        builder.setTitle(routineToEdit == null ? R.string.create_routine_title : R.string.edit_routine_title);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            if (etRoutineName.getText() == null) {
                return;
            }
            String name = etRoutineName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.routine_name_required_toast), Toast.LENGTH_SHORT).show();
                return;
            }
            
            WorkoutRoutine newRoutine = new WorkoutRoutine(name, new ArrayList<>(selectedExercises));
            if (routineToEdit != null) {
                int index = routineList.indexOf(routineToEdit);
                if (index != -1) {
                    routineList.set(index, newRoutine);
                }
            } else {
                routineList.add(newRoutine);
                selectedPosition = routineList.size() - 1;
                routineListView.setItemChecked(selectedPosition, true);
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), getString(R.string.routine_created_toast), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showExercisePreviewDialog(Exercise exercise, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_exercise_preview, null);
        TextView tvPreviewName = dialogView.findViewById(R.id.tvPreviewName);
        ImageView ivExerciseGif = dialogView.findViewById(R.id.ivExerciseGif);
        TextView tvPreviewDetails = dialogView.findViewById(R.id.tvPreviewDetails);

        tvPreviewName.setText(exercise.getName());
        tvPreviewDetails.setText(getString(R.string.exercise_details_format,
                exercise.getBodyPart(), exercise.getTarget(), exercise.getEquipment()));

        Glide.with(this)
                .load(exercise.getGifUrl())
                .placeholder(R.drawable.ic_exercise_placeholder)
                .error(R.drawable.ic_exercise_placeholder)
                .into(ivExerciseGif);

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.add_to_routine, (dialog, which) -> onConfirm.run());
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showSelectedExercisesRecap(List<Exercise> selectedExercises, TextView tvCount) {
        if (selectedExercises.isEmpty()) return;

        String[] names = new String[selectedExercises.size()];
        for (int i = 0; i < selectedExercises.size(); i++) {
            names[i] = selectedExercises.get(i).getName();
        }

        boolean[] checked = new boolean[selectedExercises.size()];
        for (int i = 0; i < checked.length; i++) checked[i] = true;

        new AlertDialog.Builder(requireContext())
                .setTitle("Riepilogo Esercizi (deseleziona per rimuovere)")
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> {
                    checked[which] = isChecked;
                })
                .setPositiveButton("Conferma", (dialog, which) -> {
                    for (int i = checked.length - 1; i >= 0; i--) {
                        if (!checked[i]) {
                            selectedExercises.remove(i);
                        }
                    }
                    tvCount.setText(getString(R.string.selected_exercises_count, selectedExercises.size()));
                })
                .show();
    }

    private void searchExercises(String query, List<Exercise> results, ExerciseSearchAdapter adapter) {
        searchExercises(query, null, results, adapter);
    }

    private void searchExercises(String query, String bodyPart, List<Exercise> results, ExerciseSearchAdapter adapter) {
        String normalizedQuery = query.toLowerCase().trim();
        RetrofitClient.getApiService().getExercisesByName(normalizedQuery, bodyPart, 50).enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExerciseResponse> call, @NonNull Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Exercise> apiData = response.body().getData();
                    
                    // Sorting by relevance
                    Collections.sort(apiData, (e1, e2) -> {
                        String name1 = e1.getName().toLowerCase();
                        String name2 = e2.getName().toLowerCase();
                        
                        boolean starts1 = name1.startsWith(normalizedQuery);
                        boolean starts2 = name2.startsWith(normalizedQuery);
                        
                        if (starts1 && !starts2) return -1;
                        if (!starts1 && starts2) return 1;
                        
                        boolean contains1 = name1.contains(normalizedQuery);
                        boolean contains2 = name2.contains(normalizedQuery);
                        
                        if (contains1 && !contains2) return -1;
                        if (!contains1 && contains2) return 1;
                        
                        return name1.compareTo(name2);
                    });

                    results.clear();
                    results.addAll(apiData);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExerciseResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Errore API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getSelectedBodyPart(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            View chip = group.findViewById(checkedId);
            if (chip.getTag() != null) {
                return chip.getTag().toString();
            }
        }
        return null;
    }
}
