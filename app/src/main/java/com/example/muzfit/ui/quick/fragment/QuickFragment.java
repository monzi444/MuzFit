package com.example.muzfit.ui.quick.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.muzfit.R;
import com.example.muzfit.model.Food;
import com.example.muzfit.model.Meal;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.ui.diet.viewmodel.DietViewModelFactory;
import com.example.muzfit.utils.ServiceLocator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class QuickFragment extends Fragment {

    private DietViewModel dietViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick, container, false);

        IDietRepository repository = ServiceLocator.getInstance().getDietRepository();
        dietViewModel = new ViewModelProvider(this, new DietViewModelFactory(repository)).get(DietViewModel.class);

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
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        ArrayAdapter<Food.Category> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, Food.Category.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        builder.setTitle("Aggiungi Cibo")
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    String name = editTextFoodName.getText().toString();
                    String caloriesStr = editTextCalories.getText().toString();
                    String carbsStr = editTextCarbs.getText().toString();
                    String proteinStr = editTextProtein.getText().toString();
                    String fatStr = editTextFat.getText().toString();
                    Food.Category selectedCategory = (Food.Category) spinnerCategory.getSelectedItem();

                    if (!name.isEmpty() && !caloriesStr.isEmpty()) {
                        int calories = Integer.parseInt(caloriesStr);
                        int carbs = carbsStr.isEmpty() ? 0 : Integer.parseInt(carbsStr);
                        int protein = proteinStr.isEmpty() ? 0 : Integer.parseInt(proteinStr);
                        int fat = fatStr.isEmpty() ? 0 : Integer.parseInt(fatStr);

                        Meal newMeal = new Meal(0, name, (float)calories, (float)carbs, (float)protein, (float)fat, selectedCategory);
                        dietViewModel.addMeal(newMeal);
                        Toast.makeText(getContext(), "Cibo aggiunto alla dieta!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Per favore inserisci nome e calorie", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }
}
