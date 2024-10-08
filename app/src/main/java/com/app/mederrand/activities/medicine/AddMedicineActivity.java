package com.app.mederrand.activities.medicine;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.apis.models.medicines.MedicineDataModel;


import com.app.mederrand.databinding.ActivityAddMedicineBinding;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;

public class AddMedicineActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    final
    String[] androidPermissionAbove13 = {Manifest.permission.READ_MEDIA_IMAGES};
    final String[] androidPermissionForOld = {Manifest.permission.READ_EXTERNAL_STORAGE};


    Context context;
    ActivityAddMedicineBinding binding;

    OnBackPressedCallback onBackPressedCallback;

    ActivityResultLauncher<Intent> onSelectMediaMethodLauncher, gallerySettingsIntentLauncher, onMedicinePurchaseLauncher;
    ActivityResultLauncher<String[]> requestGalleryPermissionLauncher;


    DisplayMetrics displayMetrics;
    String userId, userFullName;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        //hides the navigation and status bar
        Utilities.hideNavAndStatusBarNew(AddMedicineActivity.this);
        context = AddMedicineActivity.this;

        //retrieves the user ID and full name
        userId = Utilities.getString(context, "userId");
        userFullName = Utilities.getString(context, "userFullName");

        //likely ensures proper navigation behavior when the back button is pressed
        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        //adjust the UI layout based on keyboard visibility changes
        Utilities.keyBoardVisibilityListener(binding.getRoot(), binding.keyBoardVisibilityManager);
        initImageHeight();

        initLaunchers();

        initClickListeners();

    }



    private void initImageHeight(){
        //Display metrics include information such as screen width, height, density
        displayMetrics = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = binding.layoutImage.getLayoutParams();
        //sets the height of the image to be equal to the screen width minus 26 pixels
        params.height = displayMetrics.widthPixels-26;
        binding.layoutImage.setLayoutParams(params);
    }

    private void initClickListeners() {
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnSelectPicture.setOnClickListener(v -> checkGalleryPermission());

        binding.btnDelete.setOnClickListener(view -> {
            binding.ivCover.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
            binding.btnSelectPicture.setVisibility(View.VISIBLE);
            imageUri = null;
        });


        binding.btnAdd.setOnClickListener(view -> {
            String medicineName = binding.editMedicineName.getText().toString().trim();
            String medicinePrice = binding.editMedicinePrice.getText().toString().trim();
            String medicineStock = binding.editMedicineStock.getText().toString().trim();

            //check if information entered successsfully
            if (binding.ivCover.getVisibility() == View.GONE) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), AddMedicineActivity.this,
                        getResources().getString(R.string.text_please_select_medicine_image));
            }else if (TextUtils.isEmpty(medicineName)){
                CustomBannerToast.showRequiredToast(binding.getRoot(), AddMedicineActivity.this,
                        "Please enter medicine name");
            }else {
                Utilities.hideKeyboard(binding.getRoot(), context);
                binding.loading.getRoot().setVisibility(View.VISIBLE);
                uploadMedicineImageAndSaveInDataBase(imageUri, medicineName, medicinePrice, medicineStock);
            }
        });
    }

    private void uploadMedicineImageAndSaveInDataBase(Uri imageUri, String medicineName, String medicinePrice,
                                                      String medicineStock){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageId = UUID.randomUUID().toString();

        StorageReference medicineImageRef = storageRef.child("medicine_images").child(userId).
                child(imageId+".jpg");

        medicineImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image upload successful, now save the medicine object in the database
            medicineImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Now you have the correct download URL
                String imageUrl = uri.toString();
                // Save the imageUrl to the Realtime Database
                saveMedicineDataToDatabase(userId, imageUrl, medicineName, medicinePrice, medicineStock);
            });

        }).addOnFailureListener(e -> {
            CustomBannerToast.showFailureToast(binding.getRoot(), AddMedicineActivity.this,
                    "Something went wrong!","There is some issue in uploading medicine image");
        });
    }

    private void saveMedicineDataToDatabase(String userId, String imageUrl, String medicineName, String medicinePrice,
                                            String medicineStock) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("medicines").child(userId);

        // Generate a unique key for the new medicine
        String medicineId = databaseRef.push().getKey();
        // Create a Medicine object
        MedicineDataModel medicineDataModel = new MedicineDataModel(medicineId, userId, userFullName,
                medicineName, medicinePrice, medicineStock, imageUrl);
        // Save the medicine object under the medicine's node
        databaseRef.child(Objects.requireNonNull(medicineId)).setValue(medicineDataModel, (error, ref) -> {
               if (error!=null){
                   CustomBannerToast.showFailureToast(binding.getRoot(), AddMedicineActivity.this,
                           "Something went wrong!","There is some issue in saving medicine data");
               }else {
                   Intent intent = new Intent();
                   setResult(RESULT_OK, intent);
                   finish();
                   overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
               }
        });
    }



    private void initLaunchers() {
        requestGalleryPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

            String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = androidPermissionAbove13;
            } else {
                permissions = androidPermissionForOld;
            }

            boolean permissionGranted = false;
            for (int i = 0; i < result.size(); i++) {
                if (Boolean.TRUE.equals(result.get(permissions[i]))) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                    break;
                }
            }

            if (permissionGranted) {
                pickImageFromGallery();
            } else {
                binding.layoutMain.setVisibility(View.GONE);
                binding.layoutPermissionDenied.getRoot().setVisibility(View.VISIBLE);

                binding.layoutPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    gallerySettingsIntentLauncher.launch(intent);
                });
            }
        });

        gallerySettingsIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> checkGalleryPermission());

        onSelectMediaMethodLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    imageUri = data.getData();
                    if (imageUri!=null) {
                        binding.btnDelete.setVisibility(View.VISIBLE);
                        binding.ivCover.setVisibility(View.VISIBLE);
                        binding.btnSelectPicture.setVisibility(View.GONE);
                        Glide.with(context).load(imageUri).into(binding.ivCover);
                    }
                } else {
                    Toast.makeText(context, getString(R.string.text_no_image_selected), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                binding.layoutMain.setVisibility(View.VISIBLE);
                binding.layoutPermissionDenied.getRoot().setVisibility(View.GONE);
                pickImageFromGallery();
            } else {
                requestGalleryPermissionLauncher.launch(androidPermissionAbove13);
            }
        } else {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                binding.layoutMain.setVisibility(View.VISIBLE);
                binding.layoutPermissionDenied.getRoot().setVisibility(View.GONE);
                pickImageFromGallery();
            } else {
                requestGalleryPermissionLauncher.launch(androidPermissionForOld);
            }
        }

    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        onSelectMediaMethodLauncher.launch(intent);
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