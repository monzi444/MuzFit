package com.example.muzfit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.model.Exercise;

import java.util.Collections;
import java.util.List;

public class SelectedExercisesAdapter extends RecyclerView.Adapter<SelectedExercisesAdapter.ViewHolder> {

    private final List<SelectableExercise> exercises;
    private final OnStartDragListener dragListener;

    public static class SelectableExercise {
        public final Exercise exercise;
        public boolean isSelected = true;

        public SelectableExercise(Exercise exercise) {
            this.exercise = exercise;
        }
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public SelectedExercisesAdapter(List<SelectableExercise> exercises, OnStartDragListener dragListener) {
        this.exercises = exercises;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_reorder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SelectableExercise item = exercises.get(position);
        holder.tvExerciseName.setText(item.exercise.getName());
        holder.cbSelected.setOnCheckedChangeListener(null);
        holder.cbSelected.setChecked(item.isSelected);
        
        holder.cbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.isSelected = isChecked;
        });

        holder.ivReorderHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
                dragListener.onStartDrag(holder);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(exercises, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(exercises, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvExerciseName;
        final CheckBox cbSelected;
        final ImageView ivReorderHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            cbSelected = itemView.findViewById(R.id.cbSelected);
            ivReorderHandle = itemView.findViewById(R.id.ivReorderHandle);
        }
    }
}
