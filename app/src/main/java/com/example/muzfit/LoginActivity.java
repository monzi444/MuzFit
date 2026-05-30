package com.example.muzfit;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private CredentialManager credentialManager;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton createAccountButton;
    private MaterialButton googleLoginButton;
    private MaterialButton continueWithoutLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
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

    private void signInWithGoogle() {
        int webClientIdResId = getResources()
                .getIdentifier("default_web_client_id", "string", getPackageName());
        if (webClientIdResId == 0) {
            Toast.makeText(this, R.string.google_login_not_configured_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(webClientIdResId))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        runOnUiThread(() -> handleGoogleCredential(result.getCredential()));
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        });
                    }
                });
    }

    private void handleGoogleCredential(Credential credential) {
        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            CustomCredential customCredential = (CustomCredential) credential;
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(customCredential.getData());
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            setLoading(false);
            Toast.makeText(this, R.string.google_login_failed_toast, Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                        updateUI(auth.getCurrentUser());
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
}
