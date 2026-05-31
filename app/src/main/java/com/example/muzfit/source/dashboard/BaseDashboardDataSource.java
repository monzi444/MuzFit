package com.example.muzfit.source.dashboard;

import com.example.muzfit.model.User;
import com.example.muzfit.model.UserMeal;
import com.example.muzfit.model.Workout;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseDashboardDataSource {

    public abstract void fetchUsers(DataSourceCallback<List<User>> callback);

    public abstract void fetchAllUserMeals(DataSourceCallback<List<UserMeal>> callback);

    public abstract void fetchWorkouts(DataSourceCallback<List<Workout>> callback);
}
