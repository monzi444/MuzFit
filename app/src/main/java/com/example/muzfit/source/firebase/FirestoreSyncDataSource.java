package com.example.muzfit.source.firebase;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.WorkoutRoutine;
import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.utils.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncDataSource {

    private static final String USERS = "users";
    private static final String ROUTINES = "workoutRoutines";
    private static final String WEIGHT_ENTRIES = "weightEntries";
    private static final String USER_MEALS = "userMeals";
    private static final String WORKOUTS = "workouts";

    private final FirebaseFirestore db;

    public FirestoreSyncDataSource(FirebaseFirestore db) {
        this.db = db;
    }

    public boolean canSync(String uid) {
        return uid != null && !uid.isEmpty() && !Constants.DEFAULT_USERNAME.equals(uid);
    }

    public void saveUser(User user) {
        if (user == null || !canSync(user.getUid())) {
            return;
        }
        db.collection(USERS)
                .document(user.getUid())
                .set(toUserMap(user), SetOptions.merge());
    }

    public void fetchUser(String uid, DataSourceCallback<User> callback) {
        if (!canSync(uid)) {
            callback.onError(Constants.ERROR_FIREBASE_NOT_IMPLEMENTED);
            return;
        }
        db.collection(USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError(Constants.ERROR_USER_NOT_FOUND);
                        return;
                    }
                    callback.onSuccess(toUser(uid, snapshot.getData()));
                })
                .addOnFailureListener(e -> callback.onError(errorMessage(e)));
    }

    public void fetchRoutines(String uid, DataSourceCallback<List<WorkoutRoutine>> callback) {
        if (!canSync(uid)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        userCollection(uid, ROUTINES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WorkoutRoutine> routines = new ArrayList<>();
                    snapshot.forEach(document -> routines.add(toRoutine(document.getData())));
                    callback.onSuccess(routines);
                })
                .addOnFailureListener(e -> callback.onError(errorMessage(e)));
    }

    public void saveWeightEntry(WeightEntry entry) {
        if (entry == null || !canSync(entry.getUid())) {
            return;
        }
        userCollection(entry.getUid(), WEIGHT_ENTRIES)
                .document(String.valueOf(entry.getDateMillis()))
                .set(toWeightEntryMap(entry), SetOptions.merge());
    }

    public void deleteWeightEntry(WeightEntry entry) {
        if (entry == null || !canSync(entry.getUid())) {
            return;
        }
        userCollection(entry.getUid(), WEIGHT_ENTRIES)
                .document(String.valueOf(entry.getDateMillis()))
                .delete();
    }

    public void saveLoggedMeal(UserMeal userMeal, Meal meal) {
        if (userMeal == null || meal == null || !canSync(userMeal.getUid())) {
            return;
        }
        userCollection(userMeal.getUid(), USER_MEALS)
                .document(userMealDocumentId(userMeal))
                .set(toLoggedMealMap(userMeal, meal), SetOptions.merge());
    }

    public void fetchLoggedMeals(String uid,
                                 long startOfDayMillis,
                                 long endOfDayMillis,
                                 DataSourceCallback<List<LoggedMeal>> callback) {
        if (!canSync(uid)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        userCollection(uid, USER_MEALS)
                .whereGreaterThanOrEqualTo("dateMillis", startOfDayMillis)
                .whereLessThan("dateMillis", endOfDayMillis)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<LoggedMeal> loggedMeals = new ArrayList<>();
                    snapshot.forEach(document -> loggedMeals.add(toLoggedMeal(uid, document.getData())));
                    callback.onSuccess(loggedMeals);
                })
                .addOnFailureListener(e -> callback.onError(errorMessage(e)));
    }

    public void deleteLoggedMeal(UserMeal userMeal) {
        if (userMeal == null || !canSync(userMeal.getUid())) {
            return;
        }
        userCollection(userMeal.getUid(), USER_MEALS)
                .document(userMealDocumentId(userMeal))
                .delete();
    }

    public void saveWorkoutHistory(String uid, com.example.muzfit.model.Workout workout, List<com.example.muzfit.model.Exercise> exercises, Map<String, List<com.example.muzfit.model.ExerciseSet>> results) {
        if (!canSync(uid) || workout == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", workout.getId());
        data.put("dateMillis", workout.getDateMillis());
        data.put("description", workout.getDescription());

        List<Map<String, Object>> exercisesData = new ArrayList<>();
        for (com.example.muzfit.model.Exercise e : exercises) {
            Map<String, Object> eMap = new HashMap<>();
            eDataMap(e, eMap);
            
            List<com.example.muzfit.model.ExerciseSet> sets = results.get(e.getId());
            if (sets != null) {
                List<Map<String, Object>> setsData = new ArrayList<>();
                for (com.example.muzfit.model.ExerciseSet s : sets) {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("id", s.getId());
                    sMap.put("reps", s.getRepetitions());
                    sMap.put("weight", s.getWeight());
                    setsData.add(sMap);
                }
                eMap.put("sets", setsData);
            }
            exercisesData.add(eMap);
        }
        data.put("exercises", exercisesData);

        userCollection(uid, WORKOUTS)
                .document(String.valueOf(workout.getId()))
                .set(data, SetOptions.merge());
    }

    private void eDataMap(com.example.muzfit.model.Exercise exercise, Map<String, Object> exerciseData) {
        exerciseData.put("id", exercise.getId());
        exerciseData.put("name", exercise.getName());
        exerciseData.put("bodyParts", exercise.getBodyParts());
        exerciseData.put("equipments", exercise.getEquipments());
        exerciseData.put("gifUrl", exercise.getGifUrl());
        exerciseData.put("targetMuscles", exercise.getTargetMuscles());
        exerciseData.put("instructions", exercise.getInstructions());
    }

    public void saveRoutine(String uid, WorkoutRoutine routine) {
        if (!canSync(uid) || routine == null || routine.getName() == null || routine.getName().trim().isEmpty()) {
            return;
        }
        userCollection(uid, ROUTINES)
                .document(documentId(routine.getName()))
                .set(toRoutineMap(routine), SetOptions.merge());
    }

    public void deleteRoutine(String uid, String routineName) {
        if (!canSync(uid) || routineName == null || routineName.trim().isEmpty()) {
            return;
        }
        userCollection(uid, ROUTINES)
                .document(documentId(routineName))
                .delete();
    }

    public void fetchWorkouts(String uid, DataSourceCallback<List<WorkoutWithDetails>> callback) {
        if (!canSync(uid)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        userCollection(uid, WORKOUTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WorkoutWithDetails> items = new ArrayList<>();
                    snapshot.forEach(doc -> items.add(toWorkoutWithDetails(uid, doc.getData())));
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> callback.onError(errorMessage(e)));
    }

    private WorkoutWithDetails toWorkoutWithDetails(String uid, Map<String, Object> data) {
        com.example.muzfit.model.Workout workout = new com.example.muzfit.model.Workout(
                intValue(data.get("id")),
                longValue(data.get("dateMillis")),
                stringValue(data.get("description")),
                uid
        );

        List<ExerciseWithSets> exercises = new ArrayList<>();
        Object rawExercises = data.get("exercises");
        if (rawExercises instanceof List<?>) {
            for (Object obj : (List<?>) rawExercises) {
                if (obj instanceof Map<?, ?>) {
                    Map<?, ?> eMap = (Map<?, ?>) obj;
                    com.example.muzfit.model.Exercise e = toExercise(eMap);
                    
                    List<com.example.muzfit.model.ExerciseSet> sets = new ArrayList<>();
                    Object rawSets = eMap.get("sets");
                    if (rawSets instanceof List<?>) {
                        for (Object sObj : (List<?>) rawSets) {
                            if (sObj instanceof Map<?, ?>) {
                                Map<?, ?> sMap = (Map<?, ?>) sObj;
                                sets.add(new com.example.muzfit.model.ExerciseSet(
                                        intValue(sMap.get("id")),
                                        intValue(sMap.get("reps")),
                                        doubleValue(sMap.get("weight")),
                                        workout.getId(),
                                        uid,
                                        e.getId()
                                ));
                            }
                        }
                    }
                    exercises.add(new ExerciseWithSets(e, sets));
                }
            }
        }
        return new WorkoutWithDetails(workout, exercises);
    }

    private CollectionReference userCollection(String uid, String collectionName) {
        return db.collection(USERS).document(uid).collection(collectionName);
    }

    private static Map<String, Object> toUserMap(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", user.getName());
        data.put("profileImageUri", user.getProfileImageUri());
        data.put("weight", user.getWeight());
        data.put("height", user.getHeight());
        data.put("genderCode", user.getGenderCode());
        data.put("calorieBurnGoal", user.getCalorieBurnGoal());
        data.put("calorieGoal", user.getCalorieGoal());
        data.put("carbGoal", user.getCarbGoal());
        data.put("proteinGoal", user.getProteinGoal());
        data.put("fatGoal", user.getFatGoal());
        return data;
    }

    private static User toUser(String uid, Map<String, Object> data) {
        User user = new User();
        user.setUid(uid);
        if (data == null) {
            return user;
        }
        user.setName(stringValue(data.get("name")));
        user.setProfileImageUri(stringValue(data.get("profileImageUri")));
        user.setWeight(floatValue(data.get("weight")));
        user.setHeight(floatValue(data.get("height")));
        user.setGenderCode(intValue(data.get("genderCode")));
        user.setCalorieBurnGoal(intValue(data.get("calorieBurnGoal")));
        user.setCalorieGoal(intValue(data.get("calorieGoal")));
        user.setCarbGoal(floatValue(data.get("carbGoal")));
        user.setProteinGoal(floatValue(data.get("proteinGoal")));
        user.setFatGoal(floatValue(data.get("fatGoal")));
        return user;
    }

    private static Map<String, Object> toWeightEntryMap(WeightEntry entry) {
        Map<String, Object> data = new HashMap<>();
        data.put("dateMillis", entry.getDateMillis());
        data.put("weight", entry.getWeight());
        return data;
    }

    private static Map<String, Object> toLoggedMealMap(UserMeal userMeal, Meal meal) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", userMeal.getId());
        data.put("mealId", userMeal.getMealId());
        data.put("dateMillis", userMeal.getDateMillis());
        data.put("category", userMeal.getCategory().name());
        data.put("foodName", meal.getFoodName());
        data.put("calories", meal.getCalories());
        data.put("carbs", meal.getCarbs());
        data.put("protein", meal.getProtein());
        data.put("fat", meal.getFat());
        return data;
    }

    private static LoggedMeal toLoggedMeal(String uid, Map<String, Object> data) {
        if (data == null) {
            return new LoggedMeal(new UserMeal(0, uid, 0L), new Meal());
        }

        Meal meal = new Meal(
                intValue(data.get("mealId")),
                stringValue(data.get("foodName")),
                floatValue(data.get("calories")),
                floatValue(data.get("carbs")),
                floatValue(data.get("protein")),
                floatValue(data.get("fat"))
        );
        UserMeal userMeal = new UserMeal(
                meal.getId(),
                uid,
                longValue(data.get("dateMillis")),
                mealCategoryValue(data.get("category"))
        );
        userMeal.setId(intValue(data.get("id")));
        return new LoggedMeal(userMeal, meal);
    }

    private static Map<String, Object> toRoutineMap(WorkoutRoutine routine) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", routine.getName());
        List<Map<String, Object>> exercises = new ArrayList<>();
        for (Exercise exercise : routine.getExercises()) {
            Map<String, Object> exerciseData = new HashMap<>();
            exerciseData.put("id", exercise.getId());
            exerciseData.put("name", exercise.getName());
            exerciseData.put("description", exercise.getDescription());
            exerciseData.put("bodyParts", exercise.getBodyParts());
            exerciseData.put("equipments", exercise.getEquipments());
            exerciseData.put("gifUrl", exercise.getGifUrl());
            exerciseData.put("targetMuscles", exercise.getTargetMuscles());
            exerciseData.put("secondaryMuscles", exercise.getSecondaryMuscles());
            exerciseData.put("instructions", exercise.getInstructions());
            exercises.add(exerciseData);
        }
        data.put("exercises", exercises);
        return data;
    }

    private static WorkoutRoutine toRoutine(Map<String, Object> data) {
        if (data == null) {
            return new WorkoutRoutine("");
        }
        String name = stringValue(data.get("name"));
        List<Exercise> exercises = new ArrayList<>();
        Object rawExercises = data.get("exercises");
        if (rawExercises instanceof List<?>) {
            for (Object rawExercise : (List<?>) rawExercises) {
                if (rawExercise instanceof Map<?, ?>) {
                    exercises.add(toExercise((Map<?, ?>) rawExercise));
                }
            }
        }
        return new WorkoutRoutine(name, exercises);
    }

    private static Exercise toExercise(Map<?, ?> data) {
        Exercise exercise = new Exercise();
        exercise.setId(stringValue(data.get("id")));
        exercise.setName(stringValue(data.get("name")));
        exercise.setDescription(stringValue(data.get("description")));
        exercise.setBodyParts(stringListValue(data.get("bodyParts")));
        exercise.setEquipments(stringListValue(data.get("equipments")));
        exercise.setGifUrl(stringValue(data.get("gifUrl")));
        exercise.setTargetMuscles(stringListValue(data.get("targetMuscles")));
        exercise.setSecondaryMuscles(stringListValue(data.get("secondaryMuscles")));
        exercise.setInstructions(stringListValue(data.get("instructions")));
        return exercise;
    }

    private static String userMealDocumentId(UserMeal userMeal) {
        return userMeal.getId() > 0 ? String.valueOf(userMeal.getId()) : userMeal.getMealId() + "_" + userMeal.getDateMillis();
    }

    private static String documentId(String value) {
        return value.trim().replaceAll("[/#?\\[\\]]", "_");
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static int intValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static long longValue(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private static float floatValue(Object value) {
        return value instanceof Number ? ((Number) value).floatValue() : 0f;
    }

    private static double doubleValue(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private static MealCategory mealCategoryValue(Object value) {
        if (value instanceof String) {
            try {
                return MealCategory.valueOf((String) value);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return MealCategory.PRANZO;
    }

    private static List<String> stringListValue(Object value) {
        List<String> strings = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item instanceof String) {
                    strings.add((String) item);
                }
            }
        }
        return strings;
    }

    private static String errorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : Constants.ERROR_FIREBASE_NOT_IMPLEMENTED;
    }

    public static class LoggedMeal {
        private final UserMeal userMeal;
        private final Meal meal;

        public LoggedMeal(UserMeal userMeal, Meal meal) {
            this.userMeal = userMeal;
            this.meal = meal;
        }

        public UserMeal getUserMeal() {
            return userMeal;
        }

        public Meal getMeal() {
            return meal;
        }
    }

    public static class WorkoutWithDetails {
        public final com.example.muzfit.model.Workout workout;
        public final List<ExerciseWithSets> exercises;
        public WorkoutWithDetails(com.example.muzfit.model.Workout workout, List<ExerciseWithSets> exercises) {
            this.workout = workout;
            this.exercises = exercises;
        }
    }

    public static class ExerciseWithSets {
        public final com.example.muzfit.model.Exercise exercise;
        public final List<com.example.muzfit.model.ExerciseSet> sets;
        public ExerciseWithSets(com.example.muzfit.model.Exercise exercise, List<com.example.muzfit.model.ExerciseSet> sets) {
            this.exercise = exercise;
            this.sets = sets;
        }
    }
}
