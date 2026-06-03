package com.example.muzfit.ui.training;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;

import com.example.muzfit.R;
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
    
    private CountDownTimer restTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        tvInstructions = findViewById(R.id.tvSessionInstructions);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        ivExerciseGif = findViewById(R.id.ivSessionExerciseGif);
        llSetsContainer = findViewById(R.id.llSetsContainer);
        etRestSeconds = findViewById(R.id.etRestSeconds);
        btnAddSet = findViewById(R.id.btnAddSet);
        Button btnStartTimer = findViewById(R.id.btnStartTimer);
        btnSkipExercise = findViewById(R.id.btnSkipExercise);

        btnAddSet.setOnClickListener(v -> addSetView());
        btnStartTimer.setOnClickListener(v -> startRestTimer());
        btnSkipExercise.setOnClickListener(v -> moveToNextExercise());
    }

    private void loadExercise(int index) {
        if (index >= routine.getExercises().size()) {
            Toast.makeText(this, R.string.workout_complete_toast, Toast.LENGTH_LONG).show();
            finish();
            return;
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

        Glide.with(this)
                .load(exercise.getGifUrl())
                .placeholder(R.drawable.ic_exercise_placeholder)
                .into(ivExerciseGif);

        llSetsContainer.removeAllViews();
        for (int i = 0; i < 3; i++) {
            addSetView();
        }

        btnSkipExercise.setText(index == routine.getExercises().size() - 1 ? 
                getString(R.string.finish_workout) : getString(R.string.skip_exercise));
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
}
