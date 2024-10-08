package com.app.mederrand.activities.medicine;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.adapters.MapClusteringAdapter;
import com.app.mederrand.apis.models.clustering.MederrandMapClusterModel;
import com.app.mederrand.apis.models.medicines.OrderDataModel;
import com.app.mederrand.databinding.ActivitySelectDriverBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.ClusterItemReader;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.MultiDrawable;
import com.app.mederrand.utils.Utilities;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SelectDriverActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, ClusterManager.OnClusterClickListener<MederrandMapClusterModel>, ClusterManager.OnClusterInfoWindowClickListener<MederrandMapClusterModel>, ClusterManager.OnClusterItemClickListener<MederrandMapClusterModel>, ClusterManager.OnClusterItemInfoWindowClickListener<MederrandMapClusterModel>, MapClusteringAdapter.MapClusteringAdapterCallBack {
    final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    Context context;
    ActivitySelectDriverBinding binding;

    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap myGoogleMap;
    private double latitude, longitude;
    int zoomLevel = 18;

    ClusterManager<MederrandMapClusterModel> mClusterManager;

    OnBackPressedCallback onBackPressedCallback;

    String userId, address, city;

    // when opening the setting activity for turning on GPS, the activity does not return any results
    // so we have to check for the results in the onResume function.
    boolean checkResultsInOnResume;

    List<UserDataModel> usersList = new ArrayList<>();
    List<MederrandMapClusterModel> mapClusteringList = new ArrayList<>();
    MapClusteringAdapter mapClusteringAdapter;

    ActivityResultLauncher<Intent> locationSettingsIntentLauncher, onHireUserLauncher;
    ActivityResultLauncher<String[]> requestPermissionLauncher;

    OrderDataModel ordersDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        Utilities.setCustomStatusAndNavColor(SelectDriverActivity.this, R.color.app_color_dark, R.color.app_color_bg);
        context = SelectDriverActivity.this;
        userId = Utilities.getString(context, "userId");
        ordersDetail = getIntent().getParcelableExtra("ordersDetail");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        initLaunchers();

        setupMap();

        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initLaunchers() {

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
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
                getCurrentLocation();
            } else {
                binding.map.setVisibility(View.GONE);
                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);
                binding.layoutLocationPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    locationSettingsIntentLauncher.launch(intent);
                });
            }
        });

        locationSettingsIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> getCurrentLocation());


        onHireUserLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });
    }

    //sets up the Google Map fragment by retrieving it from the layout
    private void setupMap() {
        //This will initialize the map, "On Map Ready" will be called after map is initialized
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // initializes the onBackPressed callback to handle the back button press
    private void initOnBackPressed() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        };
    }

    @Override
    // checks for location permissions
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myGoogleMap = googleMap;

        checkLocationPermission();
    }

    // if location permissions are granted
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permissions);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                binding.map.setVisibility(View.VISIBLE);
                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.GONE);
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        LatLng latlng = new LatLng(latitude, longitude);
                        Utilities.createMarker(myGoogleMap, context, latitude, longitude, address, R.drawable.ic_location_pointer);

                        CircleOptions circleOptions = new CircleOptions()
                                .center(latlng)
                                .radius(0)
                                .strokeWidth(0)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.parseColor("#5066469D"));
                        myGoogleMap.addCircle(circleOptions);
                        myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel));

                        requestForFetchUsersList();
                        // Request for Fetching Driver here
                    } else {
                        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                                .setWaitForAccurateLocation(false)
                                .setMinUpdateIntervalMillis(100)
                                .setMaxUpdateDelayMillis(100)
                                .build();

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();

                                latitude = Objects.requireNonNull(location1).getLatitude();
                                longitude = location1.getLongitude();

                                Utilities.createMarker(myGoogleMap, context, latitude, longitude, address, R.drawable.ic_location_pointer);


                                CircleOptions circleOptions = new CircleOptions()
                                        .center(new LatLng(latitude, longitude))
                                        .radius(0)
                                        .strokeWidth(0)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.parseColor("#5066469D"));
                                myGoogleMap.addCircle(circleOptions);

                                myGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel));

                                requestForFetchUsersList();
                                // Request for Fetching Driver here
                            }
                        };

                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        if (latitude == 0 && longitude == 0) {
                            checkLocationPermission();
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        }
                    }

                    //Get Address
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!Objects.requireNonNull(addresses).isEmpty()) {
                            city = addresses.get(0).getLocality();
                            address = addresses.get(0).getAddressLine(0);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            } else {
                binding.map.setVisibility(View.GONE);
                binding.layoutLocationPermissionDenied.getRoot().setVisibility(View.VISIBLE);
                binding.layoutLocationPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    locationSettingsIntentLauncher.launch(intent);
                });
            }

        } else {
            binding.map.setVisibility(View.GONE);
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



    public void requestForFetchUsersList() {
        binding.map.setVisibility(View.GONE);
        binding.loading.setVisibility(View.VISIBLE);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("users");

        Query query = ordersRef.orderByChild("userType").equalTo("Driver");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    UserDataModel userDataModel = userSnapshot.getValue(UserDataModel.class);

                    if (userDataModel != null) {
                        boolean isVerified = userDataModel.getUserVerified().equals("Yes OR No") || userDataModel.getUserVerified().equals("No");
                        if (!isVerified) {
                            usersList.add(userDataModel);
                        }

                    }
                }

                if (usersList != null) {
                    binding.map.setVisibility(View.VISIBLE);
                    binding.loading.setVisibility(View.GONE);
                    initClusterManager(usersList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                CustomBannerToast.showFailureToast(binding.getRoot(), SelectDriverActivity.this, "Something went wrong !", "Error while fetching drivers list");

            }
        });
    }


    // Custom Cluster Manager
    private void initClusterManager(List<UserDataModel> usersList) {

        if (mClusterManager != null) {
            mClusterManager.getMarkerCollection().clear();
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }
        mClusterManager = new ClusterManager<>(context, myGoogleMap);
        mClusterManager.setRenderer(new CustomMapClusterRender());
        myGoogleMap.setOnCameraIdleListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        readItems(usersList);

        mClusterManager.cluster();

    }

    private void readItems(List<UserDataModel> usersList) {
        List<MederrandMapClusterModel> items = new ClusterItemReader().read(usersList);
        mClusterManager.addItems(items);
    }

    @Override
    public boolean onClusterClick(Cluster<MederrandMapClusterModel> cluster) {
        mapClusteringList = new ArrayList<>(cluster.getItems());
        showClusterBottomSheet(mapClusteringList);
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MederrandMapClusterModel> cluster) {

    }

    private void showClusterBottomSheet(List<MederrandMapClusterModel> mapClusteringList) {
        // Get the Updated category

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_map_cluster, findViewById(R.id.bottom_sheet_map_cluster));

        RecyclerView recyclerview_mapClustering = sheetView.findViewById(R.id.recyclerview_mapClustering);

        showMapClusterRecyclerview(recyclerview_mapClustering, mapClusteringList);

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
        bottomSheetDialog.setDismissWithAnimation(true);
    }

    private void showMapClusterRecyclerview(RecyclerView recyclerView, List<MederrandMapClusterModel> mapClusteringList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mapClusteringAdapter = new MapClusteringAdapter(context, mapClusteringList, this);
        recyclerView.setAdapter(mapClusteringAdapter);
    }

    @Override
    public boolean onClusterItemClick(MederrandMapClusterModel item) {
        moveToUserProfile(item.getUserDataModel().getUserId());
        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(MederrandMapClusterModel item) {

    }

    @Override
    public void onClusteringUserClicked(String userId) {
        moveToUserProfile(userId);
    }


    private class CustomMapClusterRender extends DefaultClusterRenderer<MederrandMapClusterModel> {
        private final IconGenerator mIconGenerator = new IconGenerator(context);
        private final IconGenerator mClusterIconGenerator = new IconGenerator(context);
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public CustomMapClusterRender() {
            super(context, myGoogleMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, findViewById(R.id.multi_profile));
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(context);
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }


        @Override
        protected void onBeforeClusterItemRendered(@NonNull MederrandMapClusterModel clusterModel, MarkerOptions markerOptions) {
            // Draw a single person - show their profile photo and set the info window to show their name
            markerOptions.icon(getItemIcon()).title(clusterModel.getTitle());
        }

        @Override
        protected void onClusterItemUpdated(@NonNull MederrandMapClusterModel person, @NonNull Marker marker) {
        }

        /**
         * Get a descriptor for a single person (i.e., a marker outside a cluster) from their
         * profile photo to be used for a marker icon
         *
         * @return the person's profile photo as a BitmapDescriptor
         */
        private BitmapDescriptor getItemIcon() {

            mImageView.setImageResource(R.drawable.driver_ic);
            Bitmap icon = mIconGenerator.makeIcon();
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected void onBeforeClusterRendered(@NonNull Cluster<MederrandMapClusterModel> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            markerOptions.icon(getClusterIcon(cluster));
        }

        @Override
        protected void onClusterUpdated(@NonNull Cluster<MederrandMapClusterModel> cluster, @NonNull Marker marker) {
            // Same implementation as onBeforeClusterRendered() (to update cached markers)
//            marker.setIcon(getClusterIcon(cluster));
        }

        /**
         * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
         * method runs on the UI thread. Don't spend too much time in here (like in this example).
         *
         * @param cluster cluster to draw a BitmapDescriptor for
         * @return a BitmapDescriptor representing a cluster
         */
        private BitmapDescriptor getClusterIcon(Cluster<MederrandMapClusterModel> cluster) {
            List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MederrandMapClusterModel ignored : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;

                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.driver_ic);

                if (drawable != null) {
                    drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);
                }
            }

            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);
            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


    private void moveToUserProfile(String otherProfileId) {
        // Move To Driver Profile
        Intent mainIntent = new Intent(context, UserProfileActivity.class);
        mainIntent.putExtra("otherProfileId", otherProfileId);
        mainIntent.putExtra("from", "order");
        mainIntent.putExtra("orderId", ordersDetail.getId());
        onHireUserLauncher.launch(mainIntent);
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        moveMap(latitude, longitude);
    }

    private void moveMap(double latitude, double longitude) {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                city = addresses.get(0).getLocality();
                address = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LatLng latLng = new LatLng(latitude, longitude);
        myGoogleMap.clear();
        Utilities.createMarker(myGoogleMap, context, latitude, longitude, address, R.drawable.ic_location_pointer);
        myGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        myGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        myGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        myGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }
}