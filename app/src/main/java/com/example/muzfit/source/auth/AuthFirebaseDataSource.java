package com.example.muzfit.source.auth;

import com.example.muzfit.source.common.DataSourceCallback;
import com.example.muzfit.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthFirebaseDataSource extends BaseAuthDataSource {

    private final FirebaseAuth firebaseAuth;

    public AuthFirebaseDataSource(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void signOut(DataSourceCallback<Void> callback) {
        try {
            firebaseAuth.signOut();
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e.getMessage() != null ? e.getMessage() : Constants.ERROR_AUTH_SIGN_OUT);
        }
    }

    @Override
    public void fetchCurrentUserEmail(DataSourceCallback<String> callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            callback.onSuccess(null);
            return;
        }
        callback.onSuccess(user.getEmail());
    }
}
