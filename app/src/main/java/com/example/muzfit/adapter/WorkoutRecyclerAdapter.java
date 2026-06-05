package com.example.muzfit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.model.WorkoutRoutine;

import java.util.List;

public class WorkoutRecyclerAdapter extends RecyclerView.Adapter<WorkoutRecyclerAdapter.ViewHolder> {

    private final List<WorkoutRoutine> routines;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public WorkoutRecyclerAdapter(List<WorkoutRoutine> routines, OnItemClickListener listener) {
        this.routines = routines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutRoutine routine = routines.get(position);
        holder.tvName.setText(routine.getName());
        holder.tvDetails.setText(routine.getExerciseSummary());

        holder.itemView.setActivated(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onItemClick(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void clearSelection() {
        int oldPos = selectedPosition;
        selectedPosition = -1;
        notifyItemChanged(oldPos);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.routineNameTextView);
            tvDetails = itemView.findViewById(R.id.routineDetailsTextView);
        }
    }
}
