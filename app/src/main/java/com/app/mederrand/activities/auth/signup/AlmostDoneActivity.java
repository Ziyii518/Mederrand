package com.app.mederrand.activities.auth.signup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.databinding.ActivityAlmostDoneBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class AlmostDoneActivity extends AppCompatActivity {
    Context context;
    ActivityAlmostDoneBinding binding;

    String userType, userEmail, fullName, userName, password,  countryCode, phone, latitude, longitude, userAddress, device_token = "123456", device_id;

    OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlmostDoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    @SuppressLint("HardwareIds")
    private void init(){
        Utilities.hideNavAndStatusBarNew(AlmostDoneActivity.this);
        context = AlmostDoneActivity.this;
        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        getFireBaseToken();
        device_id = Settings.Secure.getString(AlmostDoneActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

        userType = getIntent().getStringExtra("userType");
        userEmail = getIntent().getStringExtra("userEmail");
        fullName = getIntent().getStringExtra("fullName");
        userName = getIntent().getStringExtra("userName");
        password = getIntent().getStringExtra("password");
        countryCode = getIntent().getStringExtra("countryCode");
        phone = getIntent().getStringExtra("phone");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");
        userAddress = getIntent().getStringExtra("userAddress");

        initClickListeners();
    }

    private void getFireBaseToken() {
        boolean gotFBCrash = false;
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    device_token = task.getResult();
                    //GOT the token!
                }
            });
        } catch (Exception e) {
            // OMG, Firebase is used to log Firebase crash :)
            // I'm not sure if this will work...
            gotFBCrash = true;
        }
        if (gotFBCrash) {
            Toast.makeText(context, "No Firebase Token Found", Toast.LENGTH_LONG).show();

        }
    }

    private void initClickListeners() {
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnNext.setOnClickListener(view -> {
            binding.loading.getRoot().setVisibility(View.VISIBLE);
            requestForCreateAccountAndSaveUser(userEmail, password);
        });
    }

    private void requestForCreateAccountAndSaveUser(String userEmail, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();
                        String userId = Objects.requireNonNull(user).getUid();

                        UserDataModel userDataModel = new UserDataModel(userId, userType, "Yes OR No", fullName, userName, userEmail, countryCode, phone, "", latitude, longitude, userAddress, device_token, device_id);


                        FirebaseDatabase.getInstance().getReference("users")
                                .child(userId)
                                .setValue(userDataModel).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        moveToWaitingScreen();
                                    } else {
                                        binding.loading.getRoot().setVisibility(View.GONE);
                                        CustomBannerToast.showRequiredToast(binding.btnBack, AlmostDoneActivity.this, "There is some issue while saving your information");
                                    }
                                });


                    }
                    else {
                        binding.loading.getRoot().setVisibility(View.GONE);
                        CustomBannerToast.showRequiredToast(binding.btnBack, AlmostDoneActivity.this, "There is some issue while creating your account");
                    }
                });
    }

    private void moveToWaitingScreen(){
        binding.loading.getRoot().setVisibility(View.GONE);
        Intent mainIntent = new Intent(context, WaitForVerificationActivity.class);
        mainIntent.putExtra("from", "wait");
        startActivity(mainIntent);
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        finishAffinity();
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