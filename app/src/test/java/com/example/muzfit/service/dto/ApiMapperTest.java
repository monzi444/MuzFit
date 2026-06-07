package com.example.muzfit.service.dto;

import com.example.muzfit.model.Meal;
import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.model.Workout;
import com.example.muzfit.utils.DateParser;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApiMapperTest {

    private static final Gson GSON = new Gson();

    @Test
    public void toUser_nullDto_returnsEmptyUser() {
        User user = ApiMapper.toUser(null);
        assertNotNull(user);
        assertEquals("", user.getUid());
    }

    @Test
    public void toUser_mapsAllFields() {
        UtenteDto dto = GSON.fromJson(
                "{"
                        + "\"Username\":\"mario.rossi\","
                        + "\"Nome\":\"Mario Rossi\","
                        + "\"Peso\":78.5,"
                        + "\"Altezza\":178.0,"
                        + "\"Genere\":1,"
                        + "\"CalorieBruciate\":450,"
                        + "\"CalorieAssunte\":2400,"
                        + "\"Carboidrati\":280.0,"
                        + "\"Proteine\":150.0,"
                        + "\"Grassi\":70.0"
                        + "}",
                UtenteDto.class
        );

        User user = ApiMapper.toUser(dto);

        assertEquals("mario.rossi", user.getUid());
        assertEquals("Mario Rossi", user.getName());
        assertEquals(78.5f, user.getWeight(), 0.01f);
        assertEquals(178.0f, user.getHeight(), 0.01f);
        assertEquals(1, user.getGenderCode());
        assertEquals(450, user.getCalorieBurnGoal());
        assertEquals(2400, user.getCalorieGoal());
        assertEquals(280.0f, user.getCarbGoal(), 0.01f);
        assertEquals(150.0f, user.getProteinGoal(), 0.01f);
        assertEquals(70.0f, user.getFatGoal(), 0.01f);
    }

    @Test
    public void toUsers_nullList_returnsEmptyList() {
        List<User> users = ApiMapper.toUsers(null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void toMeal_mapsFields() {
        PastoDto dto = GSON.fromJson(
                "{"
                        + "\"idPasto\":1,"
                        + "\"Alimento\":\"Yogurt greco e avena\","
                        + "\"Calorie\":380.0,"
                        + "\"Carboidrati\":52.0,"
                        + "\"Proteine\":28.0,"
                        + "\"Grassi\":12.0"
                        + "}",
                PastoDto.class
        );

        Meal meal = ApiMapper.toMeal(dto);

        assertEquals(1, meal.getId());
        assertEquals("Yogurt greco e avena", meal.getFoodName());
        assertEquals(380.0f, meal.getCalories(), 0.01f);
        assertEquals(52.0f, meal.getCarbs(), 0.01f);
        assertEquals(28.0f, meal.getProtein(), 0.01f);
        assertEquals(12.0f, meal.getFat(), 0.01f);
    }

    @Test
    public void toWorkout_mapsFieldsAndParsesDate() {
        AllenamentoDto dto = GSON.fromJson(
                "{"
                        + "\"idAllenamento\":1,"
                        + "\"Data\":\"2026-05-27 18:30:00\","
                        + "\"Descrizione\":\"Forza parte alta\","
                        + "\"Utente_Username\":\"mario.rossi\""
                        + "}",
                AllenamentoDto.class
        );

        Workout workout = ApiMapper.toWorkout(dto);

        assertEquals(1, workout.getId());
        assertEquals("Forza parte alta", workout.getDescription());
        assertEquals("mario.rossi", workout.getUid());
        assertEquals(DateParser.parseApiDate("2026-05-27 18:30:00"), workout.getDateMillis());
    }

    @Test
    public void toUserMeal_mapsFieldsAndParsesDate() {
        PastoUtenteDto dto = GSON.fromJson(
                "{"
                        + "\"Pasto_idPasto\":2,"
                        + "\"Utente_Username\":\"giulia.verdi\","
                        + "\"Data\":\"2026-05-27 12:00:00\""
                        + "}",
                PastoUtenteDto.class
        );

        UserMeal userMeal = ApiMapper.toUserMeal(dto);

        assertEquals(2, userMeal.getMealId());
        assertEquals("giulia.verdi", userMeal.getUid());
        assertEquals(DateParser.parseApiDate("2026-05-27 12:00:00"), userMeal.getDateMillis());
    }

    @Test
    public void toWeightEntry_mapsFieldsAndParsesDate() {
        PesoDto dto = GSON.fromJson(
                "{"
                        + "\"Data\":\"2026-05-27 08:00:00\","
                        + "\"Peso\":78.5,"
                        + "\"Utente_Username\":\"mario.rossi\""
                        + "}",
                PesoDto.class
        );

        WeightEntry entry = ApiMapper.toWeightEntry(dto);

        assertEquals("mario.rossi", entry.getUid());
        assertEquals(78.5f, entry.getWeight(), 0.01f);
        assertEquals(DateParser.parseApiDate("2026-05-27 08:00:00"), entry.getDateMillis());
    }

    @Test
    public void toWorkouts_mapsEachItem() {
        List<AllenamentoDto> dtos = Arrays.asList(
                GSON.fromJson("{\"idAllenamento\":1,\"Data\":\"2026-05-27 18:30:00\",\"Descrizione\":\"A\",\"Utente_Username\":\"u1\"}", AllenamentoDto.class),
                GSON.fromJson("{\"idAllenamento\":2,\"Data\":\"2026-05-28 10:00:00\",\"Descrizione\":\"B\",\"Utente_Username\":\"u2\"}", AllenamentoDto.class)
        );

        List<Workout> workouts = ApiMapper.toWorkouts(dtos);

        assertEquals(2, workouts.size());
        assertEquals(1, workouts.get(0).getId());
        assertEquals(2, workouts.get(1).getId());
    }

    @Test
    public void toMeals_nullList_returnsEmptyList() {
        assertTrue(ApiMapper.toMeals(null).isEmpty());
        assertTrue(ApiMapper.toMeals(Collections.emptyList()).isEmpty());
    }
}
