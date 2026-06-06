package com.example.muzfit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
        tableName = "UserMeal",
        foreignKeys = {
                @ForeignKey(
                        entity = Meal.class,
                        parentColumns = "id",
                        childColumns = "mealId"
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "uid",
                        childColumns = "uid"
                )
        },
        indices = {@Index("mealId"), @Index("uid")}
)
public class UserMeal {

    @androidx.room.PrimaryKey(autoGenerate = true)
    private int id;
    private int mealId;
    @NonNull
    private String uid = "";
    private long dateMillis;
    private MealCategory category = MealCategory.PRANZO;

    public UserMeal() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public UserMeal(int mealId, String uid, long dateMillis) {
        this(mealId, uid, dateMillis, MealCategory.PRANZO);
    }

    @Ignore
    public UserMeal(int mealId, String uid, long dateMillis, MealCategory category) {
        this.mealId = mealId;
        this.uid = uid != null ? uid : "";
        this.dateMillis = dateMillis;
        this.category = category != null ? category : MealCategory.PRANZO;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public MealCategory getCategory() {
        return category;
    }

    public void setCategory(MealCategory category) {
        this.category = category != null ? category : MealCategory.PRANZO;
    }
}
