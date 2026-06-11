package com.example.muzfit.ui.training;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.example.muzfit.R;
import com.example.muzfit.adapter.WorkoutAdapter;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.ui.training.viewmodel.TrainingViewModel;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;

public class TrainingDialogHelper {

    private final FragmentActivity activity;
    private final TrainingViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public TrainingDialogHelper(FragmentActivity activity, TrainingViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void showStartWorkoutDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_choose_workout, null);
        setupWorkoutDialogBlur(dialogView);

        ListView listView = dialogView.findViewById(R.id.lvChooseWorkout);
        EditText etSearch = dialogView.findViewById(R.id.etChooseWorkoutSearch);
        TextView emptyView = dialogView.findViewById(R.id.tvChooseWorkoutEmpty);

        List<WorkoutRoutine> routines = new ArrayList<>();
        List<WorkoutRoutine> filteredRoutines = new ArrayList<>();
        WorkoutAdapter adapter = new WorkoutAdapter(activity, filteredRoutines);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        viewModel.getRoutines().observe(lifecycleOwner, result -> {
            if (result.isLoading()) return;
            if (result.isSuccess()) {
                List<WorkoutRoutine> data = ((Result.Success<List<WorkoutRoutine>>) result).getData();
                routines.clear();
                filteredRoutines.clear();
                if (data != null && !data.isEmpty()) {
                    routines.addAll(data);
                    filteredRoutines.addAll(data);
                    emptyView.setVisibility(View.GONE);
                } else {
                    emptyView.setText(R.string.choose_workout_empty);
                    emptyView.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            } else if (result.isError()) {
                Toast.makeText(activity, "Error loading routines", Toast.LENGTH_SHORT).show();
            }
        });

        // Search filtering
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRoutines(routines, filteredRoutines, s.toString(), adapter, emptyView);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            WorkoutRoutine routine = filteredRoutines.get(position);
            if (routine.getExercises().isEmpty()) {
                Toast.makeText(activity, "This routine has no exercises!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            startWorkout(routine);
        });

        dialogView.findViewById(R.id.btnCancelChooseWorkout).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void filterRoutines(List<WorkoutRoutine> all, List<WorkoutRoutine> filtered,
                                String query, WorkoutAdapter adapter, TextView emptyView) {
        filtered.clear();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(all);
        } else {
            String lower = query.toLowerCase().trim();
            for (WorkoutRoutine r : all) {
                if (r.getName() != null && r.getName().toLowerCase().contains(lower)) {
                    filtered.add(r);
                }
            }
        }
        if (filtered.isEmpty() && !all.isEmpty()) {
            emptyView.setText(R.string.choose_workout_empty);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    /** Blur for the "Start Workout" dialog. */
    private void setupWorkoutDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.choose_workout_blur);
        if (blurView == null) return;
        // 28dp rounded corners — same as the glass card
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    private void applyRoundedOutline(BlurView blurView) {
        final float radiusPx = 28f * blurView.getResources().getDisplayMetrics().density;
        blurView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
        blurView.setClipToOutline(true);
    }

    private void startWorkout(WorkoutRoutine routine) {
        Intent intent = new Intent(activity, WorkoutSessionActivity.class);
        intent.putExtra("EXTRA_ROUTINE", routine);
        activity.startActivity(intent);
    }
}
