package com.app.mederrand.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.activities.medicine.AddMedicineActivity;
import com.app.mederrand.activities.medicine.MedicineDetailActivity;
import com.app.mederrand.adapters.MedicineAdapter;
import com.app.mederrand.apis.models.medicines.MedicineDataModel;
import com.app.mederrand.databinding.FragmentManageMedicinesBinding;
import com.app.mederrand.utils.Utilities;
import com.app.mederrand.utils.services.MedicineRetrievalService;

import java.util.ArrayList;
import java.util.List;


public class ManageMedicinesFragment extends Fragment implements MedicineRetrievalService.OnMedicinesListCallback, MedicineAdapter.MedicineAdapterCallBack{

    Context context;
    FragmentManageMedicinesBinding binding;

    MedicineRetrievalService medicineRetrievalService;
    List<MedicineDataModel> medicinesList = new ArrayList<>();
    MedicineAdapter medicineAdapter;

    String userId, userType;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentManageMedicinesBinding.inflate(inflater);
        init();
        return binding.getRoot();
    }

    private void init(){
        context = requireActivity();
        userId = Utilities.getString(context, "userId");
        userType = Utilities.getString(context, "userType");


        // MANAGE THE Medicines OWNER DATA HERE

        if (medicinesList.isEmpty()){
            binding.mainLayout.setVisibility(View.GONE);
            binding.loading.setVisibility(View.VISIBLE);
            medicineRetrievalService = new MedicineRetrievalService(userId);
            medicineRetrievalService.retrieveMedicines(this);
        }else {
            showMedicinesRecyclerview(binding.medicinesRecyclerview, medicinesList);
        }

        initClickListeners();
    }

    private void initClickListeners(){
        binding.btnAdd.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddMedicineActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        });
    }

    @Override
    public void onMedicinesListReceived(List<MedicineDataModel> list) {
        binding.mainLayout.setVisibility(View.VISIBLE);
        binding.loading.setVisibility(View.GONE);
        medicinesList = list;

        showMedicinesRecyclerview(binding.medicinesRecyclerview, medicinesList);
    }

    private void showMedicinesRecyclerview(RecyclerView recyclerView, List<MedicineDataModel> medicinesList){
        binding.mainLayout.setVisibility(View.VISIBLE);
        binding.loading.setVisibility(View.GONE);

        if (medicinesList.isEmpty()){
            binding.layoutEmptyScreen.getRoot().setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            binding.layoutEmptyScreen.getRoot().setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            medicineAdapter = new MedicineAdapter(context, medicinesList, this);
            recyclerView.setAdapter(medicineAdapter);
        }
    }


    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(context, MedicineDetailActivity.class);
        intent.putExtra("medicineDetail", medicinesList.get(position));
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }


}