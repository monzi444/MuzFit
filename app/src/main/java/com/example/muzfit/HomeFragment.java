package com.example.muzfit;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Random;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        GridLayout heatmapGrid = view.findViewById(R.id.heatmap_grid);
        if (heatmapGrid != null) {
            setupHeatmap(heatmapGrid);
        }

        return view;
    }

    private void setupHeatmap(GridLayout grid) {
        int squaresCount = 140; // 20 columns * 7 rows
        Random random = new Random();
        
        // Define square size in pixels (approx 12dp)
        int size = (int) (12 * getResources().getDisplayMetrics().density);
        int margin = (int) (2 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < squaresCount; i++) {
            View square = new View(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            square.setLayoutParams(params);

            // Randomly color them to simulate activity
            if (random.nextBoolean()) {
                square.setBackgroundColor(getResources().getColor(R.color.square_filled));
            } else {
                square.setBackgroundColor(getResources().getColor(R.color.square_empty));
            }
            grid.addView(square);
        }
    }
}