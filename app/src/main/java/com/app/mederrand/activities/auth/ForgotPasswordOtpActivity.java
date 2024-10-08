package com.app.mederrand.activities.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.activities.auth.signup.AlmostDoneActivity;
import com.app.mederrand.databinding.ActivityForgotPasswordOtpBinding;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;

import java.util.Objects;

public class ForgotPasswordOtpActivity extends AppCompatActivity {
    Context context;
    ActivityForgotPasswordOtpBinding binding;
    OnBackPressedCallback onBackPressedCallback;

    String from, userType, userEmail, fullName, userName, password, countryCode, phone, otpCode, latitude, longitude, userAddress;

    ActivityResultLauncher<Intent> onForgotPasswordUpdateLauncher;

    AlertDialog sendCodePopup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(ForgotPasswordOtpActivity.this);
        context = ForgotPasswordOtpActivity.this;

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        from = getIntent().getStringExtra("from");
        userEmail = getIntent().getStringExtra("userEmail");
        otpCode = getIntent().getStringExtra("otpCode");
        if (from.equals("verify")){
            userType = getIntent().getStringExtra("userType");
            fullName = getIntent().getStringExtra("fullName");
            userName = getIntent().getStringExtra("userName");
            password = getIntent().getStringExtra("password");
            countryCode = getIntent().getStringExtra("countryCode");
            phone = getIntent().getStringExtra("phone");
            latitude = getIntent().getStringExtra("latitude");
            longitude = getIntent().getStringExtra("longitude");
            userAddress = getIntent().getStringExtra("userAddress");
        }

        Toast.makeText(context, otpCode, Toast.LENGTH_LONG).show();

        binding.textUserEmail.setText(userEmail);

        initLaunchers();
        initClickListeners();

    }

    private void initClickListeners(){
        binding.btnNext.setOnClickListener(view -> {
            String userOtp = binding.otpView.getOTP();
            if (userOtp.isEmpty()) {
                CustomBannerToast.showRequiredToast(binding.btnBack, ForgotPasswordOtpActivity.this, getString(R.string.text_please_enter_otp_code));
            } else if (userOtp.length()<6){
                CustomBannerToast.showRequiredToast(binding.btnBack, ForgotPasswordOtpActivity.this, getString(R.string.text_please_enter_six_digit_otp_code));
            }else if (!userOtp.equals(otpCode)){
                CustomBannerToast.showRequiredToast(binding.btnBack, ForgotPasswordOtpActivity.this, getString(R.string.text_the_provided_otp_code_is_not_correct));
            } else {
                Utilities.hideKeyboard(binding.otpView, context);
                Intent intent;
                if (from.equals("verify")){
                    //Change it to Signup Activity Next Step
                    intent = new Intent(context, AlmostDoneActivity.class);
                    intent.putExtra("userType", userType);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("fullName", fullName);
                    intent.putExtra("userName", userName);
                    intent.putExtra("password", password);
                    intent.putExtra("countryCode", countryCode);
                    intent.putExtra("phone", phone);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("userAddress", userAddress);
                    startActivity(intent);
                }else {
                    intent = new Intent(context, ResetPasswordActivity.class);
                    intent.putExtra("userEmail", userEmail);
                    onForgotPasswordUpdateLauncher.launch(intent);
                }
                overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);

            }

        });

        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnResendCode.setOnClickListener(view -> requestForSendCodePopup());
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

    private void requestForSendCodePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordOtpActivity.this, R.style.CustomDialog);
        final View customLayout = LayoutInflater.from(ForgotPasswordOtpActivity.this).inflate(R.layout.popup_yes_no, null);
        builder.setView(customLayout);

        TextView text_sure = customLayout.findViewById(R.id.text_sure);
        TextView text_blockThisPerson = customLayout.findViewById(R.id.text_blockThisPerson);

        int sendCode, areYouSure;
        sendCode = R.string.text_resend_code_exclamation;
        areYouSure = R.string.text_sure_to_resend_code ;


        text_blockThisPerson.setText(sendCode);
        text_sure.setText(areYouSure);

        TextView btn_yes = customLayout.findViewById(R.id.text_Yes);
        TextView text_No = customLayout.findViewById(R.id.text_No);

        text_No.setOnClickListener(view -> sendCodePopup.dismiss());

        btn_yes.setOnClickListener(view -> {
            binding.otpView.setOTP("");
            sendCodePopup.dismiss();
        });

        sendCodePopup = builder.create();
        Objects.requireNonNull(sendCodePopup.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        sendCodePopup.show();
        sendCodePopup.setCancelable(true);
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