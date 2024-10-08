package com.app.mederrand.activities.medicine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.app.mederrand.R;
import com.app.mederrand.apis.models.medicines.OrderDataModel;
import com.app.mederrand.databinding.ActivityOrderDetailBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {
    Context context;
    ActivityOrderDetailBinding binding;

    OnBackPressedCallback onBackPressedCallback;

    OrderDataModel ordersDetail;
    UserDataModel buyerDataModel, pharmacyDataModel, driverDataModel;

    String userId, userType;
    boolean isOrderStatusChanged = false;

    ActivityResultLauncher<Intent> onHireUserLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        Utilities.setCustomStatusAndNavColor(OrderDetailActivity.this, R.color.app_color_dark, R.color.app_color_bg);
        context = OrderDetailActivity.this;
        ordersDetail = getIntent().getParcelableExtra("ordersDetail");
        userId = Utilities.getString(context, "userId");
        userType = Utilities.getString(context, "userType");

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        initLaunchers();

        initClickListener();


        fetchUserDetailsAndShowOtherDetails(ordersDetail.getBuyerId(), ordersDetail.getPharmacyId(), ordersDetail.getDriverId());

    }


    public void fetchUserDetailsAndShowOtherDetails(String buyerId, String pharmacistId, String driverId) {
        binding.layoutMain.setVisibility(View.GONE);
        binding.loading.setVisibility(View.VISIBLE);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");


        Task<DataSnapshot> buyerTask = usersRef.child(buyerId).get();
        Task<DataSnapshot> pharmacistTask = usersRef.child(pharmacistId).get();

        Task<List<Task<?>>> taskList;
        if (!driverId.isEmpty()){
            Task<DataSnapshot> driverTask = usersRef.child(driverId).get();
            taskList = Tasks.whenAllComplete(buyerTask, pharmacistTask, driverTask);
        }else {
            taskList = Tasks.whenAllComplete(buyerTask, pharmacistTask);
        }
        taskList.addOnSuccessListener(taskResults -> {
                    for (Task<?> taskResult : taskResults) {
                        if (taskResult.isSuccessful()) {
                            DataSnapshot dataSnapshot = (DataSnapshot) taskResult.getResult();
                            if (dataSnapshot.exists()) {
                                UserDataModel userData = dataSnapshot.getValue(UserDataModel.class);
                                if (userData != null) {
                                    if (userData.getUserType().equals("Buyer")){
                                        buyerDataModel = userData;
                                    }else if (userData.getUserType().equals("Driver")){
                                        driverDataModel = userData;
                                    }else {
                                        pharmacyDataModel = userData;
                                    }
                                }
                            }
                        }
                    }

                    showOrderDetails(ordersDetail, buyerDataModel, pharmacyDataModel);
                })
                .addOnFailureListener(e -> {
                    binding.layoutMain.setVisibility(View.GONE);
                    binding.loading.setVisibility(View.GONE);
                    CustomBannerToast.showFailureToast(binding.getRoot(), OrderDetailActivity.this, "Something went wrong !",  e.getLocalizedMessage());
                });
    }

    private void showOrderDetails(OrderDataModel ordersDetail, UserDataModel buyerDataModel,
                                  UserDataModel pharmacyDataModel){
        binding.loading.setVisibility(View.GONE);
        binding.layoutMain.setVisibility(View.VISIBLE);

        if (ordersDetail !=null){
            //load pre
            Utilities.loadImage(context, ordersDetail.getPrescriptionImage(),
                    binding.prescriptionImage, binding.imageLoading);
            binding.textMedicineName.setText(ordersDetail.getMedicineName());
            binding.textOrderQuantity.setText(ordersDetail.getOrderQuantity());
            binding.textTotalPrice.setText(ordersDetail.getOrderTotalPrice());

            switch (userType) {
                case "Pharmacist" -> manageOrderButtons(ordersDetail.getOrderStatus());
                case "Driver" -> {
                    // if the order status is "Out For Delivery",shows the "Mark as Delivered" button
                    // attaches a click listener to change the order status to "Delivered"
                    if (ordersDetail.getOrderStatus().equals("Out For Delivery")) {
                        showAcceptedButtons();
                        binding.textFindDriver.setText(R.string.text_mark_as_delivered);
                        binding.btnFindDriver.setOnClickListener(view ->
                                changeOrderByOrderStatus(ordersDetail.getId(),
                                "Delivered"));
                    } else {
                        hideButtons();
                    }
                }
                case "Buyer" -> {
                    // it checks if the order status is "Delivered"shows the "Mark as Completed" button
                    // attaches a click listener to change the order status to "Completed" when clicked.
                    if (ordersDetail.getOrderStatus().equals("Delivered")) {
                        showAcceptedButtons();
                        binding.textFindDriver.setText(R.string.text_mark_as_completed);
                        binding.btnFindDriver.setOnClickListener(view -> changeOrderByOrderStatus
                                (ordersDetail.getId(),
                                "Completed"));
                    } else {
                        hideButtons();
                    }
                }
                default -> hideButtons();
            }

        }

        //populates the UI elements with the pharmacy details
        if (pharmacyDataModel !=null){
            String phoneNumber = pharmacyDataModel.getCountry_code() + pharmacyDataModel.getPhone();
            binding.textPharmacyName.setText(pharmacyDataModel.getName());
            binding.textPharmacyAddress.setText(pharmacyDataModel.getUserAddress());
            binding.textPharmacyPhoneNumber.setText(phoneNumber);
            binding.btnPharmacyProfile.setOnClickListener(view -> moveToUserProfile(pharmacyDataModel.getUserId()));
        }

        //populates the UI elements with the buyer details
        if (buyerDataModel !=null){
            String phoneNumber = buyerDataModel.getCountry_code() + buyerDataModel.getPhone();
            binding.textUserName.setText(buyerDataModel.getName());
            binding.textUserAddress.setText(buyerDataModel.getUserAddress());
            binding.textUserPhoneNumber.setText(phoneNumber);
            binding.btnBuyerProfile.setOnClickListener(view -> moveToUserProfile(buyerDataModel.getUserId()));
        }

        if (driverDataModel!=null){
            String phoneNumber = driverDataModel.getCountry_code() + driverDataModel.getPhone();
            binding.textDriverName.setText(driverDataModel.getName());
            binding.textDriverAddress.setText(driverDataModel.getUserAddress());
            binding.textDriverPhoneNumber.setText(phoneNumber);
            binding.btnDriverProfile.setOnClickListener(view -> moveToUserProfile(driverDataModel.getUserId()));
        }else {
            binding.textDriverName.setText(R.string.text_no_driver);
            binding.textDriverAddress.setText(R.string.text_no_driver_assigned_to_the_order_yet);
            binding.textDriverPhoneNumber.setText(R.string.text_no_phone_number);
        }

    }

    private void moveToUserProfile(String otherProfileId){
        Intent mainIntent = new Intent(context, UserProfileActivity.class);
        mainIntent.putExtra("otherProfileId", otherProfileId);
        mainIntent.putExtra("from", "profile");
        startActivity(mainIntent);
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }
    private void manageOrderButtons(String orderStatus){
        switch (orderStatus) {
            case "Pending" -> showPendingButtons();
            case "Accepted" -> showAcceptedButtons();
            default -> hideButtons();
        }

    }

    private void showPendingButtons() {
        binding.layoutButtons.setVisibility(View.VISIBLE);
        binding.layoutAcceptButtons.setVisibility(View.VISIBLE);
        binding.btnFindDriver.setVisibility(View.GONE);
    }

    private void showAcceptedButtons() {
        binding.layoutButtons.setVisibility(View.VISIBLE);
        binding.layoutAcceptButtons.setVisibility(View.GONE);
        binding.btnFindDriver.setVisibility(View.VISIBLE);
    }

    private void hideButtons() {
        binding.layoutButtons.setVisibility(View.GONE);
    }

    private void initClickListener(){
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnAccept.setOnClickListener(view -> changeOrderByOrderStatus(ordersDetail.getId(), "Accepted"));

        binding.btnCancel.setOnClickListener(view -> changeOrderByOrderStatus(ordersDetail.getId(), "Cancelled"));

        binding.btnFindDriver.setOnClickListener(view -> {
            Intent intent = new Intent(context, SelectDriverActivity.class);
            intent.putExtra("ordersDetail", ordersDetail);
            onHireUserLauncher.launch(intent);
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        });
    }

    private void initLaunchers(){
        onHireUserLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });
    }

    //updating the status of an order in the Firebase Realtime Database
    private void changeOrderByOrderStatus(String orderId, String orderStatus){

        // Get a reference to the orders table in Firebase
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("orders");

        // Create a map to update the order status
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("orderStatus", orderStatus);

        // Update the order status in the database
        ordersRef.child(orderId).updateChildren(updateData)
                .addOnSuccessListener(aVoid -> {
                    //if the order status has changed to "Delivered", "Completed", or "Cancelled".
                    //it sets the result of the activity as RESULT_OK
                    if (orderStatus.equals("Delivered") || orderStatus.equals("Completed") ||
                            orderStatus.equals("Cancelled")){
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                    }else {
                        isOrderStatusChanged = true;
                        manageOrderButtons(orderStatus);
                    }

                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    //shows a failure toast indicating that something went wron
                    CustomBannerToast.showFailureToast(binding.getRoot(), OrderDetailActivity.this, "Something went wrong !",  e.getLocalizedMessage());
                });

    }

    private void initOnBackPressed(){
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isOrderStatusChanged){
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);

            }
        };
    }
}