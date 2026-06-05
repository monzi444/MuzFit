# Refine Sets Page: Header Timer & Premium Sets List

We will refine the "Serie" (Sets) page of the workout session:
- **Timer Pill**: Replace the large timer card with a compact glass pill in the top-right header containing a stopwatch icon and countdown text.
- **Timer Control**: Tapping the timer pill while running will stop/reset the timer. Tapping it while stopped will show a dialog to enter the rest seconds and start the timer.
- **Sets List Headers**: Add clear column headers ("Set", "Weight (kg)", "Reps", "Done") above the list for better clarity.
- **Sleek Inputs**: Style the set rows with rounded input boxes, circular badges for set numbers, and aligned columns.

## User Review Required

> [!IMPORTANT]
> - The Rest Timer is now a small pill ("⏱️ 00:00") in the top right.
> - Single tap on the running timer will immediately stop and reset it.
> - Single tap on the stopped timer will open an input dialog to configure the countdown duration.

## Proposed Changes

### Drawables
#### [NEW] [muzfit_input_bg.xml](file:///c:/Users/feede/Desktop/AppMobile/app/src/main/res/drawable/muzfit_input_bg.xml)
- Rounded shape with dark gray background and subtle borders to act as input field backgrounds.

### Layouts
#### [MODIFY] [page_session_sets.xml](file:///c:/Users/feede/Desktop/AppMobile/app/src/main/res/layout/page_session_sets.xml)
- Remove the old `cvTimerSection` block.
- Add `llTimerPill` layout (ImageView for timer icon, TextView for time display) in the top-right header next to `btnAddSet`.
- Add a horizontal LinearLayout for column headers ("Set", "Weight (kg)", "Reps", "Done") matching the set items alignment.

#### [MODIFY] [list_item_session_set.xml](file:///c:/Users/feede/Desktop/AppMobile/app/src/main/res/layout/list_item_session_set.xml)
- Style `tvSetNumber` as a round/badge view.
- Apply `@drawable/muzfit_input_bg` to `etWeight` and `etReps`, fixing their heights, vertical centering, and internal paddings.
- Align columns to match the new headers.

### Java Logic
#### [MODIFY] [WorkoutSessionActivity.java](file:///c:/Users/feede/Desktop/AppMobile/app/src/main/java/com/example/muzfit/ui/training/WorkoutSessionActivity.java)
- Bind the header timer pill (`llTimerPill`, `ivTimerIcon`, `tvTimerDisplay`).
- Update `startRestTimer` logic:
  - If the timer is already running, stop/cancel it and reset the UI state.
  - If the timer is stopped, show an AlertDialog prompting the user for rest duration (defaulting to the last used value, e.g. 60 seconds). Once confirmed, start the countdown.
- Automatically cancel and reset the timer if the activity is destroyed or if the exercise changes.

---

## Verification Plan

### Automated Tests
- Build and compile using:
  `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat compileDebugSources`

### Manual Verification
1. Open a workout routine and tap "Start Workout".
2. Swipe right to the "Serie" tab.
3. Verify that the table headers ("Set", "Weight (kg)", "Reps", "Done") are present and match the items below.
4. Verify that the rest timer is represented as a compact pill in the header.
5. Tap the timer pill: verify an alert dialog opens prompting for seconds. Type `10` and tap "Avvia" (Start).
6. Verify the countdown starts, showing in the header.
7. Tap the running timer pill: verify it cancels immediately and resets to `00:00`.
