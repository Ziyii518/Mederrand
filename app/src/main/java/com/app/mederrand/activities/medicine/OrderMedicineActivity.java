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
import com.app.mederrand.apis.models.medicines.OrderDataModel;
import com.app.mederrand.databinding.ActivityOrderMedicineBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;

public class OrderMedicineActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    final
    String[] androidPermissionAbove13 = {Manifest.permission.READ_MEDIA_IMAGES};
    final String[] androidPermissionForOld = {Manifest.permission.READ_EXTERNAL_STORAGE};

    Context context;
    ActivityOrderMedicineBinding binding;

    OnBackPressedCallback onBackPressedCallback;
    ActivityResultLauncher<Intent> onSelectMediaMethodLauncher, gallerySettingsIntentLauncher;
    ActivityResultLauncher<String[]> requestGalleryPermissionLauncher;

    MedicineDataModel medicineDetail;
    UserDataModel userDataModel;
    String userId, orderPrice = "";
    Uri imageUri;

    private int currentQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        Utilities.hideNavAndStatusBarNew(OrderMedicineActivity.this);
        context = OrderMedicineActivity.this;
        medicineDetail = getIntent().getParcelableExtra("medicineDetail");
        userDataModel = getIntent().getParcelableExtra("medicineUserDetail");
        userId = Utilities.getString(context, "userId");

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        initLaunchers();
        initClickListeners();
        initImageHeight();


        binding.textMedicineName.setText(medicineDetail.getName());
        binding.textMedicinePrice.setText(medicineDetail.getPrice());
        updateQuantityAndPrice();

        binding.textFullName.setText(userDataModel.getName());
        binding.textUserName.setText(userDataModel.getUsername());
        binding.textUserAddress.setText(userDataModel.getUserAddress());

    }

    private void initImageHeight(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = binding.layoutImage.getLayoutParams();
        params.height = displayMetrics.widthPixels-1;
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

        binding.btnConfirmOrder.setOnClickListener(view -> {
            if (binding.ivCover.getVisibility() == View.GONE) {
                CustomBannerToast.showRequiredToast(binding.getRoot(), OrderMedicineActivity.this, getResources().getString(R.string.text_please_select_prescription_image));
            }else {
                Utilities.hideKeyboard(binding.getRoot(), context);
                binding.loading.getRoot().setVisibility(View.VISIBLE);
                uploadOrderImageAndSaveInDataBase(imageUri);
            }
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                updateQuantityAndPrice();
            }
        });

        binding.btnAdd.setOnClickListener(v -> {
            if (currentQuantity < Integer.parseInt(medicineDetail.getStock())) {
                currentQuantity++;
                updateQuantityAndPrice();
            }
        });
    }

    private void uploadOrderImageAndSaveInDataBase(Uri imageUri){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageId = UUID.randomUUID().toString();

        StorageReference orderImageRef = storageRef.child("order_images").child(userId).child(imageId+".jpg");

        orderImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image upload successful, now save the medicine object in the database
            orderImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Now you have the correct download URL
                String imageUrl = uri.toString();
                // Save the imageUrl to the Realtime Database
                saveOrderDataToDatabase(imageUrl, medicineDetail.getId(), medicineDetail.getName(),
                        medicineDetail.getImage(), userId, medicineDetail.getUserId(),
                        medicineDetail.getUserFullName(),currentQuantity+"", orderPrice);
            });

        }).addOnFailureListener(e -> {
            CustomBannerToast.showFailureToast(binding.getRoot(), OrderMedicineActivity.this,
                    "Something went wrong!","There is some issue in uploading prescription image");
        });
    }

    private void saveOrderDataToDatabase(String prescriptionImage, String medicineId, String medicineName,
                                         String medicineImage, String buyerId, String pharmacyId, String pharmacyName,
                                         String orderQuantity, String orderTotalPrice) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("orders");

        // Generate a unique key for the new order
        String orderId = databaseRef.push().getKey();
        // Create a Order object
        OrderDataModel orderDataModel = new OrderDataModel(orderId, prescriptionImage, medicineId, medicineName,
                medicineImage, buyerId, pharmacyId, pharmacyName, "", "Pending", orderQuantity, orderTotalPrice);
        // Save the order object under the order's node
        databaseRef.child(Objects.requireNonNull(orderId)).setValue(orderDataModel, (error, ref) -> {
            if (error!=null){
                CustomBannerToast.showFailureToast(binding.getRoot(), OrderMedicineActivity.this,
                        "Something went wrong!","There is some issue in saving medicine data");
            }else {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });
    }


    private void updateQuantityAndPrice() {
        orderPrice = (Float.parseFloat(medicineDetail.getPrice())* currentQuantity) +"";

        binding.textQuantity.setText(String.valueOf(currentQuantity));

        binding.textOrderPrice.setText(orderPrice);

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
                binding.btnConfirmOrder.setVisibility(View.GONE);
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
                binding.btnConfirmOrder.setVisibility(View.VISIBLE);
                binding.layoutPermissionDenied.getRoot().setVisibility(View.GONE);
                pickImageFromGallery();
            } else {
                requestGalleryPermissionLauncher.launch(androidPermissionAbove13);
            }
        } else {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                binding.layoutMain.setVisibility(View.VISIBLE);
                binding.btnConfirmOrder.setVisibility(View.VISIBLE);
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