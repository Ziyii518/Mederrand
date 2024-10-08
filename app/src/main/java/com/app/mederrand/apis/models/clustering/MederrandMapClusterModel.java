package com.app.mederrand.apis.models.clustering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.mederrand.models.local.UserDataModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MederrandMapClusterModel implements ClusterItem {
    private final int itemPosition;
    private final LatLng latLng;
    private final String mSnippet;
    private final String title;

    private final UserDataModel userDataModel;


    public MederrandMapClusterModel(int itemPosition, LatLng latLng, String mSnippet, String title, UserDataModel userDataModel) {
        this.itemPosition = itemPosition;
        this.latLng = latLng;
        this.mSnippet = mSnippet;
        this.title = title;
        this.userDataModel = userDataModel;
    }


    public int getItemPosition() {
        return itemPosition;
    }

    public UserDataModel getUserDataModel() {
        return userDataModel;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return mSnippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0.0F;
    }
}
