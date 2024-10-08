package com.app.mederrand.activities.medicine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.apis.models.medicines.MedicineDataModel;
import com.app.mederrand.databinding.ActivityMedicineDetailBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MedicineDetailActivity extends AppCompatActivity {
    Context context;
    ActivityMedicineDetailBinding binding;
    OnBackPressedCallback onBackPressedCallback;

    MedicineDataModel medicineDetail;
    UserDataModel userDataModel;

    DisplayMetrics displayMetrics;

    String userId;

    ActivityResultLauncher<Intent> onMedicinePurchaseLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        Utilities.setCustomStatusAndNavColor(MedicineDetailActivity.this, R.color.app_color_dark, R.color.app_color_bg);
        context = MedicineDetailActivity.this;
        medicineDetail = getIntent().getParcelableExtra("medicineDetail");
        userId = Utilities.getString(context, "userId");
        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        initLaunchers();

        initClickListeners();
        initImageHeight();

        showMedicineAndUserDetails();

    }

    private void showMedicineAndUserDetails(){
        binding.layoutMain.setVisibility(View.GONE);
        binding.loading.setVisibility(View.VISIBLE);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(medicineDetail.getUserId()));
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userDataModel = dataSnapshot.getValue(UserDataModel.class);
                    if (userDataModel !=null){
                        binding.loading.setVisibility(View.GONE);
                        binding.layoutMain.setVisibility(View.VISIBLE);

                        Utilities.loadImage(context, medicineDetail.getImage(), binding.medicineImage, binding.imageLoading);
                        binding.textMedicineName.setText(medicineDetail.getName());
                        binding.textPrice.setText(medicineDetail.getPrice());
                        binding.textStock.setText(medicineDetail.getStock());

                        binding.textFullName.setText(userDataModel.getName());
                        binding.textUserName.setText(userDataModel.getUsername());
                        binding.textUserAddress.setText(userDataModel.getUserAddress());

                        if (userId.equals(medicineDetail.getUserId())){
                            binding.btnBuy.setVisibility(View.GONE);
                        }else {
                            binding.btnBuy.setVisibility(View.VISIBLE);
                        }
                    }else {
                        binding.loading.setVisibility(View.GONE);
                        binding.layoutMain.setVisibility(View.GONE);
                        CustomBannerToast.showFailureToast(binding.getRoot(), MedicineDetailActivity.this, "Something went wrong !", "User Not Found");
                    }

                }else {
                    binding.loading.setVisibility(View.GONE);
                    binding.layoutMain.setVisibility(View.GONE);
                    CustomBannerToast.showFailureToast(binding.getRoot(), MedicineDetailActivity.this, "Something went wrong !", "User Not Found");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.loading.setVisibility(View.GONE);
                binding.layoutMain.setVisibility(View.GONE);
                CustomBannerToast.showFailureToast(binding.getRoot(), MedicineDetailActivity.this, "Something went wrong !", Objects.requireNonNull(databaseError.getMessage()));
            }
        });

    }
    private void initImageHeight(){
        displayMetrics = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = binding.layoutImage.getLayoutParams();
        params.height = displayMetrics.widthPixels-1;
        binding.layoutImage.setLayoutParams(params);
    }


    private void initClickListeners() {
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnUserProfile.setOnClickListener(view -> {
            Intent mainIntent = new Intent(context, UserProfileActivity.class);
            mainIntent.putExtra("otherProfileId", medicineDetail.getUserId());
            mainIntent.putExtra("from", "profile");
            startActivity(mainIntent);
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        });

        binding.btnBuy.setOnClickListener(view -> {
            Intent mainIntent = new Intent(context, OrderMedicineActivity.class);
            mainIntent.putExtra("medicineDetail", medicineDetail);
            mainIntent.putExtra("medicineUserDetail", userDataModel);
            onMedicinePurchaseLauncher.launch(mainIntent);
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        });
    }

    private void initLaunchers(){
        onMedicinePurchaseLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
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