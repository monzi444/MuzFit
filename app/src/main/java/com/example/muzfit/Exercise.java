package com.example.muzfit;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Exercise implements Serializable {
    @SerializedName("exerciseId")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("bodyParts")
    private List<String> bodyParts;
    
    @SerializedName("equipments")
    private List<String> equipments;
    
    @SerializedName("gifUrl")
    private String gifUrl;
    
    @SerializedName("targetMuscles")
    private List<String> targetMuscles;
    
    @SerializedName("secondaryMuscles")
    private List<String> secondaryMuscles;
    
    @SerializedName("instructions")
    private List<String> instructions;

    public String getId() { return id; }
    public String getName() { return name; }
    
    public String getBodyPart() { 
        return (bodyParts != null && !bodyParts.isEmpty()) ? bodyParts.get(0) : "N/A"; 
    }
    
    public String getEquipment() { 
        return (equipments != null && !equipments.isEmpty()) ? equipments.get(0) : "N/A"; 
    }
    
    public String getGifUrl() { return gifUrl; }
    
    public String getTarget() { 
        return (targetMuscles != null && !targetMuscles.isEmpty()) ? targetMuscles.get(0) : "N/A"; 
    }

    public List<String> getSecondaryMuscles() { return secondaryMuscles; }
    public List<String> getInstructions() { return instructions; }

    @Override
    public String toString() {
        return name;
    }
}
