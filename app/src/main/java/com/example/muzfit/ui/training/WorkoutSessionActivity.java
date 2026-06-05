package com.example.muzfit.ui.training;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

import com.example.muzfit.R;
import com.example.muzfit.utils.ThemeHelper;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.model.Exercise;

public class WorkoutSessionActivity extends AppCompatActivity {

    private WorkoutRoutine routine;
    private int currentExerciseIndex = 0;

    private TextView tvExerciseName, tvInstructions, tvTimerDisplay;
    private ImageView ivExerciseGif;
    private LinearLayout llSetsContainer;
    private EditText etRestSeconds;
    private Button btnAddSet, btnSkipExercise;
    private Button btnToggleExecution;
    private View cvSessionExerciseGif;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private CountDownTimer restTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_session);

        routine = (WorkoutRoutine) getIntent().getSerializableExtra("EXTRA_ROUTINE");
        if (routine == null || routine.getExercises().isEmpty()) {
            finish();
            return;
        }

        initViews();
        loadExercise(currentExerciseIndex);
    }

    private void initViews() {
        tvExerciseName = findViewById(R.id.tvSessionExerciseName);
        btnSkipExercise = findViewById(R.id.btnSkipExercise);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Inflate the 2 pages of the carousel
        View pageInstructions = LayoutInflater.from(this).inflate(R.layout.page_session_instructions, null);
        View pageSets = LayoutInflater.from(this).inflate(R.layout.page_session_sets, null);

        // Bind instructions page elements
        btnToggleExecution = pageInstructions.findViewById(R.id.btnToggleExecution);
        cvSessionExerciseGif = pageInstructions.findViewById(R.id.cvSessionExerciseGif);
        ivExerciseGif = pageInstructions.findViewById(R.id.ivSessionExerciseGif);
        tvInstructions = pageInstructions.findViewById(R.id.tvSessionInstructions);

        // Bind sets page elements
        llSetsContainer = pageSets.findViewById(R.id.llSetsContainer);
        etRestSeconds = pageSets.findViewById(R.id.etRestSeconds);
        btnAddSet = pageSets.findViewById(R.id.btnAddSet);
        tvTimerDisplay = pageSets.findViewById(R.id.tvTimerDisplay);
        Button btnStartTimer = pageSets.findViewById(R.id.btnStartTimer);

        // Listeners
        btnAddSet.setOnClickListener(v -> addSetView());
        btnStartTimer.setOnClickListener(v -> startRestTimer());
        btnSkipExercise.setOnClickListener(v -> moveToNextExercise());
        btnToggleExecution.setOnClickListener(v -> {
            if (cvSessionExerciseGif.getVisibility() == View.VISIBLE) {
                cvSessionExerciseGif.setVisibility(View.GONE);
                btnToggleExecution.setText(R.string.see_execution);
            } else {
                cvSessionExerciseGif.setVisibility(View.VISIBLE);
                btnToggleExecution.setText(R.string.hide_execution);
            }
        });

        // Setup ViewPager2 adapter
        viewPager.setAdapter(new WorkoutSessionPagerAdapter(pageInstructions, pageSets));

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Istruzioni");
            } else {
                tab.setText("Serie");
            }
        }).attach();
    }

    private void loadExercise(int index) {
        if (index >= routine.getExercises().size()) {
            Toast.makeText(this, R.string.workout_complete_toast, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Reset ViewPager to first tab
        if (viewPager != null) {
            viewPager.setCurrentItem(0, false);
        }

        Exercise exercise = routine.getExercises().get(index);
        tvExerciseName.setText(exercise.getName());

        StringBuilder instr = new StringBuilder();
        if (exercise.getInstructions() != null) {
            for (int i = 0; i < exercise.getInstructions().size(); i++) {
                instr.append(i + 1).append(". ").append(exercise.getInstructions().get(i)).append("\n\n");
            }
        }
        tvInstructions.setText(instr.toString().trim());

        // Check if GIF is present and dynamically show/hide the toggle button
        boolean hasGif = exercise.getGifUrl() != null && !exercise.getGifUrl().trim().isEmpty();
        if (btnToggleExecution != null) {
            if (hasGif) {
                btnToggleExecution.setVisibility(View.VISIBLE);
                btnToggleExecution.setText(R.string.see_execution);
            } else {
                btnToggleExecution.setVisibility(View.GONE);
            }
        }
        if (cvSessionExerciseGif != null) {
            cvSessionExerciseGif.setVisibility(View.GONE);
        }

        if (hasGif) {
            Glide.with(this)
                    .load(exercise.getGifUrl())
                    .placeholder(R.drawable.ic_exercise_placeholder)
                    .into(ivExerciseGif);
        }

        llSetsContainer.removeAllViews();
        for (int i = 0; i < 3; i++) {
            addSetView();
        }

        btnSkipExercise.setText(index == routine.getExercises().size() - 1 ? getString(R.string.finish_workout)
                : getString(R.string.skip_exercise));
    }

    private void addSetView() {
        View setView = LayoutInflater.from(this).inflate(R.layout.list_item_session_set, llSetsContainer, false);
        TextView tvSetNumber = setView.findViewById(R.id.tvSetNumber);
        EditText etReps = setView.findViewById(R.id.etReps);
        EditText etWeight = setView.findViewById(R.id.etWeight);
        CheckBox cbSetDone = setView.findViewById(R.id.cbSetDone);
        View btnDeleteSet = setView.findViewById(R.id.btnDeleteSet);

        int setNumber = llSetsContainer.getChildCount() + 1;
        tvSetNumber.setText(getString(R.string.set_label, setNumber));

        // Default reps to 3
        etReps.setText("3");

        // Hide weight field if it's body weight
        Exercise exercise = routine.getExercises().get(currentExerciseIndex);
        if ("body weight".equalsIgnoreCase(exercise.getEquipment())) {
            etWeight.setVisibility(View.GONE);
        } else {
            etWeight.setVisibility(View.VISIBLE);
        }

        btnDeleteSet.setOnClickListener(v -> {
            llSetsContainer.removeView(setView);
            updateSetNumbers();
        });

        cbSetDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAllSetsCompleted();
            }
        });

        llSetsContainer.addView(setView);
    }

    private void updateSetNumbers() {
        for (int i = 0; i < llSetsContainer.getChildCount(); i++) {
            View v = llSetsContainer.getChildAt(i);
            TextView tv = v.findViewById(R.id.tvSetNumber);
            tv.setText(getString(R.string.set_label, i + 1));
        }
    }

    private void checkAllSetsCompleted() {
        boolean allDone = true;
        for (int i = 0; i < llSetsContainer.getChildCount(); i++) {
            View v = llSetsContainer.getChildAt(i);
            CheckBox cb = v.findViewById(R.id.cbSetDone);
            if (!cb.isChecked()) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            moveToNextExercise();
        }
    }

    private void moveToNextExercise() {
        currentExerciseIndex++;
        loadExercise(currentExerciseIndex);
    }

    private void startRestTimer() {
        if (restTimer != null) {
            restTimer.cancel();
        }

        String secStr = etRestSeconds.getText().toString();
        int seconds = secStr.isEmpty() ? 60 : Integer.parseInt(secStr);

        restTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int s = (int) (millisUntilFinished / 1000);
                int m = s / 60;
                s = s % 60;
                tvTimerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
            }

            @Override
            public void onFinish() {
                tvTimerDisplay.setText("00:00");
                Toast.makeText(WorkoutSessionActivity.this, R.string.rest_over_toast, Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (restTimer != null) {
            restTimer.cancel();
        }
    }

    // A simple pager adapter to display static page views in ViewPager2
    private static class WorkoutSessionPagerAdapter
            extends RecyclerView.Adapter<WorkoutSessionPagerAdapter.PageViewHolder> {
        private final View pageInstructions;
        private final View pageSets;

        public WorkoutSessionPagerAdapter(View pageInstructions, View pageSets) {
            this.pageInstructions = pageInstructions;
            this.pageSets = pageSets;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = (viewType == 0) ? pageInstructions : pageSets;
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        static class PageViewHolder extends RecyclerView.ViewHolder {
            public PageViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
