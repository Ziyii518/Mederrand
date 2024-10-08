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
import com.app.mederrand.activities.medicine.MedicineDetailActivity;
import com.app.mederrand.adapters.MedicineAdapter;
import com.app.mederrand.apis.models.medicines.MedicineDataModel;
import com.app.mederrand.databinding.FragmentHomeBinding;
import com.app.mederrand.utils.services.AllMedicineRetrievalService;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements AllMedicineRetrievalService.AllMedicinesListCallback, MedicineAdapter.MedicineAdapterCallBack {
    FragmentHomeBinding binding;
    Context context;

    AllMedicineRetrievalService allMedicineRetrievalService;
    List<MedicineDataModel> medicinesList = new ArrayList<>();
    MedicineAdapter medicineAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater);
        init();

        return binding.getRoot();
    }

    private void init() {
        context = requireActivity();

        if (medicinesList.isEmpty()){
            binding.mainLayout.setVisibility(View.GONE);
            binding.loading.setVisibility(View.VISIBLE);
            allMedicineRetrievalService = new AllMedicineRetrievalService();
            allMedicineRetrievalService.retrieveMedicines(this);
        }else {
            showMedicinesRecyclerview(binding.medicinesRecyclerview, medicinesList);
        }
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
            binding.layoutEmptyScreen.textEmptyScreenDescription.setText(R.string.text_no_medicines_added_by_pharmacies_yet_description);
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