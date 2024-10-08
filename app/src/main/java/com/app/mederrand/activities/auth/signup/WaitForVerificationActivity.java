package com.app.mederrand.activities.auth.signup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.activities.BottomNavigationActivity;
import com.app.mederrand.activities.auth.LoginActivity;
import com.app.mederrand.databinding.ActivityWaitForVerificationBinding;
import com.app.mederrand.utils.Utilities;

import java.util.Objects;

public class WaitForVerificationActivity extends AppCompatActivity {
    Context context;
    ActivityWaitForVerificationBinding binding;

    OnBackPressedCallback onBackPressedCallback;
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWaitForVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(WaitForVerificationActivity.this);
        context = WaitForVerificationActivity.this;
        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        from = getIntent().getStringExtra("from");
        if (Objects.equals(from, "wait")){
            binding.imageWaiting.setImageResource(R.drawable.waiting_ic);
            binding.textHeading.setText(R.string.text_waiting_for_confirmation);
            binding.textDescription.setText(R.string.text_waiting_for_confirmation_message);
            binding.btnNext.setOnClickListener(view -> moveToNextScreen(LoginActivity.class));
        }
        else if (Objects.equals(from, "denied")){
            binding.imageWaiting.setImageResource(R.drawable.verification_failed_ic);
            binding.textHeading.setText(R.string.text_account_verification_failed);
            binding.textDescription.setText(R.string.text_account_verification_failed_message);
            binding.btnNext.setOnClickListener(view -> moveToNextScreen(LoginActivity.class));
        }else if (Objects.equals(from, "approved")){
            binding.imageWaiting.setImageResource(R.drawable.verification_success_ic);
            binding.textHeading.setText(R.string.text_account_verified);
            binding.textDescription.setText(R.string.text_account_verified_message);
            binding.btnNext.setOnClickListener(view -> moveToNextScreen(BottomNavigationActivity.class));
        }

    }


    private void initOnBackPressed(){
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveToNextScreen(LoginActivity.class);
            }
        };
    }

    private void moveToNextScreen(Class<?> className){
        Intent intent = new Intent(context, className);
        startActivity(intent);
        if (from.equals("approved")){
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        }else {
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
        }

        finishAffinity();
    }
}