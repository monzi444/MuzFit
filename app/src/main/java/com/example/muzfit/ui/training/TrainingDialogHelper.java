package com.example.muzfit.ui.training;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.muzfit.ui.training.WorkoutSessionActivity;

import java.util.ArrayList;
import java.util.List;

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
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_choose_meal, null);
        ListView listView = dialogView.findViewById(R.id.lvChooseMeal);
        TextView titleView = dialogView.findViewById(R.id.choose_meal_title);
        TextView emptyView = dialogView.findViewById(R.id.tvChooseMealEmpty);
        
        // Customize the generic "choose meal" dialog for workouts
        titleView.setText(R.string.workout_page_title);
        dialogView.findViewById(R.id.tilChooseMealSort).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnAddFoodFromPicker).setVisibility(View.GONE);
        
        List<WorkoutRoutine> routines = new ArrayList<>();
        WorkoutAdapter adapter = new WorkoutAdapter(activity, routines);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.Theme_MuzFit_Dialog)
                .setView(dialogView)
                .create();

        viewModel.getRoutines().observe(lifecycleOwner, result -> {
            if (result.isLoading()) return;
            if (result.isSuccess()) {
                List<WorkoutRoutine> data = ((Result.Success<List<WorkoutRoutine>>) result).getData();
                routines.clear();
                if (data != null && !data.isEmpty()) {
                    routines.addAll(data);
                    emptyView.setVisibility(View.GONE);
                } else {
                    emptyView.setText(R.string.select_routine_toast); // Reuse string or use custom
                    emptyView.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            } else if (result.isError()) {
                Toast.makeText(activity, "Error loading routines", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            WorkoutRoutine routine = routines.get(position);
            if (routine.getExercises().isEmpty()) {
                Toast.makeText(activity, "This routine has no exercises!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            startWorkout(routine);
        });

        dialogView.findViewById(R.id.btnCancelChooseMeal).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void startWorkout(WorkoutRoutine routine) {
        Intent intent = new Intent(activity, WorkoutSessionActivity.class);
        intent.putExtra("EXTRA_ROUTINE", routine);
        activity.startActivity(intent);
    }
}
