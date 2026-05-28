package com.example.muzfit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DietFragment extends Fragment {

    private ListView foodListView;
    private Button addFoodButton;
    private List<Food> foodList;
    private FoodAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        foodListView = view.findViewById(R.id.foodListView);
        addFoodButton = view.findViewById(R.id.addFoodButton);

        foodList = new ArrayList<>(Arrays.asList(
                new Food("Apple", 95),
                new Food("Banana", 105),
                new Food("Chicken Breast", 165),
                new Food("Oatmeal", 150),
                new Food("Greek Yogurt", 100)
        ));

        adapter = new FoodAdapter(requireContext(), foodList);
        foodListView.setAdapter(adapter);

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, just add a dummy food to show it works
                foodList.add(new Food("New Food Item", 0));
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), getString(R.string.food_added_toast), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}