package com.example.muzfit.source.auth;

import com.example.muzfit.source.common.DataSourceCallback;

public abstract class BaseAuthDataSource {

    public abstract void signOut(DataSourceCallback<Void> callback);

    public abstract void fetchCurrentUserEmail(DataSourceCallback<String> callback);
}
