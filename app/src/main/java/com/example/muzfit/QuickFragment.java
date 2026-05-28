package com.example.muzfit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
            viewModel.addFood(new Food("Quick Snack", 150));
            Toast.makeText(getContext(), "Cibo aggiunto alla dieta!", Toast.LENGTH_SHORT).show();
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
}