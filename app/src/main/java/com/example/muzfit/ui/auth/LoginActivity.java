package com.example.muzfit.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.muzfit.R;
import com.example.muzfit.ui.MainActivity;
import com.example.muzfit.utils.ThemeHelper;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton createAccountButton;
    private MaterialButton googleLoginButton;
    private MaterialButton continueWithoutLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);

        loginButton = findViewById(R.id.login_button);
        googleLoginButton = findViewById(R.id.google_login_button);
        createAccountButton = findViewById(R.id.create_account_button);
        continueWithoutLoginButton = findViewById(R.id.continue_without_login_button);

        loginButton.setOnClickListener(v -> loginWithEmail());
        createAccountButton.setOnClickListener(v -> createAccount());
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
        continueWithoutLoginButton.setOnClickListener(v -> openMainActivity());
    }

    private void setupGoogleSignIn() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        setLoading(false);
                        return;
                    }

                    try {
                        SignInCredential credential =
                                oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            firebaseAuthWithGoogle(idToken);
                        } else {
                            setLoading(false);
                            Toast.makeText(this, R.string.google_login_failed_toast, Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    } catch (ApiException e) {
                        setLoading(false);
                        Toast.makeText(this, R.string.google_login_failed_toast, Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });

        int webClientIdResId = getResources()
                .getIdentifier("default_web_client_id", "string", getPackageName());
        if (webClientIdResId == 0) {
            return;
        }

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(webClientIdResId))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(true)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(auth.getCurrentUser());
    }

    private void loginWithEmail() {
        String email = getInputText(emailInput);
        String password = getInputText(passwordInput);

        if (!hasCredentials(email, password)) {
            return;
        }

        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                        updateUI(auth.getCurrentUser());
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_failed_toast);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void createAccount() {
        String email = getInputText(emailInput);
        String password = getInputText(passwordInput);

        if (!hasCredentials(email, password)) {
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.account_created_toast, Toast.LENGTH_SHORT).show();
                        openProfileSetupActivity();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_failed_toast);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void signInWithGoogle() {
        int webClientIdResId = getResources()
                .getIdentifier("default_web_client_id", "string", getPackageName());
        if (webClientIdResId == 0) {
            Toast.makeText(this, R.string.google_login_not_configured_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (oneTapClient == null || signInRequest == null) {
            Toast.makeText(this, R.string.google_login_not_configured_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent()).build();
                    googleSignInLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(this, e -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.google_login_failed_toast, Toast.LENGTH_SHORT).show();
                    updateUI(null);
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                        boolean isNewUser = task.getResult() != null
                                && task.getResult().getAdditionalUserInfo() != null
                                && task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            openProfileSetupActivity();
                        } else {
                            updateUI(auth.getCurrentUser());
                        }
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.google_login_failed_toast);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private boolean hasCredentials(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.missing_credentials_toast, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getInputText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        createAccountButton.setEnabled(!loading);
        googleLoginButton.setEnabled(!loading);
        continueWithoutLoginButton.setEnabled(!loading);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            openMainActivity();
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openProfileSetupActivity() {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        startActivity(intent);
        finish();
    }
}
