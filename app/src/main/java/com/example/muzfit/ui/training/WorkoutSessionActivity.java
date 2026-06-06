package com.example.muzfit.ui.training;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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

    private TextView tvExerciseName, tvInstructions, tvTimerDisplay, tvTimerOverlayDisplay;
    private TextView tvHeaderWeight;
    private ImageView ivExerciseGif;
    private LinearLayout llSetsContainer;
    private View llTimerPill, llTimerOverlay;
    private CircularProgressIndicator cpTimerProgress;
    private Button btnAddSet, btnSkipExercise;
    private MaterialButton btnPauseResumeTimer;
    private boolean isTimerPaused = false;
    private long timerMillisRemaining = 0;
    private int originalRestSeconds = 0;
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
        llTimerPill = pageSets.findViewById(R.id.llTimerPill);
        llTimerOverlay = pageSets.findViewById(R.id.llTimerOverlay);
        cpTimerProgress = pageSets.findViewById(R.id.cpTimerProgress);
        tvTimerOverlayDisplay = pageSets.findViewById(R.id.tvTimerOverlayDisplay);
        btnAddSet = pageSets.findViewById(R.id.btnAddSet);
        tvTimerDisplay = pageSets.findViewById(R.id.tvTimerDisplay);
        tvHeaderWeight = pageSets.findViewById(R.id.tvHeaderWeight);
        btnPauseResumeTimer = pageSets.findViewById(R.id.btnPauseResumeTimer);

        // Listeners
        btnAddSet.setOnClickListener(v -> addSetView());
        llTimerPill.setOnClickListener(v -> handleTimerClick());
        llTimerOverlay.setOnClickListener(v -> cancelTimer());
        btnPauseResumeTimer.setOnClickListener(v -> toggleTimerPause());
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
                tab.setText("Instructions");
            } else {
                tab.setText("Sets");
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

    private void moveToNextExercise() {
        cancelTimer();
        currentExerciseIndex++;
        loadExercise(currentExerciseIndex);
    }

    private void handleTimerClick() {
        if (restTimer != null) {
            cancelTimer();
        } else {
            showTimerInputDialog();
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

    private void showTimerInputDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rest_time_picker, null);
        NumberPicker npMinutes = dialogView.findViewById(R.id.npMinutes);
        NumberPicker npSeconds = dialogView.findViewById(R.id.npSeconds);

        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(60);
        npSeconds.setMinValue(0);
        npSeconds.setMaxValue(59);
        npMinutes.setWrapSelectorWheel(true);
        npSeconds.setWrapSelectorWheel(true);

        // Pre-imposta i valori basandosi sull'ultimo recupero usato
        int mins = lastRestSeconds / 60;
        int secs = lastRestSeconds % 60;
        npMinutes.setValue(mins);
        npSeconds.setValue(secs);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Start", (d, which) -> {
                    int totalSeconds = (npMinutes.getValue() * 60) + npSeconds.getValue();
                    if (totalSeconds > 0) {
                        lastRestSeconds = totalSeconds;
                        startRestTimer(lastRestSeconds);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Style buttons to match app theme
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (posBtn != null) {
            posBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
            posBtn.setAllCaps(true);
        }
        if (negBtn != null) {
            negBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_on_surface_variant));
            negBtn.setAllCaps(true);
        }
    }

    private void showWeightPickerDialog(int currentWeight, int currentDecimalIndex, String[] decimals, OnWeightSelectedListener listener) {
        ContextThemeWrapper themedContext = new ContextThemeWrapper(this, R.style.MuzFitNumberPicker);
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
        container.setPadding(0, 40, 0, 0);
        container.addView(npWeight);
        
        TextView dot = new TextView(this);
        dot.setText(".");
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        dot.setTextColor(ContextCompat.getColor(this, R.color.muz_on_surface));
        dot.setPadding(10, 0, 10, 0);
        container.addView(dot);
        
        container.addView(npDecimal);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Weight")
                .setView(container)
                .setPositiveButton("Set", (d, which) -> listener.onWeightSelected(npWeight.getValue(), npDecimal.getValue()))
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) posBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
    }

    interface OnWeightSelectedListener {
        void onWeightSelected(int weight, int decimalIndex);
    }

    private void showValuePickerDialog(String title, int min, int max, int current, OnValueSelectedListener listener) {
        NumberPicker np = new NumberPicker(new ContextThemeWrapper(this, R.style.MuzFitNumberPicker));
        np.setMinValue(min);
        np.setMaxValue(max);
        np.setValue(current);
        np.setWrapSelectorWheel(true);

        LinearLayout container = new LinearLayout(this);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(0, 40, 0, 0);
        container.addView(np);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Set", (d, which) -> listener.onValueSelected(np.getValue()))
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
        
        Button posBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (posBtn != null) posBtn.setTextColor(ContextCompat.getColor(this, R.color.muz_primary_lime));
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

        AlertDialog dialog = new AlertDialog.Builder(this)
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
            btnPauseResumeTimer.setText("Pause");
            startCountdown(timerMillisRemaining);
        } else {
            // Pause
            isTimerPaused = true;
            restTimer.cancel();
            btnPauseResumeTimer.setText("Resume");
        }
    }

    private void startRestTimer(int seconds) {
        if (restTimer != null) {
            restTimer.cancel();
        }

        originalRestSeconds = seconds;
        isTimerPaused = false;
        btnPauseResumeTimer.setText("Pause");

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
