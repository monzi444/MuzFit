package com.example.muzfit.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_ID = "EXTRA_WORKOUT_ID";
    public static final String EXTRA_WORKOUT_NAME = "EXTRA_WORKOUT_NAME";
    public static final String EXTRA_UID = "EXTRA_UID";

    private RecyclerView rvWorkoutDetails;
    private DetailsAdapter adapter;
    private final List<ExerciseDetail> detailsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history_details);

        int workoutId = getIntent().getIntExtra(EXTRA_WORKOUT_ID, -1);
        String workoutName = getIntent().getStringExtra(EXTRA_WORKOUT_NAME);
        String uid = getIntent().getStringExtra(EXTRA_UID);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tvDetailWorkoutName);
        if (workoutName != null) tvTitle.setText(workoutName);

        rvWorkoutDetails = findViewById(R.id.rvWorkoutDetails);
        rvWorkoutDetails.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetailsAdapter(detailsList);
        rvWorkoutDetails.setAdapter(adapter);

        if (workoutId != -1 && uid != null) {
            loadDetails(workoutId, uid);
        }
    }

    private void loadDetails(int workoutId, String uid) {
        new Thread(() -> {
            MuzFitDao dao = MuzFitDatabase.getInstance(this).muzFitDao();
            List<WorkoutExercise> wes = dao.getWorkoutExercises(workoutId, uid);
            List<ExerciseDetail> items = new ArrayList<>();

            for (WorkoutExercise we : wes) {
                Exercise exercise = dao.getExercise(we.getExerciseId());
                if (exercise != null) {
                    List<ExerciseSet> sets = dao.getExerciseSets(workoutId, uid, exercise.getId());
                    items.add(new ExerciseDetail(exercise, sets));
                }
            }

            runOnUiThread(() -> {
                detailsList.clear();
                detailsList.addAll(items);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private static class ExerciseDetail {
        final Exercise exercise;
        final List<ExerciseSet> sets;
        ExerciseDetail(Exercise exercise, List<ExerciseSet> sets) {
            this.exercise = exercise;
            this.sets = sets;
        }
    }

    private static class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {
        private final List<ExerciseDetail> items;

        DetailsAdapter(List<ExerciseDetail> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_exercise_detail, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ExerciseDetail item = items.get(position);
            holder.tvName.setText(item.exercise.getName());
            holder.llSetsContainer.removeAllViews();

            for (ExerciseSet set : item.sets) {
                View setRow = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_history_set_row, holder.llSetsContainer, false);
                
                TextView tvNum = setRow.findViewById(R.id.tvDetailSetNumber);
                TextView tvVal = setRow.findViewById(R.id.tvDetailSetValue);

                tvNum.setText(String.valueOf(set.getId()));
                
                String value;
                if (set.getWeight() > 0) {
                    value = String.format(Locale.getDefault(), "%d reps x %.2f kg", set.getRepetitions(), set.getWeight());
                } else {
                    value = String.format(Locale.getDefault(), "%d reps", set.getRepetitions());
                }
                tvVal.setText(value);
                
                holder.llSetsContainer.addView(setRow);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvName;
            final LinearLayout llSetsContainer;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvDetailExerciseName);
                llSetsContainer = itemView.findViewById(R.id.llSetsDetailContainer);
            }
        }
    }
}
