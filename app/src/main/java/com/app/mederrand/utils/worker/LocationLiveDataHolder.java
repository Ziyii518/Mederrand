package com.app.mederrand.utils.worker;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LocationLiveDataHolder {

    private static LocationLiveDataHolder instance;
    private final MutableLiveData<Location> locationLiveData = new MutableLiveData<>();

    public static LocationLiveDataHolder getInstance() {
        if (instance == null) {
            instance = new LocationLiveDataHolder();
        }
        return instance;
    }

    public LiveData<Location> getLocationLiveData() {
        return locationLiveData;
    }

    public void setLocationData(Location location) {
        locationLiveData.postValue(location);
    }
}
