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
import com.example.muzfit.model.WeightEntry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightHistoryAdapter extends ArrayAdapter<WeightEntry> {

    public interface OnDeleteClickListener {
        void onDeleteClick(WeightEntry entry);
    }

    private final OnDeleteClickListener deleteClickListener;
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

    public WeightHistoryAdapter(@NonNull Context context, @NonNull List<WeightEntry> entries, OnDeleteClickListener deleteClickListener) {
        super(context, 0, entries);
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_food, parent, false);
        }

        WeightEntry entry = getItem(position);
        TextView nameTv = convertView.findViewById(R.id.foodNameTextView);
        ImageButton deleteBtn = convertView.findViewById(R.id.deleteFoodButton);

        if (entry != null) {
            String dateStr = dateFormat.format(new Date(entry.getDateMillis()));
            nameTv.setText(String.format(Locale.getDefault(), "%s: %.1f kg", dateStr, entry.getWeight()));
            nameTv.setTextColor(getContext().getColor(R.color.muz_on_surface));

            deleteBtn.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(entry);
                }
            });
        }

        return convertView;
    }
}
