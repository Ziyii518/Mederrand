package com.app.mederrand.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.models.local.CountriesModel;

import java.util.List;

public class CountryDialCodesAdapter extends RecyclerView.Adapter<CountryDialCodesAdapter.ViewHolder>{
    final Context context;
    final List<CountriesModel> countriesList;
    final CountryDialCodesAdapterCallBack callBack;

    public CountryDialCodesAdapter(Context context, List<CountriesModel> countriesList, CountryDialCodesAdapterCallBack callBack) {
        this.context = context;
        this.countriesList = countriesList;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_country_dial_code, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountriesModel countriesModel = countriesList.get(position);
        if (countriesModel != null) {
            holder.text_countryFlag.setText(countriesModel.flag());
            holder.text_countryName.setText(countriesModel.name());
            holder.text_countryCode.setText(countriesModel.dial_code());

            holder.itemView.setOnClickListener(v -> callBack.onCountryCodeSelectedListener(position));
        }
    }

    @Override
    public int getItemCount() {
        return countriesList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text_countryFlag;
        final TextView text_countryName;
        final TextView text_countryCode;
        public ViewHolder(@NonNull View view) {
            super(view);
            text_countryFlag = view.findViewById(R.id.text_countryFlag);
            text_countryName = view.findViewById(R.id.text_countryName);
            text_countryCode = view.findViewById(R.id.text_countryCode);
        }
    }

    public interface CountryDialCodesAdapterCallBack{
        void onCountryCodeSelectedListener(int position);
    }
}