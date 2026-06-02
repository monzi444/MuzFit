package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "DescrizioneEsercizio")
public class Exercise implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "IdEsercizio")
    @SerializedName("exerciseId")
    private String id = "";

    @ColumnInfo(name = "NomeEsercizio")
    @SerializedName("name")
    private String name = "";

    @ColumnInfo(name = "BodyParts")
    @SerializedName("bodyParts")
    private List<String> bodyParts = new ArrayList<>();

    @ColumnInfo(name = "Equipments")
    @SerializedName("equipments")
    private List<String> equipments = new ArrayList<>();

    @ColumnInfo(name = "GifUrl")
    @SerializedName("gifUrl")
    private String gifUrl = "";

    @ColumnInfo(name = "TargetMuscles")
    @SerializedName("targetMuscles")
    private List<String> targetMuscles = new ArrayList<>();

    @ColumnInfo(name = "SecondaryMuscles")
    @SerializedName("secondaryMuscles")
    private List<String> secondaryMuscles = new ArrayList<>();

    @ColumnInfo(name = "Instructions")
    @SerializedName("instructions")
    private List<String> instructions = new ArrayList<>();

    public Exercise() {
    }

    @Ignore
    public Exercise(int id, String description, String name) {
        this(String.valueOf(id), name, new ArrayList<>(), new ArrayList<>(), "",
                new ArrayList<>(), new ArrayList<>(), descriptionToInstructions(description));
    }

    @Ignore
    public Exercise(String id, String name, List<String> bodyParts, List<String> equipments,
                    String gifUrl, List<String> targetMuscles, List<String> secondaryMuscles,
                    List<String> instructions) {
        this.id = id != null ? id : "";
        this.name = name;
        this.bodyParts = bodyParts != null ? bodyParts : new ArrayList<>();
        this.equipments = equipments != null ? equipments : new ArrayList<>();
        this.gifUrl = gifUrl != null ? gifUrl : "";
        this.targetMuscles = targetMuscles != null ? targetMuscles : new ArrayList<>();
        this.secondaryMuscles = secondaryMuscles != null ? secondaryMuscles : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    @Ignore
    public int getNumericId() {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setId(int id) {
        this.id = String.valueOf(id);
    }

    @Ignore
    public String getDescription() {
        if (instructions == null || instructions.isEmpty()) {
            return "";
        }
        return String.join("\n", instructions);
    }

    public void setDescription(String description) {
        this.instructions = descriptionToInstructions(description);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public List<String> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(List<String> bodyParts) {
        this.bodyParts = bodyParts != null ? bodyParts : new ArrayList<>();
    }

    public List<String> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<String> equipments) {
        this.equipments = equipments != null ? equipments : new ArrayList<>();
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl != null ? gifUrl : "";
    }

    public List<String> getTargetMuscles() {
        return targetMuscles;
    }

    public void setTargetMuscles(List<String> targetMuscles) {
        this.targetMuscles = targetMuscles != null ? targetMuscles : new ArrayList<>();
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles != null ? secondaryMuscles : new ArrayList<>();
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions != null ? instructions : new ArrayList<>();
    }

    @Ignore
    public String getBodyPart() {
        return firstOrFallback(bodyParts);
    }

    @Ignore
    public String getEquipment() {
        return firstOrFallback(equipments);
    }

    @Ignore
    public String getTarget() {
        return firstOrFallback(targetMuscles);
    }

    @Override
    public String toString() {
        return name;
    }

    private static List<String> descriptionToInstructions(String description) {
        List<String> parsed = new ArrayList<>();
        if (description != null && !description.trim().isEmpty()) {
            parsed.add(description);
        }
        return parsed;
    }

    private static String firstOrFallback(List<String> values) {
        return values != null && !values.isEmpty() ? values.get(0) : "N/A";
    }
}
