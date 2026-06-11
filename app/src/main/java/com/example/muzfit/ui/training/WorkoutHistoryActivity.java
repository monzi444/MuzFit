package com.example.muzfit.ui.training;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.utils.RepositorySupport;
import com.example.muzfit.utils.ThemeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private com.example.muzfit.ui.training.viewmodel.TrainingViewModel viewModel;
    private HistoryAdapter adapter;
    private final List<Object> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        com.example.muzfit.ui.training.viewmodel.TrainingViewModelFactory factory = new com.example.muzfit.ui.training.viewmodel.TrainingViewModelFactory(
                com.example.muzfit.utils.ServiceLocator.getInstance().getTrainingRepository()
        );
        viewModel = new ViewModelProvider(this, factory).get(com.example.muzfit.ui.training.viewmodel.TrainingViewModel.class);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        RecyclerView rvWorkoutHistory = findViewById(R.id.rvWorkoutHistory);
        rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(historyItems, item -> {
            Intent intent = new Intent(this, WorkoutHistoryDetailsActivity.class);
            intent.putExtra(WorkoutHistoryDetailsActivity.EXTRA_WORKOUT_ID, item.workout.getId());
            intent.putExtra(WorkoutHistoryDetailsActivity.EXTRA_WORKOUT_NAME, item.workout.getDescription());
            intent.putExtra(WorkoutHistoryDetailsActivity.EXTRA_UID, item.workout.getUid());
            startActivity(intent);
        });
        rvWorkoutHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        viewModel.getWorkouts().observe(this, result -> {
            if (result.isLoading()) return;
            if (result.isError()) return;

            List<Workout> workouts = ((com.example.muzfit.model.Result.Success<List<Workout>>) result).getData();
            if (workouts == null) return;

            new Thread(() -> {
                String uid = RepositorySupport.currentUidOrDefault();
                MuzFitDao dao = MuzFitDatabase.getInstance(this).muzFitDao();
                
                List<Object> items = new ArrayList<>();
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                String lastMonth = "";

                for (Workout w : workouts) {
                    String currentMonth = monthFormat.format(new Date(w.getDateMillis()));
                    if (!currentMonth.equals(lastMonth)) {
                        items.add(currentMonth);
                        lastMonth = currentMonth;
                    }

                    List<WorkoutExercise> wes = dao.getWorkoutExercises(w.getId(), uid);
                    StringBuilder exercisesStr = new StringBuilder();
                    for (int i = 0; i < wes.size(); i++) {
                        com.example.muzfit.model.Exercise e = dao.getExercise(wes.get(i).getExerciseId());
                        if (e != null) {
                            exercisesStr.append(e.getName());
                            if (i < wes.size() - 1) exercisesStr.append(", ");
                        }
                    }
                    items.add(new HistoryItem(w, exercisesStr.toString()));
                }

                runOnUiThread(() -> {
                    historyItems.clear();
                    historyItems.addAll(items);
                    adapter.notifyDataSetChanged();
                });
            }).start();
        });
    }

    private static class HistoryItem {
        final Workout workout;
        final String exercises;
        HistoryItem(Workout workout, String exercises) {
            this.workout = workout;
            this.exercises = exercises;
        }
    }

    private interface OnHistoryItemClickListener {
        void onHistoryItemClick(HistoryItem item);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private final List<Object> items;
        private final OnHistoryItemClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());

        HistoryAdapter(List<Object> items, OnHistoryItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_date_header, parent, false);
                return new HeaderViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_history, parent, false);
                return new ItemViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) holder).tvHeader.setText((String) items.get(position));
            } else {
                HistoryItem item = (HistoryItem) items.get(position);
                ItemViewHolder itemHolder = (ItemViewHolder) holder;
                itemHolder.tvName.setText(item.workout.getDescription());
                itemHolder.tvDate.setText(dateFormat.format(new Date(item.workout.getDateMillis())));
                itemHolder.tvExercises.setText(item.exercises);
                itemHolder.itemView.setOnClickListener(v -> listener.onHistoryItemClick(item));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {
            final TextView tvHeader;
            HeaderViewHolder(View itemView) {
                super(itemView);
                tvHeader = (TextView) itemView;
            }
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {
            final TextView tvName, tvDate, tvExercises;
            ItemViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvHistoryWorkoutName);
                tvDate = itemView.findViewById(R.id.tvHistoryDate);
                tvExercises = itemView.findViewById(R.id.tvHistoryExercises);
            }
        }
    }
}
