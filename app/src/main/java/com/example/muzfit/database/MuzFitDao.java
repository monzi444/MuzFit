package com.example.muzfit.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;

import java.util.List;

@Dao
public interface MuzFitDao {

    @Query("SELECT * FROM User")
    List<User> getUsers();

    @Query("SELECT * FROM User WHERE uid = :uid LIMIT 1")
    User getUser(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM Meal ORDER BY foodName")
    List<Meal> getMeals();

    @Query("SELECT * FROM Meal WHERE id = :mealId LIMIT 1")
    Meal getMeal(int mealId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMeal(Meal meal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMeals(List<Meal> meals);

    @Delete
    void deleteMeal(Meal meal);

    @Query("SELECT COUNT(*) FROM UserMeal WHERE mealId = :mealId")
    int countUserMealsForMeal(int mealId);

    @Query("SELECT * FROM UserMeal WHERE uid = :uid")
    List<UserMeal> getUserMeals(String uid);

    @Query(
            "SELECT * FROM UserMeal WHERE uid = :uid " +
                    "AND dateMillis >= :startOfDayMillis AND dateMillis < :endOfDayMillis " +
                    "ORDER BY dateMillis"
    )
    List<UserMeal> getUserMealsForDay(String uid, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.calories), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.uid = :uid " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedCalories(String uid, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.carbs), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.uid = :uid " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedCarbs(String uid, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.protein), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.uid = :uid " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedProteins(String uid, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.fat), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.uid = :uid " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedFats(String uid, long startOfDayMillis, long endOfDayMillis);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserMeal(UserMeal userMeal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserMeals(List<UserMeal> userMeals);

    @Delete
    void deleteUserMeal(UserMeal userMeal);

    @Query("DELETE FROM UserMeal WHERE uid = :uid")
    void deleteUserMeals(String uid);

    @Query("SELECT * FROM Workout WHERE uid = :uid ORDER BY dateMillis DESC")
    List<Workout> getWorkouts(String uid);

    @Query(
            "SELECT * FROM Workout " +
                    "WHERE uid = :uid " +
                    "AND dateMillis >= :startMillis " +
                    "AND dateMillis < :endMillis " +
                    "ORDER BY dateMillis"
    )
    List<Workout> getWorkoutsBetween(String uid, long startMillis, long endMillis);

    @Query("SELECT * FROM Workout WHERE id = :workoutId AND uid = :uid LIMIT 1")
    Workout getWorkout(int workoutId, String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkout(Workout workout);

    @Delete
    void deleteWorkout(Workout workout);

    @Query("DELETE FROM Workout WHERE uid = :uid")
    void deleteWorkouts(String uid);

    @Query("SELECT * FROM Exercise ORDER BY name")
    List<Exercise> getExercises();

    @Query("SELECT * FROM Exercise WHERE id = :exerciseId LIMIT 1")
    Exercise getExercise(String exerciseId);

    @Query("SELECT * FROM Exercise WHERE name LIKE '%' || :query || '%' ORDER BY name")
    List<Exercise> searchExercises(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExercise(Exercise exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExercises(List<Exercise> exercises);

    @Query("SELECT * FROM WorkoutExercise WHERE workoutId = :workoutId AND uid = :uid")
    List<WorkoutExercise> getWorkoutExercises(int workoutId, String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercise(WorkoutExercise workoutExercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercises(List<WorkoutExercise> workoutExercises);

    @Query("DELETE FROM WorkoutExercise WHERE workoutId = :workoutId AND uid = :uid")
    void deleteWorkoutExercises(int workoutId, String uid);

    @Query("DELETE FROM WorkoutExercise WHERE uid = :uid")
    void deleteWorkoutExercises(String uid);

    @Query(
            "SELECT * FROM ExerciseSet " +
                    "WHERE workoutId = :workoutId " +
                    "AND uid = :uid " +
                    "AND exerciseId = :exerciseId"
    )
    List<ExerciseSet> getExerciseSets(int workoutId, String uid, String exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExerciseSet(ExerciseSet exerciseSet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExerciseSets(List<ExerciseSet> exerciseSets);

    @Query("DELETE FROM ExerciseSet WHERE workoutId = :workoutId AND uid = :uid")
    void deleteExerciseSets(int workoutId, String uid);

    @Query("DELETE FROM ExerciseSet WHERE uid = :uid")
    void deleteExerciseSets(String uid);

    @Query("SELECT * FROM WeightEntry WHERE uid = :uid ORDER BY dateMillis")
    List<WeightEntry> getWeightEntries(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeightEntry(WeightEntry weightEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeightEntries(List<WeightEntry> weightEntries);

    @Query("DELETE FROM WeightEntry WHERE uid = :uid")
    void deleteWeightEntries(String uid);
}
