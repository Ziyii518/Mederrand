package com.app.mederrand.activities.auth;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.app.mederrand.R;
import com.app.mederrand.databinding.ActivityResetPasswordBinding;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;

public class ResetPasswordActivity extends AppCompatActivity {

    ActivityResetPasswordBinding binding;
    Context context;

    OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(ResetPasswordActivity.this);
        context = ResetPasswordActivity.this;

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);


        initClickListeners();
    }

    private void initClickListeners(){
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        Utilities.showHidePassword(binding.icPasswordEye, binding.edPassword);
        Utilities.showHidePassword(binding.icConfirmPasswordEye, binding.edConfirmPassword);


        binding.btnVerify.setOnClickListener(view -> {

            String inputNewPassword = binding.edPassword.getText().toString();
            String inputConfirmPassword = binding.edConfirmPassword.getText().toString();

            if (TextUtils.isEmpty(inputNewPassword)) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), ResetPasswordActivity.this, getString(R.string.text_please_enter_new_password));
            } else if (inputNewPassword.length() < 6) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), ResetPasswordActivity.this,  getString(R.string.text_password_must_contain_six_character));
            } else if (TextUtils.isEmpty(inputConfirmPassword)) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), ResetPasswordActivity.this, getString(R.string.text_please_enter_your_confirm_password));
            } else if (!TextUtils.equals(inputNewPassword, inputConfirmPassword)) {
                CustomBannerToast.showFailureToast(binding.getRoot(), ResetPasswordActivity.this,getString(R.string.text_password_mismatched), getString(R.string.text_confirm_password_does_not_matched));
            } else {
                Utilities.hideKeyboard(binding.getRoot(), context);
                backToLogin();
            }

        });
    }

    private void backToLogin(){
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
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