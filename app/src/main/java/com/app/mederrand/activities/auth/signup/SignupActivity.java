package com.app.mederrand.activities.auth.signup;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.app.mederrand.R;
import com.app.mederrand.activities.SelectLocationActivity;
import com.app.mederrand.activities.auth.CountryCodesListActivity;
import com.app.mederrand.activities.auth.ForgotPasswordOtpActivity;
import com.app.mederrand.databinding.ActivitySignupBinding;
import com.app.mederrand.models.local.CountriesModel;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.app.mederrand.utils.worker.LocationLiveDataHolder;
import com.app.mederrand.utils.worker.LocationWorker;
import com.app.mederrand.utils.worker.SendOtpWorker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {
    String[] userCategories = {"Buyer", "Pharmacist", "Driver"};
    final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    ActivityResultLauncher<Intent> locationSettingsIntentLauncher, onSelectedNewLocation;
    ActivityResultLauncher<String[]> requestLocationPermissionLauncher;

    Context context;
    ActivitySignupBinding binding;
    OnBackPressedCallback onBackPressedCallback;
    FusedLocationProviderClient fusedLocationProviderClient;

    ActivityResultLauncher<Intent> onSelectedCountryCodeLauncher;
    List<CountriesModel> countriesList;

    String userType = "", fullName, userName, userEmail, selectedCountryName, countryCode, phone, password, conPassword, otpCode, userAddress = "";
    boolean isValidUserName, checkResultsInOnResume;
    int maxLength;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        Utilities.hideNavAndStatusBarNew(SignupActivity.this);
        context = SignupActivity.this;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        otpCode = generateOtp();

        Utilities.keyBoardVisibilityListener(binding.getRoot(), binding.keyBoardVisibilityManager);

        initCategoriesSpinner();
        userNameTextWatcher();
        initCountryCodePicker();
        initClickListeners();
        initLaunchers();

        checkForLocationEnabled();

    }

    private void checkForLocationEnabled() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestLocationPermissionLauncher.launch(locationPermissions);
        }
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.
                NETWORK_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.
                    PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.GONE);
                binding.layoutMain.setVisibility(View.VISIBLE);

                OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(LocationWorker.class)
                        .setInputData(new Data.Builder().putString("key", "value").build())
                        .build();

                // Enqueue the work request
                WorkManager.getInstance(context).enqueue(locationWorkRequest);

                LocationLiveDataHolder locationLiveDataHolder = LocationLiveDataHolder.getInstance();
                locationLiveDataHolder.getLocationLiveData().observe(this, location -> {
                    // Handle the received location data
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        //Get Address
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(context, Locale.getDefault());
                        try {
                            String str = Build.MANUFACTURER;
                            if (str.equalsIgnoreCase("onePlus")) {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (Objects.requireNonNull(addresses).isEmpty()) {
                                    Toast.makeText(context, "We are unable to fetch your location", Toast.LENGTH_LONG).show();
                                } else {
                                    userAddress = addresses.get(0).getAddressLine(0);
                                    binding.textJobLocation.setText(userAddress);
                                }

                            } else {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (addresses !=null && !addresses.isEmpty()) {
                                    userAddress = addresses.get(0).getAddressLine(0);
                                    binding.textJobLocation.setText(userAddress);
                                }
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });


            } else {

                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);
                binding.layoutMain.setVisibility(View.GONE);
                binding.layoutLocationPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    locationSettingsIntentLauncher.launch(intent);
                });

            }

        }
        else {
            binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);
            binding.layoutMain.setVisibility(View.GONE);

            binding.layoutLocationPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    checkResultsInOnResume = true;
                } catch (Exception e) {
                    // ignored
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkResultsInOnResume) {
            checkResultsInOnResume = false;
            getCurrentLocation();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initCategoriesSpinner(){
        binding.userTypeSpinner.setItems(userCategories);
        binding.userTypeSpinner.setOnTouchListener((v, event) -> {
            Utilities.hideKeyboard(v, context);
            return false;
        });
        binding.userTypeSpinner.setOnItemSelectedListener((view, position, id, item) -> userType = item.toString());
    }

    private void initCountryCodePicker(){
        countriesList = new ArrayList<>();
        countriesList = Utilities.getCountriesList(context);
        selectedCountryName = Utilities.getCountryName(context);
        setCountryCodeHint(selectedCountryName);
    }

    private void setCountryCodeHint(String selectedCountryName) {
        for (CountriesModel countriesModel : countriesList) {
            String countryName = countriesModel.name();
            if (countryName !=null && !countryName.isEmpty() && countryName.equals(selectedCountryName)) {
                maxLength = Integer.parseInt(countriesModel.digit1());
                String format = countriesModel.format();
                binding.textCountryCode.setText(countriesModel.dial_code());
                binding.textCountryFlag.setText(countriesModel.flag());
                binding.edPhone.setHint(format);
                binding.edPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
        }

    }

    private void initClickListeners(){
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnCountryCodePicker.setOnClickListener(v -> {
            Utilities.hideKeyboard(v, context);
            Intent countryPickerIntent = new Intent(context, CountryCodesListActivity.class);
            onSelectedCountryCodeLauncher.launch(countryPickerIntent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        });


        binding.btnCheckBox.setOnClickListener(v -> {
            if (binding.icTick.getVisibility() != View.VISIBLE) {
                binding.icTick.setVisibility(View.VISIBLE);
            }else {
                binding.icTick.setVisibility(View.GONE);
            }
        });


        Utilities.showHidePassword(binding.icPasswordEye, binding.edPassword);
        Utilities.showHidePassword(binding.icConfirmPasswordEye, binding.edConfirmPassword);

        binding.btnSignUp.setOnClickListener(view -> checkSignUpValidations());

        binding.btnLocation.setOnClickListener(view -> {
            Intent selectionIntent = new Intent(context, SelectLocationActivity.class);
            selectionIntent.putExtra("latitude", latitude);
            selectionIntent.putExtra("longitude", longitude);
            onSelectedNewLocation.launch(selectionIntent);
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        });

    }

    private void checkSignUpValidations(){

        fullName = binding.editFullName.getText().toString().trim();
        userName = binding.editUserName.getText().toString().trim();
        userEmail = binding.editEmail.getText().toString().trim();
        countryCode = binding.textCountryCode.getText().toString().trim();
        phone = binding.edPhone.getText().toString().trim();
        password = binding.edPassword.getText().toString().trim();
        conPassword = binding.edConfirmPassword.getText().toString().trim();

        boolean isValidEmail = Utilities.isValidEmail(userEmail);

        if (TextUtils.isEmpty(userType)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, "Please select user type");
        } else if (TextUtils.isEmpty(fullName)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_full_name));
        } else if (fullName.length() < 3) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_three_character_full_name));
        }  else if (TextUtils.isEmpty(userName)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_user_name));
        }  else if (userName.length() < 3) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_three_character_user_name));
        } else if (TextUtils.isEmpty(userEmail)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_email_address));
        } else if (!isValidEmail) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_valid_email_address));
        } else if (TextUtils.isEmpty(phone)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_phone_number));
        } else if (phone.length() < maxLength) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_complete_phone_number));
        }else if (TextUtils.isEmpty(password)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_your_password));
        } else if (password.length() < 6) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_six_digit_password));
        } else if (TextUtils.isEmpty(conPassword)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_enter_confirm_password));
        } else if (!password.equals(conPassword)) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_confirm_password_does_not_matched));
        }
        else if (binding.icTick.getVisibility() != VISIBLE) {
            CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, getString(R.string.text_please_agree_to_privacy_policy));
        }
        else {
            Utilities.hideKeyboard(binding.edConfirmPassword, context);
            binding.loading.getRoot().setVisibility(View.VISIBLE);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

            // Add a ValueEventListener to check if data exists at the specified location
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                binding.loading.getRoot().setVisibility(View.GONE);
                                if (dataSnapshot.exists()) {
                                    CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, "This Email Already Exist");
                                } else {
                                    moveToVerifyOtp();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                binding.loading.getRoot().setVisibility(View.GONE);
                                CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, "There is some issue with firebase" + databaseError.getMessage());
                            }
                        });
                    }
                    else {
                        moveToVerifyOtp();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors, if any
                    CustomBannerToast.showRequiredToast(binding.btnBack, SignupActivity.this, "There is some issue with firebase database" + databaseError.getMessage());
                }
            });
        }

    }


    private void moveToVerifyOtp(){

        // SEND OTP REQUEST WITH WORK MANAGER
        OneTimeWorkRequest sendOtpRequest = new OneTimeWorkRequest.Builder(SendOtpWorker.class)
                .setInputData(createInputDataForWorker())
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(sendOtpRequest);

        Intent otpVerificationIntent = new Intent(context, ForgotPasswordOtpActivity.class);
        otpVerificationIntent.putExtra("from", "verify");
        otpVerificationIntent.putExtra("userType", userType);
        otpVerificationIntent.putExtra("userEmail", userEmail);
        otpVerificationIntent.putExtra("fullName", fullName);
        otpVerificationIntent.putExtra("userName", userName);
        otpVerificationIntent.putExtra("latitude", latitude+"");
        otpVerificationIntent.putExtra("longitude", longitude+"");
        otpVerificationIntent.putExtra("userAddress", userAddress);
        otpVerificationIntent.putExtra("password", password);
        otpVerificationIntent.putExtra("countryCode", countryCode);
        otpVerificationIntent.putExtra("phone", phone);
        otpVerificationIntent.putExtra("otpCode", otpCode);
        startActivity(otpVerificationIntent);
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }

    private Data createInputDataForWorker() {
        return new Data.Builder()
                .putString("userEmail", userEmail)
                .putString("userFullName", fullName)
                .putString("otpCode", otpCode)
                .build();
    }

    private void initLaunchers(){
        onSelectedCountryCodeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    binding.edPhone.setText("");
                    selectedCountryName = result.getData().getStringExtra("selectedCountryName");
                    setCountryCodeHint(selectedCountryName);
                }
            }
        });

        locationSettingsIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> getCurrentLocation());
        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean permissionGranted = false;
            for (int i = 0; i < result.size(); i++) {
                if (Boolean.TRUE.equals(result.get(locationPermissions[i]))) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                    break;
                }
            }

            if (permissionGranted) {
                getCurrentLocation();
            } else {
                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);
                binding.layoutMain.setVisibility(View.GONE);
                binding.layoutLocationPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    locationSettingsIntentLauncher.launch(intent);
                });
            }
        });


        onSelectedNewLocation = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    userAddress = result.getData().getStringExtra("selectedAddress");
                    latitude = result.getData().getDoubleExtra("selectedLatitude", 31.5204);
                    longitude = result.getData().getDoubleExtra("selectedLongitude", 74.3587);
                    binding.textJobLocation.setText(userAddress);
                }

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

    private void userNameTextWatcher() {
        binding.editUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidUserName) {
                    String validUsername = getValidUserName(s.toString().toLowerCase());
                    binding.editUserName.setText(validUsername);
                    binding.editUserName.setSelection(validUsername.length());
                } else {
                    isValidUserName = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private String getValidUserName(String name) {
        isValidUserName = true;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if ((ch < 'a' || ch > 'z') && (ch < '0' || ch > '9') && ch != '_' && ch != '.') {
                name = name.substring(0, i) + name.substring(i + 1);
            }
        }
        return name;
    }

    public static String generateOtp() {
        // Define the length of the OTP
        int otpLength = 6;

        // Define the range of characters allowed in the OTP
        String allowedChars = "0123456789";

        // Create a StringBuilder to store the OTP
        StringBuilder otp = new StringBuilder(otpLength);

        // Use a random number generator to pick characters from the allowedChars string
        Random random = new Random();

        for (int i = 0; i < otpLength; i++) {
            int index = random.nextInt(allowedChars.length());
            char randomChar = allowedChars.charAt(index);
            otp.append(randomChar);
        }

        // Convert the StringBuilder to a String and return the OTP
        return otp.toString();
    }


}