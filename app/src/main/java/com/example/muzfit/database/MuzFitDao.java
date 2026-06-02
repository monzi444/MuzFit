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

    @Query("SELECT * FROM Utente")
    List<User> getUsers();

    @Query("SELECT * FROM Utente WHERE Username = :username LIMIT 1")
    User getUser(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM Pasto ORDER BY Alimento")
    List<Meal> getMeals();

    @Query("SELECT * FROM Pasto WHERE idPasto = :mealId LIMIT 1")
    Meal getMeal(int mealId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMeal(Meal meal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMeals(List<Meal> meals);

    @Query("SELECT * FROM Pasto_has_Utente WHERE Utente_Username = :username")
    List<UserMeal> getUserMeals(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserMeal(UserMeal userMeal);

    @Delete
    void deleteUserMeal(UserMeal userMeal);

    @Query("SELECT * FROM Allenamento WHERE Utente_Username = :username ORDER BY Data DESC")
    List<Workout> getWorkouts(String username);

    @Query("SELECT * FROM Allenamento WHERE idAllenamento = :workoutId AND Utente_Username = :username LIMIT 1")
    Workout getWorkout(int workoutId, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkout(Workout workout);

    @Delete
    void deleteWorkout(Workout workout);

    @Query("SELECT * FROM DescrizioneEsercizio ORDER BY NomeEsercizio")
    List<Exercise> getExercises();

    @Query("SELECT * FROM DescrizioneEsercizio WHERE IdEsercizio = :exerciseId LIMIT 1")
    Exercise getExercise(String exerciseId);

    @Query("SELECT * FROM DescrizioneEsercizio WHERE NomeEsercizio LIKE '%' || :query || '%' ORDER BY NomeEsercizio")
    List<Exercise> searchExercises(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExercise(Exercise exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExercises(List<Exercise> exercises);

    @Query("SELECT * FROM AllenamentoEsercizio WHERE Allenamento_idAllenamento = :workoutId AND Allenamento_Utente_Username = :username")
    List<WorkoutExercise> getWorkoutExercises(int workoutId, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutExercise(WorkoutExercise workoutExercise);

    @Query(
            "SELECT * FROM Serie " +
                    "WHERE AllenamentoEsercizio_Allenamento_idAllenamento = :workoutId " +
                    "AND AllenamentoEsercizio_Allenamento_Utente_Username = :username " +
                    "AND AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio = :exerciseId"
    )
    List<ExerciseSet> getExerciseSets(int workoutId, String username, String exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExerciseSet(ExerciseSet exerciseSet);

    @Query("SELECT * FROM Peso WHERE Utente_Username = :username ORDER BY Data")
    List<WeightEntry> getWeightEntries(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeightEntry(WeightEntry weightEntry);
}
