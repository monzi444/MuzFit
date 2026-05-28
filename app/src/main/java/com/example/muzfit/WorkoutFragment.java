package com.example.muzfit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment {

    private ListView routineListView;
    private Button startWorkoutButton;
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
        createRoutineButton = view.findViewById(R.id.createRoutineButton);

        if (routineList.isEmpty()) {
            routineList.add(new WorkoutRoutine("Push Day", 6));
            routineList.add(new WorkoutRoutine("Leg Day", 5));
            routineList.add(new WorkoutRoutine("Full Body", 8));
        }

        adapter = new WorkoutAdapter(requireContext(), routineList);
        routineListView.setAdapter(adapter);

        routineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                selectedPosition = position;
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
                Toast.makeText(
                        getContext(),
                        getString(R.string.starting_workout_toast, routine.getName()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        createRoutineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateRoutineDialog();
            }
        });

        return view;
    }

    private void showCreateRoutineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_workout, null);
        TextInputEditText etRoutineName = dialogView.findViewById(R.id.etRoutineName);

        builder.setTitle(R.string.create_routine_title);
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
            routineList.add(new WorkoutRoutine(name, 0));
            adapter.notifyDataSetChanged();
            selectedPosition = routineList.size() - 1;
            routineListView.setItemChecked(selectedPosition, true);
            Toast.makeText(getContext(), getString(R.string.routine_created_toast), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
