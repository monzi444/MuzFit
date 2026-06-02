package com.example.muzfit.database;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Food;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.utils.Constants;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MuzFitDatabaseSeeder {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private MuzFitDatabaseSeeder() {
    }

    public static void seedBrunoMoretti(MuzFitDatabase database) {
        EXECUTOR.execute(() -> {
            MuzFitDao dao = database.muzFitDao();
            if (dao.getUser(Constants.DEFAULT_USERNAME) != null) {
                return;
            }

            seedUser(dao);
            seedMeals(dao);
            seedExercises(dao);
            seedWorkouts(dao);
            seedWorkoutExercises(dao);
            seedExerciseSets(dao);
            seedUserMeals(dao);
            seedWeightEntries(dao);
        });
    }

    private static void seedUser(MuzFitDao dao) {
        dao.insertUser(new User(
                Constants.DEFAULT_USERNAME,
                "Bruno Moretti",
                "bruno2026",
                81.4f,
                179.0f,
                1,
                520,
                2450,
                285.0f,
                155.0f,
                75.0f
        ));
    }

    private static void seedMeals(MuzFitDao dao) {
        dao.insertMeals(Arrays.asList(
                new Meal(1, "Yogurt greco con avena", 390.0f, 50.0f, 29.0f, 9.0f, Food.Category.COLAZIONE),
                new Meal(2, "Riso basmati pollo e zucchine", 710.0f, 86.0f, 48.0f, 18.0f, Food.Category.PRANZO),
                new Meal(3, "Pasta integrale al tonno", 680.0f, 82.0f, 42.0f, 16.0f, Food.Category.PRANZO),
                new Meal(4, "Salmone patate e insalata", 640.0f, 46.0f, 43.0f, 28.0f, Food.Category.CENA),
                new Meal(5, "Frullato banana e proteine", 330.0f, 38.0f, 31.0f, 6.0f, Food.Category.COLAZIONE),
                new Meal(6, "Tacchino verdure e pane", 560.0f, 52.0f, 45.0f, 15.0f, Food.Category.CENA)
        ));
    }

    private static void seedExercises(MuzFitDao dao) {
        dao.insertExercises(Arrays.asList(
                new Exercise("1", "Panca piana",
                        Arrays.asList("chest"),
                        Arrays.asList("barbell"),
                        "",
                        Arrays.asList("pectorals"),
                        Arrays.asList("triceps", "front delts"),
                        Arrays.asList("Sdraiati sulla panca.", "Abbassa il bilanciere al petto.", "Spingi fino a distendere le braccia.")),
                new Exercise("2", "Squat",
                        Arrays.asList("upper legs"),
                        Arrays.asList("barbell"),
                        "",
                        Arrays.asList("quadriceps"),
                        Arrays.asList("glutes", "hamstrings"),
                        Arrays.asList("Posiziona il bilanciere sulle spalle.", "Scendi controllando il busto.", "Risalire spingendo con i piedi.")),
                new Exercise("3", "Stacco da terra",
                        Arrays.asList("back", "upper legs"),
                        Arrays.asList("barbell"),
                        "",
                        Arrays.asList("glutes"),
                        Arrays.asList("hamstrings", "lower back"),
                        Arrays.asList("Afferra il bilanciere a terra.", "Mantieni la schiena neutra.", "Estendi anche e ginocchia insieme.")),
                new Exercise("4", "Trazioni",
                        Arrays.asList("back"),
                        Arrays.asList("body weight"),
                        "",
                        Arrays.asList("lats"),
                        Arrays.asList("biceps", "rear delts"),
                        Arrays.asList("Appenditi alla sbarra.", "Tira il petto verso l alto.", "Scendi lentamente.")),
                new Exercise("5", "Shoulder press",
                        Arrays.asList("shoulders"),
                        Arrays.asList("dumbbell"),
                        "",
                        Arrays.asList("delts"),
                        Arrays.asList("triceps"),
                        Arrays.asList("Porta i manubri alle spalle.", "Spingi sopra la testa.", "Torna alla posizione iniziale.")),
                new Exercise("6", "Plank",
                        Arrays.asList("waist"),
                        Arrays.asList("body weight"),
                        "",
                        Arrays.asList("abs"),
                        Arrays.asList("glutes"),
                        Arrays.asList("Appoggia gli avambracci.", "Mantieni il corpo in linea.", "Contrai addome e glutei."))
        ));
    }

    private static void seedWorkouts(MuzFitDao dao) {
        dao.insertWorkout(new Workout(1, daysAgo(6, 18, 30), "Forza parte alta", Constants.DEFAULT_USERNAME));
        dao.insertWorkout(new Workout(2, daysAgo(4, 19, 0), "Gambe pesanti", Constants.DEFAULT_USERNAME));
        dao.insertWorkout(new Workout(3, daysAgo(2, 7, 15), "Full body rapido", Constants.DEFAULT_USERNAME));
        dao.insertWorkout(new Workout(4, daysAgo(0, 18, 0), "Spinta e core", Constants.DEFAULT_USERNAME));
    }

    private static void seedWorkoutExercises(MuzFitDao dao) {
        dao.insertWorkoutExercise(new WorkoutExercise(145, 1, Constants.DEFAULT_USERNAME, "1"));
        dao.insertWorkoutExercise(new WorkoutExercise(110, 1, Constants.DEFAULT_USERNAME, "4"));
        dao.insertWorkoutExercise(new WorkoutExercise(210, 2, Constants.DEFAULT_USERNAME, "2"));
        dao.insertWorkoutExercise(new WorkoutExercise(180, 2, Constants.DEFAULT_USERNAME, "3"));
        dao.insertWorkoutExercise(new WorkoutExercise(125, 3, Constants.DEFAULT_USERNAME, "4"));
        dao.insertWorkoutExercise(new WorkoutExercise(90, 3, Constants.DEFAULT_USERNAME, "6"));
        dao.insertWorkoutExercise(new WorkoutExercise(135, 4, Constants.DEFAULT_USERNAME, "5"));
        dao.insertWorkoutExercise(new WorkoutExercise(80, 4, Constants.DEFAULT_USERNAME, "6"));
    }

    private static void seedExerciseSets(MuzFitDao dao) {
        dao.insertExerciseSet(new ExerciseSet(1, 8, 72, 1, Constants.DEFAULT_USERNAME, "1"));
        dao.insertExerciseSet(new ExerciseSet(2, 8, 75, 1, Constants.DEFAULT_USERNAME, "1"));
        dao.insertExerciseSet(new ExerciseSet(1, 8, 0, 1, Constants.DEFAULT_USERNAME, "4"));
        dao.insertExerciseSet(new ExerciseSet(2, 7, 0, 1, Constants.DEFAULT_USERNAME, "4"));
        dao.insertExerciseSet(new ExerciseSet(1, 6, 110, 2, Constants.DEFAULT_USERNAME, "2"));
        dao.insertExerciseSet(new ExerciseSet(2, 6, 115, 2, Constants.DEFAULT_USERNAME, "2"));
        dao.insertExerciseSet(new ExerciseSet(1, 5, 135, 2, Constants.DEFAULT_USERNAME, "3"));
        dao.insertExerciseSet(new ExerciseSet(2, 5, 140, 2, Constants.DEFAULT_USERNAME, "3"));
        dao.insertExerciseSet(new ExerciseSet(1, 9, 0, 3, Constants.DEFAULT_USERNAME, "4"));
        dao.insertExerciseSet(new ExerciseSet(1, 50, 0, 3, Constants.DEFAULT_USERNAME, "6"));
        dao.insertExerciseSet(new ExerciseSet(1, 10, 18, 4, Constants.DEFAULT_USERNAME, "5"));
        dao.insertExerciseSet(new ExerciseSet(2, 10, 20, 4, Constants.DEFAULT_USERNAME, "5"));
        dao.insertExerciseSet(new ExerciseSet(1, 60, 0, 4, Constants.DEFAULT_USERNAME, "6"));
    }

    private static void seedUserMeals(MuzFitDao dao) {
        dao.insertUserMeal(new UserMeal(1, Constants.DEFAULT_USERNAME, daysAgo(0, 8, 0)));
        dao.insertUserMeal(new UserMeal(2, Constants.DEFAULT_USERNAME, daysAgo(0, 13, 0)));
        dao.insertUserMeal(new UserMeal(4, Constants.DEFAULT_USERNAME, daysAgo(0, 20, 15)));
        dao.insertUserMeal(new UserMeal(5, Constants.DEFAULT_USERNAME, daysAgo(1, 16, 30)));
        dao.insertUserMeal(new UserMeal(3, Constants.DEFAULT_USERNAME, daysAgo(2, 13, 15)));
        dao.insertUserMeal(new UserMeal(6, Constants.DEFAULT_USERNAME, daysAgo(2, 20, 0)));
    }

    private static void seedWeightEntries(MuzFitDao dao) {
        dao.insertWeightEntry(new WeightEntry(daysAgo(28, 7, 30), 83.0f, Constants.DEFAULT_USERNAME));
        dao.insertWeightEntry(new WeightEntry(daysAgo(21, 7, 30), 82.4f, Constants.DEFAULT_USERNAME));
        dao.insertWeightEntry(new WeightEntry(daysAgo(14, 7, 30), 81.9f, Constants.DEFAULT_USERNAME));
        dao.insertWeightEntry(new WeightEntry(daysAgo(7, 7, 30), 81.6f, Constants.DEFAULT_USERNAME));
        dao.insertWeightEntry(new WeightEntry(daysAgo(0, 7, 30), 81.4f, Constants.DEFAULT_USERNAME));
    }

    private static long daysAgo(int daysAgo, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
