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

    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    User getUser(String username);

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

    @Query("SELECT * FROM UserMeal WHERE username = :username")
    List<UserMeal> getUserMeals(String username);

    @Query(
            "SELECT COALESCE(SUM(Meal.calories), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.username = :username " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedCalories(String username, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.carbs), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.username = :username " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedCarbs(String username, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.protein), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.username = :username " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedProteins(String username, long startOfDayMillis, long endOfDayMillis);

    @Query(
            "SELECT COALESCE(SUM(Meal.fat), 0) " +
                    "FROM UserMeal " +
                    "JOIN Meal ON Meal.id = UserMeal.mealId " +
                    "WHERE UserMeal.username = :username " +
                    "AND UserMeal.dateMillis >= :startOfDayMillis " +
                    "AND UserMeal.dateMillis < :endOfDayMillis"
    )
    float getConsumedFats(String username, long startOfDayMillis, long endOfDayMillis);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserMeal(UserMeal userMeal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserMeals(List<UserMeal> userMeals);

    @Delete
    void deleteUserMeal(UserMeal userMeal);

    @Query("DELETE FROM UserMeal WHERE username = :username")
    void deleteUserMeals(String username);

    @Query("SELECT * FROM Workout WHERE username = :username ORDER BY dateMillis DESC")
    List<Workout> getWorkouts(String username);

    @Query(
            "SELECT * FROM Workout " +
                    "WHERE username = :username " +
                    "AND dateMillis >= :startMillis " +
                    "AND dateMillis < :endMillis " +
                    "ORDER BY dateMillis"
    )
    List<Workout> getWorkoutsBetween(String username, long startMillis, long endMillis);

    @Query("SELECT * FROM Workout WHERE id = :workoutId AND username = :username LIMIT 1")
    Workout getWorkout(int workoutId, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkout(Workout workout);

    @Delete
    void deleteWorkout(Workout workout);

    @Query("DELETE FROM Workout WHERE username = :username")
    void deleteWorkouts(String username);

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

    @Query("SELECT * FROM WorkoutExercise WHERE workoutId = :workoutId AND username = :username")
    List<WorkoutExercise> getWorkoutExercises(int workoutId, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercise(WorkoutExercise workoutExercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercises(List<WorkoutExercise> workoutExercises);

    @Query("DELETE FROM WorkoutExercise WHERE username = :username")
    void deleteWorkoutExercises(String username);

    @Query(
            "SELECT * FROM ExerciseSet " +
                    "WHERE workoutId = :workoutId " +
                    "AND username = :username " +
                    "AND exerciseId = :exerciseId"
    )
    List<ExerciseSet> getExerciseSets(int workoutId, String username, String exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExerciseSet(ExerciseSet exerciseSet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExerciseSets(List<ExerciseSet> exerciseSets);

    @Query("DELETE FROM ExerciseSet WHERE username = :username")
    void deleteExerciseSets(String username);

    @Query("SELECT * FROM WeightEntry WHERE username = :username ORDER BY dateMillis")
    List<WeightEntry> getWeightEntries(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeightEntry(WeightEntry weightEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeightEntries(List<WeightEntry> weightEntries);

    @Query("DELETE FROM WeightEntry WHERE username = :username")
    void deleteWeightEntries(String username);
}
