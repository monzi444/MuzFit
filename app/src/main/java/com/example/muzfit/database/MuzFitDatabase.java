package com.example.muzfit.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;

@Database(
        entities = {
                User.class,
                Meal.class,
                Workout.class,
                Exercise.class,
                WorkoutExercise.class,
                ExerciseSet.class,
                UserMeal.class,
                WeightEntry.class
        },
        version = 6,
        exportSchema = false
)
@TypeConverters(StringListConverter.class)
public abstract class MuzFitDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "muzfit.db";
    private static volatile MuzFitDatabase instance;

    public abstract MuzFitDao muzFitDao();

    public static MuzFitDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (MuzFitDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    MuzFitDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
