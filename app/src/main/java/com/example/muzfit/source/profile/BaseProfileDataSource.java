package com.example.muzfit.source.profile;

import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.source.common.DataSourceCallback;

import java.util.List;

public abstract class BaseProfileDataSource {

    public abstract void fetchUsers(DataSourceCallback<List<User>> callback);

    public abstract void fetchWeightEntries(DataSourceCallback<List<WeightEntry>> callback);
}
