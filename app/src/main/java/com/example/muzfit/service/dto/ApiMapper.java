package com.example.muzfit.service.dto;

import com.example.muzfit.model.Exercise;
import com.example.muzfit.model.ExerciseSet;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.model.WorkoutExercise;
import com.example.muzfit.utils.DateParser;

import java.util.ArrayList;
import java.util.List;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static User toUser(UtenteDto dto) {
        if (dto == null) {
            return new User();
        }
        String uid = dto.getUsername() != null ? dto.getUsername() : "";
        String name = dto.getName() != null ? dto.getName() : "";
        return new User(
                uid,
                name,
                "",
                dto.getWeight(),
                dto.getHeight(),
                dto.getGenderCode(),
                dto.getCalorieBurnGoal(),
                dto.getCalorieGoal(),
                dto.getCarbGoal(),
                dto.getProteinGoal(),
                dto.getFatGoal()
        );
    }

    public static List<User> toUsers(List<UtenteDto> dtos) {
        List<User> users = new ArrayList<>();
        if (dtos == null) {
            return users;
        }
        for (UtenteDto dto : dtos) {
            users.add(toUser(dto));
        }
        return users;
    }

    public static Meal toMeal(PastoDto dto) {
        if (dto == null) {
            return new Meal();
        }
        String foodName = dto.getFoodName() != null ? dto.getFoodName() : "";
        return new Meal(dto.getId(), foodName, dto.getCalories(), dto.getCarbs(), dto.getProtein(), dto.getFat());
    }

    public static List<Meal> toMeals(List<PastoDto> dtos) {
        List<Meal> meals = new ArrayList<>();
        if (dtos == null) {
            return meals;
        }
        for (PastoDto dto : dtos) {
            meals.add(toMeal(dto));
        }
        return meals;
    }

    public static UserMeal toUserMeal(PastoUtenteDto dto) {
        if (dto == null) {
            return new UserMeal();
        }
        String uid = dto.getUsername() != null ? dto.getUsername() : "";
        return new UserMeal(dto.getMealId(), uid, DateParser.parseApiDate(dto.getDate()));
    }

    public static List<UserMeal> toUserMeals(List<PastoUtenteDto> dtos) {
        List<UserMeal> userMeals = new ArrayList<>();
        if (dtos == null) {
            return userMeals;
        }
        for (PastoUtenteDto dto : dtos) {
            userMeals.add(toUserMeal(dto));
        }
        return userMeals;
    }

    public static Workout toWorkout(AllenamentoDto dto) {
        if (dto == null) {
            return new Workout();
        }
        String description = dto.getDescription() != null ? dto.getDescription() : "";
        String uid = dto.getUsername() != null ? dto.getUsername() : "";
        return new Workout(
                dto.getId(),
                DateParser.parseApiDate(dto.getDate()),
                description,
                uid
        );
    }

    public static List<Workout> toWorkouts(List<AllenamentoDto> dtos) {
        List<Workout> workouts = new ArrayList<>();
        if (dtos == null) {
            return workouts;
        }
        for (AllenamentoDto dto : dtos) {
            workouts.add(toWorkout(dto));
        }
        return workouts;
    }

    public static Exercise toExercise(DescrizioneEsercizioDto dto) {
        if (dto == null) {
            return new Exercise();
        }
        String description = dto.getDescription() != null ? dto.getDescription() : "";
        String name = dto.getName() != null ? dto.getName() : "";
        return new Exercise(dto.getId(), description, name);
    }

    public static List<Exercise> toExercises(List<DescrizioneEsercizioDto> dtos) {
        List<Exercise> exercises = new ArrayList<>();
        if (dtos == null) {
            return exercises;
        }
        for (DescrizioneEsercizioDto dto : dtos) {
            exercises.add(toExercise(dto));
        }
        return exercises;
    }

    public static WorkoutExercise toWorkoutExercise(AllenamentoEsercizioDto dto) {
        if (dto == null) {
            return new WorkoutExercise();
        }
        String uid = dto.getUsername() != null ? dto.getUsername() : "";
        return new WorkoutExercise(
                dto.getCalories(),
                dto.getWorkoutId(),
                uid,
                dto.getExerciseId()
        );
    }

    public static List<WorkoutExercise> toWorkoutExercises(List<AllenamentoEsercizioDto> dtos) {
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        if (dtos == null) {
            return workoutExercises;
        }
        for (AllenamentoEsercizioDto dto : dtos) {
            workoutExercises.add(toWorkoutExercise(dto));
        }
        return workoutExercises;
    }

    public static ExerciseSet toExerciseSet(SerieDto dto) {
        if (dto == null) {
            return new ExerciseSet();
        }
        String uid = dto.getUsername() != null ? dto.getUsername() : "";
        return new ExerciseSet(
                dto.getId(),
                dto.getRepetitions(),
                dto.getWeight(),
                dto.getWorkoutId(),
                uid,
                dto.getExerciseId()
        );
    }

    public static List<ExerciseSet> toExerciseSets(List<SerieDto> dtos) {
        List<ExerciseSet> exerciseSets = new ArrayList<>();
        if (dtos == null) {
            return exerciseSets;
        }
        for (SerieDto dto : dtos) {
            exerciseSets.add(toExerciseSet(dto));
        }
        return exerciseSets;
    }

    public static WeightEntry toWeightEntry(PesoDto dto) {
        if (dto == null) {
            return new WeightEntry();
        }
        String uid = dto.getUsername() != null ? dto.getUsername() : "";
        return new WeightEntry(DateParser.parseApiDate(dto.getDate()), dto.getWeight(), uid);
    }

    public static List<WeightEntry> toWeightEntries(List<PesoDto> dtos) {
        List<WeightEntry> entries = new ArrayList<>();
        if (dtos == null) {
            return entries;
        }
        for (PesoDto dto : dtos) {
            entries.add(toWeightEntry(dto));
        }
        return entries;
    }
}
