package com.example.muzfit.ui.training.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.muzfit.R;
import com.example.muzfit.adapter.ExerciseSearchAdapter;
import com.example.muzfit.adapter.SelectedExercisesAdapter;
import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.ui.training.WorkoutSessionActivity;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModel;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment {

    private ComposeView workoutRoutinesCompose;
    private Button startWorkoutButton;
    private Button btnRoutineOptions;
    private Button createRoutineButton;
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

        workoutRoutinesCompose = view.findViewById(R.id.workoutRoutinesCompose);
        startWorkoutButton = view.findViewById(R.id.startWorkoutButton);
        btnRoutineOptions = view.findViewById(R.id.btnRoutineOptions);
        createRoutineButton = view.findViewById(R.id.createRoutineButton);

        view.findViewById(R.id.btnWorkoutHistory).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), com.example.muzfit.ui.training.WorkoutHistoryActivity.class));
        });

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
                java.util.Collections.sort(routineList, (r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
                renderRoutines();
                preloadRoutineGifs(data);
            }
        });
    }

    private void renderRoutines() {
        if (workoutRoutinesCompose == null) return;
        WorkoutRoutinesBridge.setContent(
                workoutRoutinesCompose,
                routineList,
                selectedPosition,
                (Integer idx) -> { selectedPosition = idx; renderRoutines(); return kotlin.Unit.INSTANCE; },
                (WorkoutRoutine r) -> { showManageRoutineDialog(r); return kotlin.Unit.INSTANCE; },
                (WorkoutRoutine r) -> {
                    int pos = routineList.indexOf(r);
                    if (pos >= 0) showDeleteConfirmDialog(pos);
                    return kotlin.Unit.INSTANCE;
                },
                () -> {
                    List<Exercise> selectedExercises = new ArrayList<>();
                    showRoutineDialog(selectedExercises, () -> {
                        if (!selectedExercises.isEmpty()) {
                            showManageRoutineDialog(new WorkoutRoutine(getNextDefaultWorkoutName(), selectedExercises));
                        } else {
                            Toast.makeText(getContext(), "Select at least one exercise to continue", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return kotlin.Unit.INSTANCE;
                }
        );
    }

    private void preloadRoutineGifs(List<WorkoutRoutine> routines) {
        if (routines == null || !isAdded() || getContext() == null) return;
        
        for (WorkoutRoutine routine : routines) {
            if (routine != null && routine.getExercises() != null) {
                for (Exercise exercise : routine.getExercises()) {
                    if (exercise != null && exercise.getGifUrl() != null && !exercise.getGifUrl().trim().isEmpty()) {
                        Glide.with(this)
                                .load(exercise.getGifUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .preload();
                    }
                }
            }
        }
    }

    private void showDeleteConfirmDialog(int position) {
        WorkoutRoutine routine = routineList.get(position);
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_delete_confirm, null);
        setupDeleteConfirmDialogBlur(dialogView);

        TextView tvDeleteConfirmMessage = dialogView.findViewById(R.id.tvDeleteConfirmMessage);
        // Personalize the message with the routine name so the user knows
        // exactly which routine they're about to remove.
        tvDeleteConfirmMessage.setText(getString(R.string.delete_confirm_message_with_name, routine.getName()));

        com.google.android.material.button.MaterialButton btnCancelDelete =
                dialogView.findViewById(R.id.btnCancelDelete);
        com.google.android.material.button.MaterialButton btnConfirmDelete =
                dialogView.findViewById(R.id.btnConfirmDelete);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            viewModel.deleteRoutine(routine.getName()).observe(getViewLifecycleOwner(), result -> {
                if (!isAdded()) return;
                if (result.isLoading()) return;
                if (result.isSuccess()) {
                    dialog.dismiss();
                    loadRoutines();
                    selectedPosition = -1;
                    Toast.makeText(getContext(), R.string.routine_deleted_toast, Toast.LENGTH_SHORT).show();
                } else {
                    String msg = ((Result.Error<Void>) result).getMessage();
                    Toast.makeText(getContext(), "Error deleting: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showRoutineOptionsDialog(WorkoutRoutine routine, int position) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_routine_options, null);
        setupRoutineOptionsDialogBlur(dialogView);

        TextView tvRoutineOptionsName = dialogView.findViewById(R.id.tvRoutineOptionsName);
        View llOptionEdit = dialogView.findViewById(R.id.llOptionEdit);
        View llOptionDelete = dialogView.findViewById(R.id.llOptionDelete);
        com.google.android.material.button.MaterialButton btnCancel =
                dialogView.findViewById(R.id.btnCancelRoutineOptions);

        if (routine != null && tvRoutineOptionsName != null) {
            tvRoutineOptionsName.setText(routine.getName());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        llOptionEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showManageRoutineDialog(routine);
        });
        llOptionDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmDialog(position);
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showManageRoutineDialog(@Nullable WorkoutRoutine routineToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manage_exercises, null);
        setupManageExercisesDialogBlur(dialogView);
        TextInputEditText etRoutineName = dialogView.findViewById(R.id.etRoutineName);
        RecyclerView rvSelectedExercises = dialogView.findViewById(R.id.rvSelectedExercises);
        Button btnAddNewExercise = dialogView.findViewById(R.id.btnAddNewExercise);
        com.google.android.material.button.MaterialButton btnCancelManage =
                dialogView.findViewById(R.id.btnCancelManage);
        com.google.android.material.button.MaterialButton btnSaveManage =
                dialogView.findViewById(R.id.btnSaveManage);

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

        // Build the dialog WITHOUT system title/positive/negative buttons — those
        // would sit outside our glass card. Instead wire the in-XML Cancel/Save
        // buttons so the entire panel reads as one coherent surface.
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Hold a ref to the final selected exercises list for the save handler.
        final List<SelectedExercisesAdapter.SelectableExercise> finalSelectable = selectableExercises;

        btnCancelManage.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnSaveManage.setOnClickListener(v -> {
            if (etRoutineName.getText() == null) {
                return;
            }
            String name = etRoutineName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.routine_name_required_toast), Toast.LENGTH_SHORT).show();
                return;
            }

            List<Exercise> finalExercises = new ArrayList<>();
            for (SelectedExercisesAdapter.SelectableExercise se : finalSelectable) {
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
                    dialog.dismiss();
                    loadRoutines();
                    Toast.makeText(getContext(), getString(R.string.routine_created_toast), Toast.LENGTH_SHORT).show();
                } else {
                    String msg = ((Result.Error<Void>) result).getMessage();
                    Toast.makeText(getContext(), "Errore salvataggio: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showRoutineDialog(List<Exercise> selectedExercises, Runnable onComplete) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_workout, null);
        setupCreateWorkoutDialogBlur(dialogView);
        View til = dialogView.findViewById(R.id.tilRoutineName);
        if (til != null) til.setVisibility(View.GONE);

        EditText etSearchExercise = dialogView.findViewById(R.id.etSearchExercise);
        RecyclerView rvExerciseResults = dialogView.findViewById(R.id.rvExerciseResults);
        TextView tvSelectedExercisesCount = dialogView.findViewById(R.id.tvSelectedExercisesCount);
        ChipGroup cgBodyParts = dialogView.findViewById(R.id.cgBodyParts);
        com.google.android.material.button.MaterialButton btnCancelCreateWorkout =
                dialogView.findViewById(R.id.btnCancelCreateWorkout);
        com.google.android.material.button.MaterialButton btnSaveCreateWorkout =
                dialogView.findViewById(R.id.btnSaveCreateWorkout);

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

        // Build the dialog WITHOUT system title/positive/negative buttons — those
        // would sit outside our glass card and double up with the in-XML header
        // and the in-XML Cancel/Save buttons.
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        btnCancelCreateWorkout.setOnClickListener(v -> dialog.dismiss());
        btnSaveCreateWorkout.setOnClickListener(v -> {
            dialog.dismiss();
            onComplete.run();
        });

        dialog.show();
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

    /**
     * Wires the BlurView on the manage-exercises dialog (Android 12+). On older
     * devices the BlurView stays transparent and the translucent glass
     * background still gives a soft frosted look.
     */
    private void setupManageExercisesDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.manage_exercises_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /** Same as above, for the search-exercises dialog. */
    private void setupCreateWorkoutDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.create_workout_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /** Blur for dialog_routine_options (edit / delete routine). */
    private void setupRoutineOptionsDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.routine_options_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /** Blur for dialog_delete_confirm (confirm a routine deletion). */
    private void setupDeleteConfirmDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.delete_confirm_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /**
     * Clips the given BlurView to a 28dp rounded rectangle. The XML
     * `bg_dialog_blur_rounded` background + clipToOutline combo is
     * unreliable for BlurView (which extends ConstraintLayout) on
     * some devices/emulators, so we also set a programmatic outline
     * provider. The two are belt-and-braces and both yield the same
     * 28dp corner radius as the glass card on top.
     */
    private void applyRoundedOutline(BlurView blurView) {
        if (blurView == null) return;
        final float radiusPx = 28f * blurView.getResources().getDisplayMetrics().density;
        blurView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
        blurView.setClipToOutline(true);
    }
}
