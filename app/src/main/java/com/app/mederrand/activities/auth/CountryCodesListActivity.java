package com.app.mederrand.activities.auth;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.app.mederrand.R;
import com.app.mederrand.adapters.CountryDialCodesAdapter;
import com.app.mederrand.databinding.ActivityCountryCodesListBinding;
import com.app.mederrand.models.local.CountriesModel;
import com.app.mederrand.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class CountryCodesListActivity extends AppCompatActivity implements CountryDialCodesAdapter.CountryDialCodesAdapterCallBack {
    ActivityCountryCodesListBinding binding;
    Context context;

    CountryDialCodesAdapter countryDialCodesAdapter;
    List<CountriesModel> countriesList;
    OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCountryCodesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(CountryCodesListActivity.this);
        context = CountryCodesListActivity.this;

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        countriesList = new ArrayList<>();
        countriesList = Utilities.getCountriesList(context);
        showAllCountryCodesRecyclerView(binding.recyclerViewCountries, countriesList);

        binding.btnCross.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void showAllCountryCodesRecyclerView(RecyclerView recyclerView, List<CountriesModel> countriesList) {
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context , LinearLayoutManager.VERTICAL , false);
        recyclerView.setLayoutManager(linearLayoutManager);
        countryDialCodesAdapter = new CountryDialCodesAdapter(context, countriesList, this);
        recyclerView.setAdapter(countryDialCodesAdapter);
    }

    @Override
    public void onCountryCodeSelectedListener(int position) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra("selectedCountryName", countriesList.get(position).name());
        finish();
        overridePendingTransition(R.anim.slide_out_up, R.anim.slide_bottom);
    }

    private void initOnBackPressed(){
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_out_up, R.anim.slide_bottom);
            }
        };
    }

}