package com.example.muzfit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private TextView tvQuote, tvAuthor;
    private LinearLayout splashLayout;
    private final Handler handler = new Handler();
    private Runnable transitionRunnable;
    private boolean isTransitioning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tvQuote = findViewById(R.id.tvSplashQuote);
        tvAuthor = findViewById(R.id.tvSplashAuthor);
        splashLayout = findViewById(R.id.splashLayout);

        splashLayout.setOnClickListener(v -> transitionToMain());

        fetchQuote();
    }

    private void fetchQuote() {
        RetrofitClient.getStoicApiService().getQuote().enqueue(new Callback<StoicQuoteResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoicQuoteResponse> call, @NonNull Response<StoicQuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StoicQuote quote = response.body().getData();
                    tvQuote.setText(String.format("\"%s\"", quote.getQuote()));
                    tvAuthor.setText(String.format("- %s", quote.getAuthor()));
                } else {
                    setDefaultQuote();
                }
                startTimer();
            }

            @Override
            public void onFailure(@NonNull Call<StoicQuoteResponse> call, @NonNull Throwable t) {
                setDefaultQuote();
                startTimer();
            }
        });
    }

    private void setDefaultQuote() {
        tvQuote.setText("\"Waste no more time arguing what a good man should be. Be one.\"");
        tvAuthor.setText("- Marcus Aurelius");
    }

    private void startTimer() {
        transitionRunnable = this::transitionToMain;
        handler.postDelayed(transitionRunnable, 20000); // 20 seconds
    }

    private void transitionToMain() {
        if (isTransitioning) return;
        isTransitioning = true;

        if (transitionRunnable != null) {
            handler.removeCallbacks(transitionRunnable);
        }

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);
        fadeOut.setFillAfter(true);
        splashLayout.startAnimation(fadeOut);

        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("START_QUICK", true);
            startActivity(intent);
            finish();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transitionRunnable != null) {
            handler.removeCallbacks(transitionRunnable);
        }
    }
}
