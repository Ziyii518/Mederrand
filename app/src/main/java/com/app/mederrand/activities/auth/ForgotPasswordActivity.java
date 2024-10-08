package com.app.mederrand.activities.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.databinding.ActivityForgotPasswordBinding;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;

public class ForgotPasswordActivity extends AppCompatActivity {
    Context context;
    ActivityForgotPasswordBinding binding;
    OnBackPressedCallback onBackPressedCallback;

    ActivityResultLauncher<Intent> onForgotPasswordUpdateLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(ForgotPasswordActivity.this);
        context = ForgotPasswordActivity.this;

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        initLaunchers();
        initClickListeners();

    }

    private void initClickListeners(){
        binding.btnNext.setOnClickListener(view -> {
            String userEmail = binding.editEmail.getText().toString();
            boolean isValidEmail = Utilities.isValidEmail(userEmail);
            if (TextUtils.isEmpty(userEmail)) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), ForgotPasswordActivity.this, getString(R.string.text_please_enter_email_address));
            }else if (!isValidEmail){
                CustomBannerToast.showRequiredToast(binding.getRoot(), ForgotPasswordActivity.this, getString(R.string.text_please_enter_valid_email_address));
            }else {
                Utilities.hideKeyboard( binding.getRoot(), context);
                Intent otpVerificationIntent = new Intent(context, ForgotPasswordOtpActivity.class);
                otpVerificationIntent.putExtra("from", "forgot");
                otpVerificationIntent.putExtra("userEmail", userEmail);
                otpVerificationIntent.putExtra("otpCode", "123456");
                onForgotPasswordUpdateLauncher.launch(otpVerificationIntent);
                overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
            }
        });

        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }
    private void initLaunchers(){
        onForgotPasswordUpdateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });
    }

    private void initOnBackPressed(){
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        };
    }
}