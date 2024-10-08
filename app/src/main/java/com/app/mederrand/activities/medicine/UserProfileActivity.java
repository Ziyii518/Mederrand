package com.app.mederrand.activities.medicine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.activities.MessagesActivity;
import com.app.mederrand.databinding.ActivityUserProfileBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {
    ActivityUserProfileBinding binding;
    Context context;
    OnBackPressedCallback onBackPressedCallback;

    String userId, otherProfileId, from, orderId;
    UserDataModel userDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        Utilities.setCustomStatusAndNavColor(UserProfileActivity.this, R.color.app_color_dark, R.color.app_color_bg);
        context = UserProfileActivity.this;

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        userId = Utilities.getString(context, "userId");
        otherProfileId = getIntent().getStringExtra("otherProfileId");
        from = getIntent().getStringExtra("from");

        if (Objects.equals(from, "order")){
            orderId = getIntent().getStringExtra("orderId");
            binding.btnHireMe.setVisibility(View.VISIBLE);
        }

        requestForUserProfile();

        initClickListeners();
    }

    private void requestForUserProfile(){
        binding.layoutMain.setVisibility(View.GONE);
        binding.loading.setVisibility(View.VISIBLE);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(otherProfileId));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userDataModel = dataSnapshot.getValue(UserDataModel.class);
                    if (userDataModel !=null){
                        binding.loading.setVisibility(View.GONE);
                        binding.layoutMain.setVisibility(View.VISIBLE);

                        binding.textUserName.setText(userDataModel.getUsername());
                        binding.textFullName.setText(userDataModel.getName());
                        binding.textUserLocation.setText(userDataModel.getUserAddress());
                    }else {
                        binding.loading.setVisibility(View.GONE);
                        binding.layoutMain.setVisibility(View.GONE);
                        CustomBannerToast.showFailureToast(binding.getRoot(), UserProfileActivity.this, "Something went wrong !", "User Not Found");
                    }

                }else {
                    binding.loading.setVisibility(View.GONE);
                    binding.layoutMain.setVisibility(View.GONE);
                    CustomBannerToast.showFailureToast(binding.getRoot(), UserProfileActivity.this, "Something went wrong !", "User Not Found");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.loading.setVisibility(View.GONE);
                binding.layoutMain.setVisibility(View.GONE);
                CustomBannerToast.showFailureToast(binding.getRoot(), UserProfileActivity.this, "Something went wrong !", Objects.requireNonNull(databaseError.getMessage()));
            }
        });

    }

    private void initClickListeners() {
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        if (userId.equals(otherProfileId)){
            binding.settingsIc.setImageResource(R.drawable.settings_ic);
        }else {
            binding.settingsIc.setImageResource(R.drawable.inbox_ic);
            binding.btnSettings.setOnClickListener(view -> {
                Intent intent = new Intent(context, MessagesActivity.class);
                intent.putExtra("otherUserName", userDataModel.getName());
                intent.putExtra("otherUserId", otherProfileId);
                intent.putExtra("otherFcmToken", userDataModel.getDeviceToken());
                startActivity(intent);
                overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
            });
        }

        binding.btnHireMe.setOnClickListener(view -> requestForHireDriver(orderId, otherProfileId));
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

    private void requestForHireDriver(String orderId, String driverId){

        // Get a reference to the orders table in Firebase
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("orders");

        // Create a map to update the order status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("orderStatus", "Out For Delivery");
        updateData.put("driverId", driverId);

        // Update the order status in the database
        ordersRef.child(orderId).updateChildren(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    CustomBannerToast.showFailureToast(binding.getRoot(), UserProfileActivity.this, "Something went wrong !",  e.getLocalizedMessage());
                });

    }
}