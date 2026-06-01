package com.example.muzfit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.model.Food;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class QuickFragment extends Fragment {

    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        view.findViewById(R.id.card_home).setOnClickListener(v -> {
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

        view.findViewById(R.id.card_add_food).setOnClickListener(v -> {
            showAddFoodDialog();
        });

        view.findViewById(R.id.card_work).setOnClickListener(v -> {
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_workout);
            }
        });

        view.findViewById(R.id.card_profile).setOnClickListener(v -> {
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }
        });

        return view;
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        EditText editTextFoodName = dialogView.findViewById(R.id.editTextFoodName);
        EditText editTextCalories = dialogView.findViewById(R.id.editTextCalories);
        EditText editTextCarbs = dialogView.findViewById(R.id.editTextCarbs);
        EditText editTextProtein = dialogView.findViewById(R.id.editTextProtein);
        EditText editTextFat = dialogView.findViewById(R.id.editTextFat);

        builder.setTitle("Add New Food")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = editTextFoodName.getText().toString();
                        String caloriesStr = editTextCalories.getText().toString();
                        String carbsStr = editTextCarbs.getText().toString();
                        String proteinStr = editTextProtein.getText().toString();
                        String fatStr = editTextFat.getText().toString();

                        if (!name.isEmpty() && !caloriesStr.isEmpty()) {
                            int calories = Integer.parseInt(caloriesStr);
                            int carbs = carbsStr.isEmpty() ? 0 : Integer.parseInt(carbsStr);
                            int protein = proteinStr.isEmpty() ? 0 : Integer.parseInt(proteinStr);
                            int fat = fatStr.isEmpty() ? 0 : Integer.parseInt(fatStr);

                            Food newFood = new Food(name, calories, carbs, protein, fat);
                            viewModel.addFood(newFood);
                            Toast.makeText(getContext(), "Cibo aggiunto alla dieta!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Per favore inserisci nome e calorie", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }
}