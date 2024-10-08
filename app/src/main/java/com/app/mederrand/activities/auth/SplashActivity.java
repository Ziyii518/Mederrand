package com.app.mederrand.activities.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.app.mederrand.R;
import com.app.mederrand.activities.BottomNavigationActivity;
import com.app.mederrand.databinding.ActivitySplashBinding;
import com.app.mederrand.utils.Utilities;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    public static int SPLASH_TIME = 2500;
    ActivitySplashBinding binding;
    Context context;
    boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Utilities.hideNavAndStatusBarNew(SplashActivity.this);
        context = SplashActivity.this;

        checkForInternetAndMoveNext();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void checkForInternetAndMoveNext() {
        if (Utilities.isConnected(context)) {
            binding.layoutNoInternet.getRoot().setVisibility(View.GONE);
            binding.mainLayout.setVisibility(View.VISIBLE);

            isLoggedIn = Utilities.getBoolean(context, "isLoggedIn");

            Class<?> activityToOpen;
            if (isLoggedIn){
                activityToOpen = BottomNavigationActivity.class;
            }else {
                activityToOpen = LoginActivity.class;
            }

            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> moveToNextScreen(activityToOpen), SPLASH_TIME);
        } else {
            binding.layoutNoInternet.getRoot().setVisibility(View.VISIBLE);
            binding.mainLayout.setVisibility(View.GONE);

            binding.layoutNoInternet.layoutRefreshBtn.setOnClickListener(v -> {
                binding.layoutNoInternet.getRoot().setVisibility(View.GONE);
                binding.mainLayout.setVisibility(View.VISIBLE);
                checkForInternetAndMoveNext();
            });
        }

    }

    public void moveToNextScreen(Class<?> className) {
        Intent mainIntent = new Intent(context, className);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        finish();
    }
}