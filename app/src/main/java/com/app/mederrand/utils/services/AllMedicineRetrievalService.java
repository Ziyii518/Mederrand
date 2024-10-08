package com.app.mederrand.utils.services;

import androidx.annotation.NonNull;

import com.app.mederrand.apis.models.medicines.MedicineDataModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllMedicineRetrievalService {
    private final DatabaseReference databaseReference;

    public AllMedicineRetrievalService() {
        // Get the reference to the "medicines" node in the Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines");
    }

    public void retrieveMedicines(final AllMedicinesListCallback callback) {
        // Attach a listener to read the data at the "medicines" node
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // dataSnapshot contains the data from the "medicines" node
                List<MedicineDataModel> medicinesList = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Iterate over each user node
                    for (DataSnapshot medicineSnapshot : userSnapshot.getChildren()) {
                        // Iterate over each medicine node under the user
                        // Convert each medicine to a MedicineDataModel object
                        MedicineDataModel medicineDataModel = medicineSnapshot.getValue(MedicineDataModel.class);
                        if (medicineDataModel != null) {
                            // Add the medicine to the list
                            medicinesList.add(medicineDataModel);
                        }
                    }
                }

                // Notify the callback with the complete list
                if (callback != null) {
                    callback.onMedicinesListReceived(medicinesList);
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

    // Callback interface to handle the received medicine list
    public interface AllMedicinesListCallback {
        void onMedicinesListReceived(List<MedicineDataModel> medicinesList);
    }
}
