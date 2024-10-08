package com.app.mederrand.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.app.mederrand.databinding.FragmentProfileBinding;
import com.app.mederrand.utils.Utilities;


public class ProfileFragment extends Fragment{
    FragmentProfileBinding binding;
    Context context;

    String userId, userFullName, userName, userAddress;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        init();
        return binding.getRoot();
    }

    private void init() {
        context = requireActivity();
        userId = Utilities.getString(context, "userId");
        userFullName = Utilities.getString(context, "userFullName");
        userName = Utilities.getString(context, "userName");
        userAddress = Utilities.getString(context, "userAddress");

        binding.textUserName.setText(userName);
        binding.textFullName.setText(userFullName);
        binding.textUserLocation.setText(userAddress);
    }

}