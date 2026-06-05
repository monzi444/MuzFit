package com.example.muzfit.ui.training.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.muzfit.R;
import com.example.muzfit.adapter.ExerciseSearchAdapter;
import com.example.muzfit.adapter.SelectedExercisesAdapter;
import com.example.muzfit.adapter.WorkoutRecyclerAdapter;
import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.ui.training.WorkoutSessionActivity;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModel;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class WorkoutFragment extends Fragment {

    private RecyclerView routineRecyclerView;
    private Button startWorkoutButton;
    private Button btnRoutineOptions;
    private Button createRoutineButton;
    private WorkoutRecyclerAdapter adapter;
    private LinearLayout scrollbarContainer;
    private TrainingViewModel viewModel;
    private final List<WorkoutRoutine> routineList = new ArrayList<>();
    private int selectedPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        TrainingViewModelFactory factory = new TrainingViewModelFactory(
                ServiceLocator.getInstance().getTrainingRepository()
        );
        viewModel = new ViewModelProvider(this, factory).get(TrainingViewModel.class);

        routineRecyclerView = view.findViewById(R.id.routineRecyclerView);
        scrollbarContainer = view.findViewById(R.id.scrollbar_container);
        startWorkoutButton = view.findViewById(R.id.startWorkoutButton);
        btnRoutineOptions = view.findViewById(R.id.btnRoutineOptions);
        createRoutineButton = view.findViewById(R.id.createRoutineButton);

        adapter = new WorkoutRecyclerAdapter(routineList, position -> {
            selectedPosition = position;
        });
        routineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        routineRecyclerView.setAdapter(adapter);

        loadRoutines();

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
                List<Exercise> selectedExercises = new ArrayList<>();
                showRoutineDialog(selectedExercises, () -> {
                    if (!selectedExercises.isEmpty()) {
                        showManageRoutineDialog(new WorkoutRoutine(getNextDefaultWorkoutName(), selectedExercises));
                    } else {
                        Toast.makeText(getContext(), "Select at least one exercise to continue", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnRoutineOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition < 0) {
                    Toast.makeText(getContext(), getString(R.string.select_routine_toast), Toast.LENGTH_SHORT).show();
                    return;
                }
                showRoutineOptionsDialog(routineList.get(selectedPosition), selectedPosition);
            }
        });

        return view;
    }

    private void loadRoutines() {
        viewModel.getRoutines().observe(getViewLifecycleOwner(), result -> {
            if (!isAdded()) return;
            if (result.isLoading()) return;
            if (result.isError()) return;
            
            List<WorkoutRoutine> data = ((Result.Success<List<WorkoutRoutine>>) result).getData();
            if (data != null) {
                routineList.clear();
                routineList.addAll(data);
                Collections.sort(routineList, (r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
                adapter.notifyDataSetChanged();
                setupAlphabetScrollbar();
            }
        });
    }

    private void setupAlphabetScrollbar() {
        if (scrollbarContainer == null) return;
        scrollbarContainer.removeAllViews();

        Set<Character> lettersSet = new TreeSet<>();
        for (WorkoutRoutine r : routineList) {
            if (r.getName() != null && !r.getName().isEmpty()) {
                lettersSet.add(Character.toUpperCase(r.getName().charAt(0)));
            }
        }
        
        List<Character> letters = new ArrayList<>(lettersSet);
        if (letters.isEmpty()) return;

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.muz_primary_lime);
        int variantColor = ContextCompat.getColor(requireContext(), R.color.muz_on_surface_variant);

        // Calcolo DINAMICO basato sulla proporzione dello spazio
        int numLetters = letters.size();
        
        // Formule più sensibili per il ridimensionamento
        float scaleFactor = Math.max(0.4f, Math.min(1.0f, 12f / numLetters));
        
        int dotSizeDp = (int) (14 * scaleFactor);
        if (dotSizeDp < 4) dotSizeDp = 4;
        
        int textSizeSp = (int) (12 * scaleFactor);
        if (textSizeSp < 7) textSizeSp = 7;

        for (Character letter : letters) {
            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            
            // Usiamo il peso (weight) per far sì che gli elementi si distribuiscano uniformemente
            // e si rimpiccioliscano automaticamente per stare nel contenitore
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
            itemLayout.setLayoutParams(itemParams);

            View dot = new View(requireContext());
            int dotSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dotSizeDp, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            dot.setLayoutParams(dotParams);
            
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(primaryColor);
            dot.setBackground(shape);

            TextView tv = new TextView(requireContext());
            tv.setText(String.valueOf(letter));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
            tv.setTextColor(variantColor);
            tv.setTypeface(null, Typeface.BOLD);
            
            itemLayout.addView(dot);
            itemLayout.addView(tv);
            
            scrollbarContainer.addView(itemLayout);
        }

        // Supporto per lo scroll al tocco/trascinamento
        scrollbarContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_MOVE || 
                event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                
                float y = event.getY();
                int childCount = scrollbarContainer.getChildCount();
                if (childCount == 0) return true;
                
                for (int i = 0; i < childCount; i++) {
                    View child = scrollbarContainer.getChildAt(i);
                    if (y >= child.getTop() && y <= child.getBottom()) {
                        Character letter = letters.get(i);
                        scrollToLetter(letter);
                        break;
                    }
                }
                return true;
            }
            return false;
        });
    }

    private void scrollToLetter(Character letter) {
        for (int i = 0; i < routineList.size(); i++) {
            if (Character.toUpperCase(routineList.get(i).getName().charAt(0)) == letter) {
                ((LinearLayoutManager) routineRecyclerView.getLayoutManager())
                        .scrollToPositionWithOffset(i, 0);
                break;
            }
        }
    }

    private void showDeleteConfirmDialog(int position) {
        WorkoutRoutine routine = routineList.get(position);
        new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteRoutine(routine.getName()).observe(getViewLifecycleOwner(), result -> {
                        if (!isAdded()) return;
                        if (result.isLoading()) return;
                        if (result.isSuccess()) {
                            loadRoutines(); // Refresh full list from DB
                            selectedPosition = -1;
                            adapter.clearSelection();
                            Toast.makeText(getContext(), R.string.routine_deleted_toast, Toast.LENGTH_SHORT).show();
                        } else {
                            String msg = ((Result.Error<Void>) result).getMessage();
                            Toast.makeText(getContext(), "Error deleting: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRoutineOptionsDialog(WorkoutRoutine routine, int position) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setTitle("Routine Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showManageRoutineDialog(routine);
                    } else if (which == 1) {
                        showDeleteConfirmDialog(position);
                    }
                })
                .show();
    }

    private void showManageRoutineDialog(@Nullable WorkoutRoutine routineToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_exercises, null);
        TextInputEditText etRoutineName = dialogView.findViewById(R.id.etRoutineName);
        RecyclerView rvSelectedExercises = dialogView.findViewById(R.id.rvSelectedExercises);
        Button btnAddNewExercise = dialogView.findViewById(R.id.btnAddNewExercise);

        List<SelectedExercisesAdapter.SelectableExercise> selectableExercises = new ArrayList<>();
        if (routineToEdit != null) {
            etRoutineName.setText(routineToEdit.getName());
            for (Exercise e : routineToEdit.getExercises()) {
                selectableExercises.add(new SelectedExercisesAdapter.SelectableExercise(e));
            }
        } else {
            etRoutineName.setText(getNextDefaultWorkoutName());
        }

        final ItemTouchHelper[] ith = new ItemTouchHelper[1];
        SelectedExercisesAdapter selectedAdapter = new SelectedExercisesAdapter(selectableExercises, viewHolder -> {
            if (ith[0] != null) ith[0].startDrag(viewHolder);
        });

        ith[0] = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                selectedAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });

        rvSelectedExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSelectedExercises.setAdapter(selectedAdapter);
        ith[0].attachToRecyclerView(rvSelectedExercises);

        btnAddNewExercise.setOnClickListener(v -> {
            List<Exercise> newlyAdded = new ArrayList<>();
            showRoutineDialog(newlyAdded, () -> {
                for (Exercise e : newlyAdded) {
                    selectableExercises.add(new SelectedExercisesAdapter.SelectableExercise(e));
                }
                selectedAdapter.notifyDataSetChanged();
            });
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

            List<Exercise> finalExercises = new ArrayList<>();
            for (SelectedExercisesAdapter.SelectableExercise se : selectableExercises) {
                if (se.isSelected) {
                    finalExercises.add(se.exercise);
                }
            }

            WorkoutRoutine newRoutine = new WorkoutRoutine(name, finalExercises);
            String oldName = (routineToEdit != null) ? routineToEdit.getName() : null;

            viewModel.saveRoutine(newRoutine, oldName).observe(getViewLifecycleOwner(), result -> {
                if (!isAdded()) return;
                if (result.isLoading()) return;

                if (result.isSuccess()) {
                    loadRoutines();
                    Toast.makeText(getContext(), getString(R.string.routine_created_toast), Toast.LENGTH_SHORT).show();
                } else {
                    String msg = ((Result.Error<Void>) result).getMessage();
                    Toast.makeText(getContext(), "Errore salvataggio: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showRoutineDialog(List<Exercise> selectedExercises, Runnable onComplete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_workout, null);
        View til = dialogView.findViewById(R.id.tilRoutineName);
        if (til != null) til.setVisibility(View.GONE);
        
        EditText etSearchExercise = dialogView.findViewById(R.id.etSearchExercise);
        RecyclerView rvExerciseResults = dialogView.findViewById(R.id.rvExerciseResults);
        TextView tvSelectedExercisesCount = dialogView.findViewById(R.id.tvSelectedExercisesCount);
        ChipGroup cgBodyParts = dialogView.findViewById(R.id.cgBodyParts);

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
        tvSelectedExercisesCount.setClickable(false);

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

        builder.setTitle(R.string.search_exercises);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.save, (dialog, which) -> onComplete.run());
        builder.show();
    }

    private void showRoutineDialog(@Nullable WorkoutRoutine routineToEdit) {
        showManageRoutineDialog(routineToEdit);
    }

    private void showExercisePreviewDialog(Exercise exercise, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog);
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

    private String getNextDefaultWorkoutName() {
        int maxNum = 0;
        String prefix = "Workout ";
        for (WorkoutRoutine r : routineList) {
            String name = r.getName();
            if (name != null && name.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(name.substring(prefix.length()).trim());
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + (maxNum + 1);
    }

    private void searchExercises(String query, List<Exercise> results, ExerciseSearchAdapter adapter) {
        searchExercises(query, null, results, adapter);
    }

    private void searchExercises(String query, String bodyPart, List<Exercise> results, ExerciseSearchAdapter adapter) {
        viewModel.searchExerciseCatalog(query, bodyPart).observe(getViewLifecycleOwner(), result -> {
            if (!isAdded()) return;
            if (result.isLoading()) return;
            if (result.isError()) {
                String message = ((Result.Error<List<Exercise>>) result).getMessage();
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            List<Exercise> apiData = ((Result.Success<List<Exercise>>) result).getData();
            results.clear();
            if (apiData != null) {
                results.addAll(apiData);
            }
            adapter.notifyDataSetChanged();
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
