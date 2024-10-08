package com.app.mederrand.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.mederrand.R;
import com.app.mederrand.databinding.ActivitySelectLocationBinding;
import com.app.mederrand.models.local.LocationHelper;
import com.app.mederrand.utils.Utilities;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final String TAG = "SelectLocationActivity";

    Context context;
    ActivitySelectLocationBinding binding;

    Boolean locationPermissionGranted = false;
    GoogleMap mMap;
    Location lastKnownLocation;
    FusedLocationProviderClient fusedLocationProviderClient;


    double longitude, latitude;
    String postalCode, address, city, state, country, knownName;

    LatLng latlng1, myLatLng;
    LocationHelper placeLocation;
    MarkerOptions markerOptions;

    Double oldLatitude, oldLongitude;

    ActivityResultLauncher<Intent> appSettingsIntentLauncher;

    // when opening the setting activity for turning on GPS, the activity does not return any results
    // so we have to check for the results in the onResume function.
    boolean checkResultsInOnResume;
    OnBackPressedCallback onBackPressedCallback;

    AutocompleteSupportFragment autocompleteSupportFragment1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        Utilities.hideNavAndStatusBarNew(SelectLocationActivity.this);
        context = SelectLocationActivity.this;
        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        oldLatitude = getIntent().getDoubleExtra("latitude", 0);
        oldLongitude = getIntent().getDoubleExtra("longitude", 0);

        initLaunchers();
        initClickListeners();

        checkForInternetAndMoveNext();
    }

    private void initClickListeners() {
        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnPickLocation.setOnClickListener(v -> moveMap(latitude, longitude, "pickLocation"));
        binding.btnMyLocation.setOnClickListener(v -> getDeviceLocation());
    }

    private void initLaunchers() {
        appSettingsIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> initAllViews());
    }

    private void initOnBackPressed() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        };
    }

    private void checkForInternetAndMoveNext() {

        if (Utilities.isConnected(context)) {
            binding.layoutNoInternet.getRoot().setVisibility(View.GONE);
            binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.GONE);
            binding.mainLayout.setVisibility(View.GONE);

            initAllViews();

        } else {
            binding.layoutNoInternet.getRoot().setVisibility(View.VISIBLE);
            binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.GONE);
            binding.mainLayout.setVisibility(View.GONE);
            binding.layoutNoInternet.getRoot().setOnClickListener(v -> {
                binding.layoutNoInternet.getRoot().setVisibility(View.GONE);
                binding.mainLayout.setVisibility(View.VISIBLE);
                checkForInternetAndMoveNext();
            });
        }


    }

    private void initAllViews() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            getLocationPermission();
            //Search Places Code
            if (!Places.isInitialized()) {
                Places.initialize(SelectLocationActivity.this, getString(R.string.map_key));
            }
            Places.createClient(SelectLocationActivity.this);

            autocompleteSupportFragment1 = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment1);
            if (autocompleteSupportFragment1 != null) {
                autocompleteSupportFragment1.setHint(getString(R.string.text_search_location));
                autocompleteSupportFragment1.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
                autocompleteSupportFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        latlng1 = place.getLatLng();
                        markerOptions = new MarkerOptions();
                        mMap.clear();
                        markerOptions.position(latlng1);
                        markerOptions.title("Location");
                        markerOptions.icon((BitmapDescriptorFactory.fromResource(R.drawable.location_ic)));
                        placeLocation = new LocationHelper(
                                latlng1.longitude,
                                latlng1.latitude
                        );

                        latitude = latlng1.latitude;
                        longitude = latlng1.longitude;
                        getAddress(SelectLocationActivity.this, placeLocation.getLatitude(), placeLocation.getLongitude());
                        placeLocation = new LocationHelper(placeLocation.getLatitude(), placeLocation.getLongitude());

                        mMap.clear();
                        moveMap(latitude, longitude, "searchPlaces");
                    }

                    @Override
                    public void onError(@NonNull Status status) {
                    }
                });
            }
        }
    }

    private void getLocationPermission() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationEnabled();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            // GPS is disabled
            binding.mainLayout.setVisibility(View.GONE);
            binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationEnabled();
            } else {
                binding.mainLayout.setVisibility(View.GONE);
                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);

                binding.layoutLocationPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    appSettingsIntentLauncher.launch(intent);
                });
            }
        }

    }


    private void locationEnabled() {
        binding.mainLayout.setVisibility(View.VISIBLE);
        binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.GONE);
        locationPermissionGranted = true;
        updateLocationUI();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapLongClick(@NonNull @NotNull LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
    }

    @Override
    public boolean onMarkerClick(@NonNull @NotNull Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(@NonNull @NotNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull @NotNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull @NotNull Marker marker) {
        // getting the Co-ordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //move to current position
        moveMap(latitude, longitude, "drag");
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        mMap = googleMap;

        /*
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
            if (!success) {
                Log.e("TAG", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("TAG", "Can't find style. Error: ", e);
        }
        */

        getDeviceLocation();
        getMarkerPosition();

    }

    public void getMarkerPosition() {
        mMap.setOnCameraIdleListener(() -> {
            latitude = mMap.getCameraPosition().target.latitude;
            longitude = mMap.getCameraPosition().target.longitude;
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    postalCode = addresses.get(0).getPostalCode();
                    city = addresses.get(0).getLocality();
                    address = addresses.get(0).getAddressLine(0);
                    state = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    knownName = addresses.get(0).getFeatureName();
                    Log.d("LocationDetail", postalCode + city + address + state + knownName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            LatLng latLng1 = new LatLng(latitude, longitude);
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng1)
                    .title(city + " " + country));
        });

    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();

                        float zoomLevel = 18.0f; //This goes up to 21

                        if (lastKnownLocation != null) {

                            if (oldLatitude != 0 && oldLongitude != 0) {
                                //For Zoom MAp
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(oldLatitude, oldLongitude), zoomLevel));
                                //
                                myLatLng = new LatLng(oldLatitude, oldLongitude);
                            } else {
                                //For Zoom MAp
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), zoomLevel));
                                //
                                myLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            }
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
                            try {
                                addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                                postalCode = Objects.requireNonNull(addresses).get(0).getPostalCode();
                                city = Objects.requireNonNull(addresses).get(0).getLocality();
                                address = Objects.requireNonNull(addresses).get(0).getAddressLine(0);
                                state = Objects.requireNonNull(addresses).get(0).getAdminArea();
                                country = Objects.requireNonNull(addresses).get(0).getCountryName();
                                knownName = Objects.requireNonNull(addresses).get(0).getFeatureName();
                                Log.d("LocationDetail", postalCode + city + address + state + knownName);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mMap.addMarker(new MarkerOptions().position(myLatLng).draggable(false).title(city + " " + country));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            mMap.getUiSettings().setZoomControlsEnabled(false);
                            mMap.getUiSettings().setZoomGesturesEnabled(true);
                        } else {
                            if (oldLatitude != 0 && oldLongitude != 0) {
                                //For Zoom MAp
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(oldLatitude, oldLongitude), zoomLevel));
                                myLatLng = new LatLng(oldLatitude, oldLongitude);
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.852, 151.211), zoomLevel));
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            }
                        }
                    } else {
                        float zoomLevel = 18.0f; //This goes up to 21
                        if (oldLatitude != 0 && oldLongitude != 0) {
                            //For Zoom MAp
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(oldLatitude, oldLongitude), zoomLevel));
                            myLatLng = new LatLng(oldLatitude, oldLongitude);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.852, 151.211), zoomLevel));
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    private void moveMap(double latitude, double longitude, String callFor) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                postalCode = addresses.get(0).getPostalCode();
                city = addresses.get(0).getLocality();
                address = addresses.get(0).getAddressLine(0);
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                knownName = addresses.get(0).getFeatureName();

                LatLng latLng = new LatLng(latitude, longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(city + " " + country + state + knownName));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(true);

                if (callFor.equals("pickLocation")) {
                    Intent intent = new Intent();
                    intent.putExtra("selectedAddress", address);
                    intent.putExtra("selectedLocation", city + ", " + country);
                    intent.putExtra("selectedCity", city);
                    intent.putExtra("selectedCountry", country);
                    intent.putExtra("selectedLatitude", latitude);
                    intent.putExtra("selectedLongitude", longitude);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            double finalLat = LATITUDE / 100000;
            double finalLong = LONGITUDE / 100000;
            List<Address> addresses = geocoder.getFromLocation(finalLat, finalLong, 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (checkResultsInOnResume) {
            checkResultsInOnResume = false;
            initAllViews();
        }
    }
}