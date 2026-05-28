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
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class DietFragment extends Fragment {

    private ListView foodListView;
    private Button addFoodButton;
    private FoodAdapter adapter;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        foodListView = view.findViewById(R.id.foodListView);
        addFoodButton = view.findViewById(R.id.addFoodButton);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewModel.getFoodList().observe(getViewLifecycleOwner(), foodList -> {
            if (adapter == null) {
                adapter = new FoodAdapter(requireContext(), foodList);
                foodListView.setAdapter(adapter);
            } else {
                adapter.clear();
                adapter.addAll(foodList);
                adapter.notifyDataSetChanged();
            }
        });

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.addFood(new Food("New Food Item", 100));
                Toast.makeText(getContext(), getString(R.string.food_added_toast), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}