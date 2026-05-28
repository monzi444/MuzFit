package com.example.muzfit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<Food>> foodList = new MutableLiveData<>(new ArrayList<>(Arrays.asList(
            new Food("Apple", 95),
            new Food("Banana", 105),
            new Food("Chicken Breast", 165),
            new Food("Oatmeal", 150),
            new Food("Greek Yogurt", 100)
    )));

    public LiveData<List<Food>> getFoodList() {
        return foodList;
    }

    public void addFood(Food food) {
        List<Food> currentList = foodList.getValue();
        if (currentList != null) {
            List<Food> updatedList = new ArrayList<>(currentList);
            updatedList.add(food);
            foodList.setValue(updatedList);
        }
    }
}