package com.app.mederrand.activities.auth;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.app.mederrand.R;
import com.app.mederrand.activities.BottomNavigationActivity;
import com.app.mederrand.activities.auth.signup.SignupActivity;
import com.app.mederrand.activities.auth.signup.WaitForVerificationActivity;
import com.app.mederrand.databinding.ActivityLoginBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    Context context;
    ActivityLoginBinding binding;

    OnBackPressedCallback onBackPressedCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(LoginActivity.this);
        context = LoginActivity.this;
        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        binding.editEmail.setText(R.string.text_default_user_email);
        binding.edPassword.setText(R.string.text_default_password);

        Utilities.keyBoardVisibilityListener(binding.getRoot(), binding.keyBoardVisibilityManager);
        initClickListeners();
    }

    private void initClickListeners(){
        Utilities.showHidePassword(binding.icPasswordEye, binding.edPassword);

        binding.btnCheckBox.setOnClickListener(v -> {
            if (binding.icTick.getVisibility() != VISIBLE) {
                binding.icTick.setVisibility(VISIBLE);
            }else {
                binding.icTick.setVisibility(View.GONE);
            }
        });

        Utilities.moveToClickedContentAndHideKeyBoard(LoginActivity.this, binding.btnReset, ForgotPasswordActivity.class);
        Utilities.moveToClickedContentAndHideKeyBoard(LoginActivity.this, binding.btnSignUp, SignupActivity.class);

        binding.btnSignIn.setOnClickListener(view -> {
            String email = binding.editEmail.getText().toString();
            String password = binding.edPassword.getText().toString();
            if (TextUtils.isEmpty(email)) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), LoginActivity.this, getString(R.string.text_please_enter_your_email));
            } else if (TextUtils.isEmpty(password)) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), LoginActivity.this, getString(R.string.text_please_enter_your_password));
            } else {
                Utilities.hideKeyboard(binding.btnSignIn, LoginActivity.this);
                binding.loading.getRoot().setVisibility(VISIBLE);
                userLogin(email, password);
            }
        });
    }

    //user authentication using Firebase Authentication
    //retrieves user data from the Firebase Realtime Database
    private void userLogin(String userEmail, String userPassword){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(LoginActivity.this, task -> {
            if (task.isSuccessful()) {
                //It creates a reference to the "users" node in the Firebase Realtime Database
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        binding.loading.getRoot().setVisibility(View.GONE);
                        //If the user's data exists, it retrieves the user's data model from the DataSnapshot
                        if (dataSnapshot.exists()) {
                            UserDataModel userDataModel = dataSnapshot.getValue(UserDataModel.class);
                            if (userDataModel !=null){
                                //calls the saveUserData method
                                //save the user's data locally or perform further actions with it
                                saveUserData(userDataModel, userPassword);
                            }

                        }else {
                            CustomBannerToast.showFailureToast(binding.getRoot(), LoginActivity.this, "Something went wrong !", "User Not Found");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        binding.loading.getRoot().setVisibility(View.GONE);
                        CustomBannerToast.showFailureToast(binding.getRoot(), LoginActivity.this, "Something went wrong !", Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
            }
            else {
                binding.loading.getRoot().setVisibility(View.GONE);
                CustomBannerToast.showFailureToast(binding.getRoot(), LoginActivity.this, "Something went wrong !",  "Please enter correct email and password");
            }
        });
    }

    //saving user data locally
    //navigating the user to different activities whether their account is verified or not
    private void saveUserData(UserDataModel userDataModel, String userPassword){
        //Utilities saves various user-related data
        Utilities.saveString(context, "userId",  userDataModel.getUserId());
        Utilities.saveString(context, "userEmail",  userDataModel.getEmail());
        Utilities.saveString(context, "userPassword", userPassword);
        Utilities.saveString(context, "userFullName", userDataModel.getName());
        Utilities.saveString(context, "userName", userDataModel.getUsername());
        Utilities.saveString(context, "userProfileImage", userDataModel.getProfileImage());
        Utilities.saveString(context, "userCountryCode", userDataModel.getCountry_code());
        Utilities.saveString(context, "userMobileNumber", userDataModel.getPhone());
        Utilities.saveString(context, "userType", userDataModel.getUserType());
        Utilities.saveString(context, "userVerified", userDataModel.getUserVerified());
        Utilities.saveString(context, "userAddress", userDataModel.getUserAddress());
        Utilities.saveString(context, "fcmToken", userDataModel.getDeviceToken());

        String isVerified = userDataModel.getUserVerified();

        Intent intent;
        if (isVerified.equalsIgnoreCase("Yes OR No")){
            Utilities.saveBoolean(context, "userNotVerified", true);
            intent = new Intent(context, WaitForVerificationActivity.class);
            intent.putExtra("from", "wait");
        }else if (isVerified.equalsIgnoreCase("No")){
            Utilities.saveBoolean(context, "userNotVerified", true);
            intent = new Intent(context, WaitForVerificationActivity.class);
            intent.putExtra("from", "denied");
        }
        else {
            boolean isLoggedIn = binding.icTick.getVisibility() == VISIBLE;
            Utilities.saveBoolean(LoginActivity.this, "isLoggedIn", isLoggedIn);
            if (Utilities.getBoolean(context, "userNotVerified")){
                Utilities.saveBoolean(context, "userNotVerified", false);
                intent = new Intent(context, WaitForVerificationActivity.class);
                intent.putExtra("from", "approved");
            }else {
                intent = new Intent(context, BottomNavigationActivity.class);
            }
        }

        startActivity(intent);
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