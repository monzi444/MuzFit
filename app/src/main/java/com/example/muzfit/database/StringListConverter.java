package com.example.muzfit.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StringListConverter {

    private static final Gson GSON = new Gson();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    @TypeConverter
    public String fromStringList(List<String> values) {
        return GSON.toJson(values != null ? values : new ArrayList<>());
    }

    @TypeConverter
    public List<String> toStringList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> parsed = GSON.fromJson(value, STRING_LIST_TYPE);
        return parsed != null ? parsed : new ArrayList<>();
    }
}
