package com.app.mederrand.utils.services;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.app.mederrand.apis.models.medicines.MedicineDataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicineRetrievalService {

    private final DatabaseReference databaseReference;
    String userId;

    public MedicineRetrievalService(String userId) {
        // Get the reference to the "medicines" node in the Realtime Database
        this.userId = userId;
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines").child(userId);
    }

    public void retrieveMedicines(final OnMedicinesListCallback callback) {
        // Attach a listener to read the data at "medicines" node and its child
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // dataSnapshot contains the data from "medicines" node from user id
                List<MedicineDataModel> medicinesList = new ArrayList<>();

                if (dataSnapshot.exists() && dataSnapshot.hasChildren()){
                    for (DataSnapshot medicineSnapShot : dataSnapshot.getChildren()) {
                        // Convert each child to a MedicineDataModel object
                        MedicineDataModel medicineDataModel = medicineSnapShot.getValue(MedicineDataModel.class);
                        if (medicineDataModel != null) {
                            // Add the medicine to the list
                            medicinesList.add(medicineDataModel);
                        }
                    }

                    // Notify the callback with the complete list
                    if (callback != null) {
                        callback.onMedicinesListReceived(medicinesList);
                    }
                }else {
                    if (callback != null) {
                        callback.onMedicinesListReceived(Collections.emptyList());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                if (callback != null) {
                    callback.onMedicinesListReceived(Collections.emptyList());
                }
            }
        });
    }

    // Callback interface to handle the received medicines list
    public interface OnMedicinesListCallback {
        void onMedicinesListReceived(List<MedicineDataModel> medicinesList);
    }
}

