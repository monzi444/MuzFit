package com.example.muzfit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class WorkoutAdapter extends ArrayAdapter<WorkoutRoutine> {
    private final Context context;
    private final List<WorkoutRoutine> routineList;

    public WorkoutAdapter(@NonNull Context context, List<WorkoutRoutine> routineList) {
        super(context, 0, routineList);
        this.context = context;
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_workout, parent, false);
        }

        WorkoutRoutine routine = routineList.get(position);

        TextView routineNameTextView = convertView.findViewById(R.id.routineNameTextView);
        TextView routineDetailsTextView = convertView.findViewById(R.id.routineDetailsTextView);

        routineNameTextView.setText(routine.getName());
        routineDetailsTextView.setText(routine.getExerciseSummary());

        return convertView;
    }
}
