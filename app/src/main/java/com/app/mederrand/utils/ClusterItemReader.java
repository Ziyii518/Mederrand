package com.app.mederrand.utils;

import com.app.mederrand.apis.models.clustering.MederrandMapClusterModel;
import com.app.mederrand.models.local.UserDataModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClusterItemReader {

    public List<MederrandMapClusterModel> read(List<UserDataModel> usersList){
        List<MederrandMapClusterModel> items = new ArrayList<>();

        for (int i = 0; i < usersList.size(); i++) {
            UserDataModel userDataModel = usersList.get(i);
            String snippet = userDataModel.getUserAddress();

            double lat = Double.parseDouble(userDataModel.getLatitude());
            double lng = Double.parseDouble(userDataModel.getLongitude());

            items.add(new MederrandMapClusterModel(i, new LatLng(lat, lng), snippet, userDataModel.getName(), userDataModel));


        }
        return items;
    }

}