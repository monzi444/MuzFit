package com.example.muzfit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.model.Meal;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsMapper;

import java.util.List;

public class FoodSearchAdapter extends RecyclerView.Adapter<FoodSearchAdapter.ViewHolder> {

    private final List<Meal> foods;
    private final OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(Meal meal);
    }

    public FoodSearchAdapter(List<Meal> foods, OnFoodClickListener listener) {
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_food_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = foods.get(position);
        holder.tvName.setText(meal.getFoodName());
        holder.tvDetails.setText(OpenFoodFactsMapper.formatSearchSubtitle(meal));
        holder.itemView.setOnClickListener(v -> listener.onFoodClick(meal));
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDetails = itemView.findViewById(R.id.tvFoodDetails);
        }
    }
}
