package com.example.muzfit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.muzfit.R;
import com.example.muzfit.model.Food;

import java.util.List;

public class FoodAdapter extends ArrayAdapter<Food> {
    private Context context;
    private List<Food> foodList;

    public FoodAdapter(@NonNull Context context, List<Food> foodList) {
        super(context, 0, foodList);
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_food, parent, false);
        }

        Food currentFood = foodList.get(position);

        TextView foodNameTextView = convertView.findViewById(R.id.foodNameTextView);
        ImageButton deleteFoodButton = convertView.findViewById(R.id.deleteFoodButton);

        if (currentFood != null) {
            foodNameTextView.setText(currentFood.toString());
        }

        deleteFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodList.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}
