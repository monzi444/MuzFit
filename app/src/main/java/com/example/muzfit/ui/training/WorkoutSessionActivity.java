package com.example.muzfit.ui.training;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

import com.example.muzfit.R;
import com.example.muzfit.utils.ThemeHelper;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.model.Exercise;

public class WorkoutSessionActivity extends AppCompatActivity {

    private WorkoutRoutine routine;
    private int currentExerciseIndex = 0;

    private TextView tvExerciseName, tvInstructions, tvTimerDisplay, tvTimerOverlayDisplay;
    private TextView tvHeaderWeight;
    private ImageView ivExerciseGif;
    private LinearLayout llSetsContainer;
    private View llTimerPill, llTimerOverlay;
    private View llPickerContainer, rlCircleContainer;
    private NumberPicker npTimerMinutes, npTimerSeconds;
    private CircularProgressIndicator cpTimerProgress;
    private Button btnAddSet, btnSkipExercise;
    private MaterialButton btnAutoRestToggle;
    private ImageButton btnPauseResumeTimer;
    private ImageButton btnResetTimer, btnOverlayClose;
    private Button btnPreset30s, btnPreset1m, btnPreset1_30m, btnCustomTime;
    private boolean isTimerPaused = false;
    private boolean isAutoRestEnabled = true;
    private long timerMillisRemaining = 0;
    private int originalRestSeconds = 0;
    private Button btnToggleExecution;
    private View cvSessionExerciseGif;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private final Map<String, List<com.example.muzfit.model.ExerciseSet>> workoutResults = new HashMap<>();
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
        preloadExerciseGifs();
        loadExercise(currentExerciseIndex);
    }

    private void preloadExerciseGifs() {
        if (routine == null || routine.getExercises() == null) return;
        
        for (Exercise exercise : routine.getExercises()) {
            if (exercise.getGifUrl() != null && !exercise.getGifUrl().trim().isEmpty()) {
                Glide.with(this)
                        .load(exercise.getGifUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }
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
        llTimerPill = pageSets.findViewById(R.id.llTimerPill);
        llTimerOverlay = pageSets.findViewById(R.id.llTimerOverlay);
        llPickerContainer = pageSets.findViewById(R.id.llPickerContainer);
        rlCircleContainer = pageSets.findViewById(R.id.rlCircleContainer);
        npTimerMinutes = pageSets.findViewById(R.id.npMinutes);
        npTimerSeconds = pageSets.findViewById(R.id.npSeconds);
        cpTimerProgress = pageSets.findViewById(R.id.cpTimerProgress);
        tvTimerOverlayDisplay = pageSets.findViewById(R.id.tvTimerOverlayDisplay);
        btnAddSet = pageSets.findViewById(R.id.btnAddSet);
        tvTimerDisplay = pageSets.findViewById(R.id.tvTimerDisplay);
        tvHeaderWeight = pageSets.findViewById(R.id.tvHeaderWeight);
        btnAutoRestToggle = pageSets.findViewById(R.id.btnAutoRestToggle);
        btnPauseResumeTimer = pageSets.findViewById(R.id.btnPauseResumeTimer);
        btnResetTimer = pageSets.findViewById(R.id.btnResetTimer);
        btnOverlayClose = pageSets.findViewById(R.id.btnOverlayClose);
        btnPreset30s = pageSets.findViewById(R.id.btnPreset30s);
        btnPreset1m = pageSets.findViewById(R.id.btnPreset1m);
        btnPreset1_30m = pageSets.findViewById(R.id.btnPreset1_30m);
        btnCustomTime = pageSets.findViewById(R.id.btnCustomTime);

        // Setup Timer Pickers
        npTimerMinutes.setMinValue(0);
        npTimerMinutes.setMaxValue(60);
        npTimerSeconds.setMinValue(0);
        npTimerSeconds.setMaxValue(59);
        npTimerMinutes.setWrapSelectorWheel(true);
        npTimerSeconds.setWrapSelectorWheel(true);

        // Listeners
        btnAddSet.setOnClickListener(v -> addSetView());
        btnAutoRestToggle.setOnClickListener(v -> toggleAutoRest());
        llTimerPill.setOnClickListener(v -> handleTimerClick());
        llTimerOverlay.setOnClickListener(v -> cancelTimer());
        btnOverlayClose.setOnClickListener(v -> cancelTimer());
        btnResetTimer.setOnClickListener(v -> resetTimerToOriginal());
        btnPauseResumeTimer.setOnClickListener(v -> {
            if (restTimer == null || (llPickerContainer.getVisibility() == View.VISIBLE && !isTimerPaused)) {
                startTimerFromPickers();
            } else {
                toggleTimerPause();
            }
        });
        btnSkipExercise.setOnClickListener(v -> moveToNextExercise());

        btnPreset30s.setOnClickListener(v -> startRestTimer(30));
        btnPreset1m.setOnClickListener(v -> startRestTimer(60));
        btnPreset1_30m.setOnClickListener(v -> startRestTimer(90));
        btnCustomTime.setOnClickListener(v -> showPickersInOverlay());
        btnToggleExecution.setOnClickListener(v -> {
            if (cvSessionExerciseGif.getVisibility() == View.VISIBLE) {
                cvSessionExerciseGif.setVisibility(View.GONE);
                btnToggleExecution.setText("See Execution");
            } else {
                cvSessionExerciseGif.setVisibility(View.VISIBLE);
                btnToggleExecution.setText("Hide Execution");
            }
        });

        // Setup ViewPager2 adapter (Swapped order: Sets is first, Instructions is second)
        viewPager.setAdapter(new WorkoutSessionPagerAdapter(pageSets, pageInstructions));

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Sets");
            } else {
                tab.setText("Instructions");
            }
        }).attach();
    }

    private void loadExercise(int index) {
        if (index >= routine.getExercises().size()) {
            Toast.makeText(this, R.string.workout_complete_toast, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Reset ViewPager to first tab (Sets)
        if (viewPager != null) {
            viewPager.setCurrentItem(0, false);
        }

        Exercise exercise = routine.getExercises().get(index);
        tvExerciseName.setText(exercise.getName());

        // Toggle weight header visibility
        if (tvHeaderWeight != null) {
            boolean isBodyWeight = "body weight".equalsIgnoreCase(exercise.getEquipment());
            tvHeaderWeight.setVisibility(isBodyWeight ? View.GONE : View.VISIBLE);
        }

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
                btnToggleExecution.setText("See Execution");
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
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivExerciseGif);
        }

        llSetsContainer.removeAllViews();
        for (int i = 0; i < 3; i++) {
            addSetView();
        }

        btnSkipExercise.setText(index == routine.getExercises().size() - 1 ? "Finish Workout"
                : getString(R.string.skip_exercise));
    }

    private void addSetView() {
        View setView = LayoutInflater.from(this).inflate(R.layout.list_item_session_set, llSetsContainer, false);
        TextView tvSetNumber = setView.findViewById(R.id.tvSetNumber);
        TextView tvWeightSelector = setView.findViewById(R.id.tvWeightSelector);
        TextView tvRepsSelector = setView.findViewById(R.id.tvRepsSelector);
        CheckBox cbSetDone = setView.findViewById(R.id.cbSetDone);
        View btnDeleteSet = setView.findViewById(R.id.btnDeleteSet);

        int setNumber = llSetsContainer.getChildCount() + 1;
        tvSetNumber.setText(String.valueOf(setNumber));

        // Initial values: Tag stores [intWeight, decimalIndex]
        tvWeightSelector.setTag(new int[]{20, 0});
        tvWeightSelector.setText("20.0 kg");
        
        tvRepsSelector.setTag(10);
        tvRepsSelector.setText("10 reps");

        String[] decimals = {".0", ".25", ".5", ".75"};

        tvWeightSelector.setOnClickListener(v -> {
            int[] current = (int[]) v.getTag();
            showWeightPickerDialog(current[0], current[1], decimals, (newWeight, newDecimalIndex) -> {
                v.setTag(new int[]{newWeight, newDecimalIndex});
                ((TextView)v).setText(newWeight + decimals[newDecimalIndex] + " kg");
            });
        });

        tvRepsSelector.setOnClickListener(v -> showValuePickerDialog("Reps", 1, 100, (int)v.getTag(), value -> {
            v.setTag(value);
            ((TextView)v).setText(value + " reps");
        }));

        // Hide weight field if it's body weight
        Exercise exercise = routine.getExercises().get(currentExerciseIndex);
        if ("body weight".equalsIgnoreCase(exercise.getEquipment())) {
            tvWeightSelector.setVisibility(View.GONE);
        } else {
            tvWeightSelector.setVisibility(View.VISIBLE);
        }

        btnDeleteSet.setOnClickListener(v -> {
            llSetsContainer.removeView(setView);
            updateSetNumbers();
        });

        cbSetDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Auto-start rest timer (1 minute = 60 seconds) if enabled
                if (isAutoRestEnabled) {
                    startRestTimer(60);
                }
                checkAllSetsCompleted();
            }
        });

        llSetsContainer.addView(setView);
    }

    private void updateSetNumbers() {
        for (int i = 0; i < llSetsContainer.getChildCount(); i++) {
            View v = llSetsContainer.getChildAt(i);
            TextView tv = v.findViewById(R.id.tvSetNumber);
            tv.setText(String.valueOf(i + 1));
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

    private void toggleAutoRest() {
        isAutoRestEnabled = !isAutoRestEnabled;
        if (isAutoRestEnabled) {
            btnAutoRestToggle.setText("AutoRest: ON");
            btnAutoRestToggle.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
        } else {
            btnAutoRestToggle.setText("AutoRest: OFF");
            btnAutoRestToggle.setTextColor(ContextCompat.getColor(this, R.color.muz_on_surface_variant));
        }
    }

    private void moveToNextExercise() {
        cancelTimer();
        captureCurrentExerciseResults();
        if (currentExerciseIndex == routine.getExercises().size() - 1) {
            saveWorkoutToHistory();
        } else {
            currentExerciseIndex++;
            loadExercise(currentExerciseIndex);
        }
    }

    private void captureCurrentExerciseResults() {
        Exercise exercise = routine.getExercises().get(currentExerciseIndex);
        List<com.example.muzfit.model.ExerciseSet> sets = new ArrayList<>();
        String uid = com.example.muzfit.utils.RepositorySupport.currentUidOrDefault();

        for (int i = 0; i < llSetsContainer.getChildCount(); i++) {
            View setView = llSetsContainer.getChildAt(i);
            TextView tvWeight = setView.findViewById(R.id.tvWeightSelector);
            TextView tvReps = setView.findViewById(R.id.tvRepsSelector);
            CheckBox cbDone = setView.findViewById(R.id.cbSetDone);

            // Only save if marked as done
            if (cbDone.isChecked()) {
                int[] wTag = (int[]) tvWeight.getTag();
                int reps = (int) tvReps.getTag();
                
                String[] decimals = {".0", ".25", ".5", ".75"};
                double weight = wTag[0] + Double.parseDouble(decimals[wTag[1]]);
                
                sets.add(new com.example.muzfit.model.ExerciseSet(
                        i + 1, reps, weight, 0, uid, exercise.getId()
                ));
            }
        }
        workoutResults.put(exercise.getId(), sets);
    }

    private void saveWorkoutToHistory() {
        // Check if any sets were actually completed
        boolean anySetDone = false;
        for (List<com.example.muzfit.model.ExerciseSet> sets : workoutResults.values()) {
            if (sets != null && !sets.isEmpty()) {
                anySetDone = true;
                break;
            }
        }

        if (!anySetDone) {
            Toast.makeText(this, "Empty workout - not saved to history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int workoutId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        long now = System.currentTimeMillis();
        String uid = com.example.muzfit.utils.RepositorySupport.currentUidOrDefault();

        com.example.muzfit.model.Workout workout = new com.example.muzfit.model.Workout(
                workoutId, now, routine.getName(), uid
        );

        new Thread(() -> {
            com.example.muzfit.database.MuzFitDao dao = com.example.muzfit.database.MuzFitDatabase.getInstance(this).muzFitDao();
            dao.insertWorkout(workout);

            for (Exercise exercise : routine.getExercises()) {
                dao.insertWorkoutExercise(new com.example.muzfit.model.WorkoutExercise(
                        0, workoutId, uid, exercise.getId()
                ));
                
                List<com.example.muzfit.model.ExerciseSet> sets = workoutResults.get(exercise.getId());
                if (sets != null) {
                    for (com.example.muzfit.model.ExerciseSet s : sets) {
                        s.setWorkoutId(workoutId);
                        dao.insertExerciseSet(s);
                    }
                }
            }

            // Sync with Firebase
            com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            com.example.muzfit.source.firebase.FirestoreSyncDataSource firebaseSource = new com.example.muzfit.source.firebase.FirestoreSyncDataSource(firestore);
            firebaseSource.saveWorkoutHistory(uid, workout, routine.getExercises(), workoutResults);

            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved to history!", Toast.LENGTH_LONG).show();
                finish();
            });
        }).start();
    }

    private void handleTimerClick() {
        if (llTimerOverlay != null) {
            llTimerOverlay.setVisibility(View.VISIBLE);
            if (restTimer == null) {
                showPickersInOverlay();
            } else {
                showCircleInOverlay();
            }
        }
    }

    private void showPickersInOverlay() {
        llPickerContainer.setVisibility(View.VISIBLE);
        rlCircleContainer.setVisibility(View.GONE);
        btnPauseResumeTimer.setImageResource(android.R.drawable.ic_media_play);
        
        int mins = lastRestSeconds / 60;
        int secs = lastRestSeconds % 60;
        npTimerMinutes.setValue(mins);
        npTimerSeconds.setValue(secs);
    }

    private void showCircleInOverlay() {
        llPickerContainer.setVisibility(View.GONE);
        rlCircleContainer.setVisibility(View.VISIBLE);
        if (isTimerPaused) {
            btnPauseResumeTimer.setImageResource(android.R.drawable.ic_media_play);
        } else {
            btnPauseResumeTimer.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void startTimerFromPickers() {
        int totalSeconds = (npTimerMinutes.getValue() * 60) + npTimerSeconds.getValue();
        if (totalSeconds > 0) {
            lastRestSeconds = totalSeconds;
            startRestTimer(lastRestSeconds);
        }
    }

    private void cancelTimer() {
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
        }
        if (llTimerOverlay != null) {
            llTimerOverlay.setVisibility(View.GONE);
        }
        tvTimerDisplay.setText("00:00");
    }

    private int lastRestSeconds = 60;

    private void showWeightPickerDialog(int currentWeight, int currentDecimalIndex, String[] decimals, OnWeightSelectedListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_MuzFit_Dialog_Glass)
                .setTitle("Select Weight")
                .create();

        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(this, R.style.MuzFitNumberPicker);
        NumberPicker npWeight = new NumberPicker(themedContext);
        npWeight.setMinValue(0);
        npWeight.setMaxValue(300);
        npWeight.setValue(currentWeight);
        npWeight.setWrapSelectorWheel(true);

        NumberPicker npDecimal = new NumberPicker(themedContext);
        npDecimal.setMinValue(0);
        npDecimal.setMaxValue(decimals.length - 1);
        npDecimal.setDisplayedValues(decimals);
        npDecimal.setValue(currentDecimalIndex);
        npDecimal.setWrapSelectorWheel(true);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(0, 40, 0, 40);
        container.addView(npWeight);
        
        TextView dot = new TextView(this);
        dot.setText(".");
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        dot.setTextColor(ContextCompat.getColor(this, R.color.muz_on_surface));
        dot.setPadding(10, 0, 10, 0);
        container.addView(dot);
        
        container.addView(npDecimal);

        dialog.setView(container);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Set", (d, which) -> listener.onWeightSelected(npWeight.getValue(), npDecimal.getValue()));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());

        dialog.show();
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) posBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
        Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negBtn != null) negBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_on_surface_variant));
    }

    interface OnWeightSelectedListener {
        void onWeightSelected(int weight, int decimalIndex);
    }

    private void showValuePickerDialog(String title, int min, int max, int current, OnValueSelectedListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_MuzFit_Dialog_Glass)
                .setTitle(title)
                .create();

        NumberPicker np = new NumberPicker(new ContextThemeWrapper(this, R.style.MuzFitNumberPicker));
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(current);
        np.setWrapSelectorWheel(true);

        LinearLayout container = new LinearLayout(this);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(0, 40, 0, 40);
        container.addView(np);

        dialog.setView(container);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Set", (d, which) -> listener.onValueSelected(np.getValue()));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());

        dialog.show();
        
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) posBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
        Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negBtn != null) negBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_on_surface_variant));
    }

    private void showStringPickerDialog(String title, String[] values, int currentIndex, OnValueSelectedListener listener) {
        NumberPicker np = new NumberPicker(new ContextThemeWrapper(this, R.style.MuzFitNumberPicker));
        np.setMinValue(0);
        np.setMaxValue(values.length - 1);
        np.setDisplayedValues(values);
        np.setValue(currentIndex);
        np.setWrapSelectorWheel(true);

        LinearLayout container = new LinearLayout(this);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(0, 40, 0, 0);
        container.addView(np);

        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_MuzFit_Dialog_Glass))
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Set", (d, which) -> listener.onValueSelected(np.getValue()))
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
        
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) posBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
    }

    interface OnValueSelectedListener {
        void onValueSelected(int value);
    }

    private void toggleTimerPause() {
        if (restTimer == null) return;

        if (isTimerPaused) {
            // Resume
            isTimerPaused = false;
            btnPauseResumeTimer.setImageResource(android.R.drawable.ic_media_pause);
            startCountdown(timerMillisRemaining);
        } else {
            // Pause
            isTimerPaused = true;
            restTimer.cancel();
            btnPauseResumeTimer.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void resetTimerToOriginal() {
        if (originalRestSeconds > 0) {
            startRestTimer(originalRestSeconds);
        }
    }

    private void startRestTimer(int seconds) {
        if (restTimer != null) {
            restTimer.cancel();
        }

        originalRestSeconds = seconds;
        lastRestSeconds = seconds; // Update last used
        isTimerPaused = false;
        showCircleInOverlay();

        if (llTimerOverlay != null) {
            llTimerOverlay.setVisibility(View.VISIBLE);
            cpTimerProgress.setIndicatorDirection(CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE);
            cpTimerProgress.setMax(seconds);
            cpTimerProgress.setProgress(seconds);
        }

        startCountdown(seconds * 1000L);
    }

    private void startCountdown(long millis) {
        if (restTimer != null) {
            restTimer.cancel();
        }

        restTimer = new CountDownTimer(millis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerMillisRemaining = millisUntilFinished;
                int s = (int) (Math.ceil(millisUntilFinished / 1000.0));
                int m = s / 60;
                int displayS = s % 60;
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", m, displayS);
                tvTimerDisplay.setText(timeStr);
                tvTimerOverlayDisplay.setText(timeStr);
                cpTimerProgress.setProgress(s);
            }

            @Override
            public void onFinish() {
                tvTimerDisplay.setText("00:00");
                tvTimerOverlayDisplay.setText("00:00");
                cpTimerProgress.setProgress(0);
                if (llTimerOverlay != null) {
                    llTimerOverlay.setVisibility(View.GONE);
                }
                restTimer = null;
                isTimerPaused = false;
                playTimerFinishedSound();
                Toast.makeText(WorkoutSessionActivity.this, R.string.rest_over_toast, Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void playTimerFinishedSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        private final View pageSets;
        private final View pageInstructions;

        public WorkoutSessionPagerAdapter(View pageSets, View pageInstructions) {
            this.pageSets = pageSets;
            this.pageInstructions = pageInstructions;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = (viewType == 0) ? pageSets : pageInstructions;
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
