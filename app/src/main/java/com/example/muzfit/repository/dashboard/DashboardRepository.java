package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.muzfit.database.MuzFitDao;
import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.model.DashboardCalendarDay;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.source.firebase.FirestoreSyncDataSource;
import com.example.muzfit.utils.RepositorySupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DashboardRepository implements IDashboardRepository {

    private static final int WEEK_DAYS = 7;
    private static final float CALORIE_GOAL_MARGIN = 200f;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final FirestoreSyncDataSource firestoreSyncDataSource;
    private MuzFitDao localDao;
    private Future<?> seedFuture;

    public DashboardRepository(FirestoreSyncDataSource firestoreSyncDataSource) {
        this.firestoreSyncDataSource = firestoreSyncDataSource;
    }

    public void setLocalDatabase(MuzFitDatabase database) {
        if (database != null) {
            localDao = database.muzFitDao();
        }
    }

    public void setSeedFuture(Future<?> seedFuture) {
        this.seedFuture = seedFuture;
    }

    @Override
    public LiveData<Result<Float>> getConsumedCalories() {
        return getConsumedCalories(System.currentTimeMillis());
    }

    @Override
    public LiveData<Result<Float>> getConsumedCalories(long dateMillis) {
        return getConsumedMacroFromDatabase(Macro.CALORIES, dateMillis);
    }

    @Override
    public LiveData<Result<Float>> getConsumedCarbs() {
        return getConsumedCarbs(System.currentTimeMillis());
    }

    @Override
    public LiveData<Result<Float>> getConsumedCarbs(long dateMillis) {
        return getConsumedMacroFromDatabase(Macro.CARBS, dateMillis);
    }

    @Override
    public LiveData<Result<Float>> getConsumedProteins() {
        return getConsumedProteins(System.currentTimeMillis());
    }

    @Override
    public LiveData<Result<Float>> getConsumedProteins(long dateMillis) {
        return getConsumedMacroFromDatabase(Macro.PROTEINS, dateMillis);
    }

    @Override
    public LiveData<Result<Float>> getConsumedFats() {
        return getConsumedFats(System.currentTimeMillis());
    }

    @Override
    public LiveData<Result<Float>> getConsumedFats(long dateMillis) {
        return getConsumedMacroFromDatabase(Macro.FATS, dateMillis);
    }

    @Override
    public LiveData<Result<User>> getMacroGoals() {
        MutableLiveData<Result<User>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                User user = RepositorySupport.ensureLocalUser(localDao, uid);
                if (user == null) {
                    liveData.postValue(new Result.Error<>("User not found"));
                    return;
                }
                liveData.postValue(new Result.Success<>(user));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<WeightEntry>>> getWeights() {
        if (localDao == null) {
            MutableLiveData<Result<List<WeightEntry>>> liveData = new MutableLiveData<>();
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        String uid = RepositorySupport.currentUidOrDefault();
        return androidx.lifecycle.Transformations.map(localDao.getWeightEntries(uid), Result.Success::new);
    }

    @Override
    public LiveData<Result<int[]>> getDailyCaloriesBurned() {
        return getDailyCaloriesBurned(System.currentTimeMillis());
    }

    public LiveData<Result<int[]>> getDailyCaloriesBurned(long dateMillis) {
        MutableLiveData<Result<int[]>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                long startOfWeek = getStartOfCurrentWeekMillis(dateMillis);
                long endOfWeek = getEndOfDayMillis(startOfWeek + (6 * 24L * 60L * 60L * 1000L));
                List<Workout> workouts = localDao.getWorkoutsBetween(
                        uid,
                        startOfWeek,
                        endOfWeek
                );
                liveData.postValue(new Result.Success<>(buildDailyCaloriesBurned(uid, startOfWeek, workouts)));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<int[]>> getDailyCaloriesConsumed(long dateMillis) {
        MutableLiveData<Result<int[]>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                long startOfWeek = getStartOfCurrentWeekMillis(dateMillis);
                int[] dailyConsumed = new int[WEEK_DAYS];
                for (int i = 0; i < WEEK_DAYS; i++) {
                    long currentDayStart = startOfWeek + (i * 24L * 60L * 60L * 1000L);
                    long currentDayEnd = getEndOfDayMillis(currentDayStart);
                    dailyConsumed[i] = (int) localDao.getConsumedCalories(uid, currentDayStart, currentDayEnd);
                }
                liveData.postValue(new Result.Success<>(dailyConsumed));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Integer>> getCaloriesBurned(long dateMillis) {
        MutableLiveData<Result<Integer>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                long startOfDay = getStartOfDayMillis(dateMillis);
                long endOfDay = getEndOfDayMillis(startOfDay);
                List<Workout> workouts = localDao.getWorkoutsBetween(
                        uid,
                        startOfDay,
                        endOfDay
                );
                int total = 0;
                for (Workout workout : workouts) {
                    List<WorkoutExercise> workoutExercises = localDao.getWorkoutExercises(
                            workout.getId(),
                            uid
                    );
                    for (WorkoutExercise workoutExercise : workoutExercises) {
                        total += workoutExercise.getCalories();
                    }
                }
                liveData.postValue(new Result.Success<>(total));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<List<DashboardCalendarDay>>> getCalendarData(int year, int month) {
        MutableLiveData<Result<List<DashboardCalendarDay>>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                User user = RepositorySupport.ensureLocalUser(localDao, uid);
                float calorieGoal = user != null && user.getCalorieGoal() > 0
                        ? user.getCalorieGoal()
                        : 2000f;
                liveData.postValue(new Result.Success<>(buildCalendarData(
                        year,
                        month,
                        uid,
                        calorieGoal
                )));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    @Override
    public LiveData<Result<Void>> deleteWeightEntry(WeightEntry weightEntry) {
        MutableLiveData<Result<Void>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                weightEntry.setUid(uid);
                localDao.deleteWeightEntry(weightEntry);
                firestoreSyncDataSource.deleteWeightEntry(weightEntry);
                liveData.postValue(new Result.Success<>(null));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    private LiveData<Result<Float>> getConsumedMacroFromDatabase(Macro macro, long dateMillis) {
        MutableLiveData<Result<Float>> liveData = new MutableLiveData<>();
        liveData.setValue(new Result.Loading<>());
        if (localDao == null) {
            liveData.setValue(new Result.Error<>("Local database is not initialized"));
            return liveData;
        }

        EXECUTOR.execute(() -> {
            try {
                awaitSeedIfNeeded();
                String uid = RepositorySupport.currentUidOrDefault();
                long startOfDay = getStartOfDayMillis(dateMillis);
                long endOfDay = getEndOfDayMillis(startOfDay);
                liveData.postValue(new Result.Success<>(
                        getConsumedMacroValue(uid, macro, startOfDay, endOfDay)
                ));
            } catch (Exception e) {
                liveData.postValue(new Result.Error<>(e.getMessage()));
            }
        });
        return liveData;
    }

    private void awaitSeedIfNeeded() throws Exception {
        if (seedFuture != null) {
            seedFuture.get();
        }
    }

    private float getConsumedMacroValue(String uid, Macro macro, long startOfDayMillis, long endOfDayMillis) {
        switch (macro) {
            case CALORIES:
                return localDao.getConsumedCalories(uid, startOfDayMillis, endOfDayMillis);
            case CARBS:
                return localDao.getConsumedCarbs(uid, startOfDayMillis, endOfDayMillis);
            case PROTEINS:
                return localDao.getConsumedProteins(uid, startOfDayMillis, endOfDayMillis);
            case FATS:
                return localDao.getConsumedFats(uid, startOfDayMillis, endOfDayMillis);
            default:
                return 0f;
        }
    }

    private long getStartOfDayMillis(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDayMillis(long startOfDayMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfDayMillis);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    private long getStartOfCurrentWeekMillis(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getStartOfDayMillis(dateMillis));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offsetToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        calendar.add(Calendar.DAY_OF_YEAR, offsetToMonday);
        return calendar.getTimeInMillis();
    }

    private int[] buildDailyCaloriesBurned(String uid, long startOfWeekMillis, List<Workout> workouts) {
        int[] dailyCalories = new int[WEEK_DAYS];
        if (workouts == null) {
            return dailyCalories;
        }

        for (Workout workout : workouts) {
            int dayIndex = getDayOffset(startOfWeekMillis, workout.getDateMillis());
            if (dayIndex < 0 || dayIndex >= WEEK_DAYS) {
                continue;
            }

            int total = 0;
            List<WorkoutExercise> workoutExercises = localDao.getWorkoutExercises(
                    workout.getId(),
                    uid
            );
            for (WorkoutExercise workoutExercise : workoutExercises) {
                total += workoutExercise.getCalories();
            }
            dailyCalories[dayIndex] += total;
        }
        return dailyCalories;
    }

    private int getDayOffset(long startOfWeekMillis, long dateMillis) {
        long startOfWorkoutDay = getStartOfDayMillis(dateMillis);
        return (int) ((startOfWorkoutDay - startOfWeekMillis) / (24L * 60L * 60L * 1000L));
    }

    private long getStartOfMonthMillis(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfMonthMillis(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTimeInMillis();
    }

    private List<DashboardCalendarDay> buildCalendarData(
            int year,
            int month,
            String uid,
            float calorieGoal
    ) {
        List<DashboardCalendarDay> data = new ArrayList<>();

        Calendar firstDay = Calendar.getInstance();
        firstDay.set(year, month, 1);
        int firstDayOffset = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar previousMonth = (Calendar) firstDay.clone();
        previousMonth.add(Calendar.MONTH, -1);
        int daysInPreviousMonth = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (int i = firstDayOffset - 1; i >= 0; i--) {
            data.add(new DashboardCalendarDay(
                    daysInPreviousMonth - i,
                    DashboardCalendarDay.ActivityLevel.EMPTY,
                    false
            ));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            Calendar dayCal = Calendar.getInstance();
            dayCal.set(year, month, day, 0, 0, 0);
            dayCal.set(Calendar.MILLISECOND, 0);

            DashboardCalendarDay.ActivityLevel level = DashboardCalendarDay.ActivityLevel.EMPTY;
            if (!dayCal.after(today)) {
                long startOfDay = dayCal.getTimeInMillis();
                long endOfDay = getEndOfDayMillis(startOfDay);
                float consumed = localDao.getConsumedCalories(uid, startOfDay, endOfDay);
                if (consumed >= calorieGoal - CALORIE_GOAL_MARGIN && consumed <= calorieGoal + CALORIE_GOAL_MARGIN) {
                    level = DashboardCalendarDay.ActivityLevel.GOAL;
                } else if (consumed > calorieGoal + CALORIE_GOAL_MARGIN) {
                    level = DashboardCalendarDay.ActivityLevel.OVERFLOW;
                } else if (consumed >= calorieGoal * 0.5f) {
                    level = DashboardCalendarDay.ActivityLevel.PARTIAL;
                } else if (consumed > 0f) {
                    level = DashboardCalendarDay.ActivityLevel.NONE;
                }
            }
            data.add(new DashboardCalendarDay(day, level, true));
        }

        int nextMonthDay = 1;
        while (data.size() % 7 != 0) {
            data.add(new DashboardCalendarDay(
                    nextMonthDay,
                    DashboardCalendarDay.ActivityLevel.EMPTY,
                    false
            ));
            nextMonthDay++;
        }

        return data;
    }

    private enum Macro {
        CALORIES,
        CARBS,
        PROTEINS,
        FATS
    }
}
