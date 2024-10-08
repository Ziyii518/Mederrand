package com.app.mederrand.apis.models.auth;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CountryCityResponseModel implements Parcelable {
    @SerializedName("address")
    private CountryCityAddressModel countryCityAddressModel;

    public CountryCityResponseModel() {
    }

    public CountryCityResponseModel(CountryCityAddressModel countryCityAddressModel) {
        this.countryCityAddressModel = countryCityAddressModel;
    }

    public CountryCityAddressModel getCountryCityAddressModel() {
        return countryCityAddressModel;
    }

    public void setCountryCityAddressModel(CountryCityAddressModel countryCityAddressModel) {
        this.countryCityAddressModel = countryCityAddressModel;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.countryCityAddressModel, flags);
    }

    public void readFromParcel(Parcel source) {
        this.countryCityAddressModel = source.readParcelable(CountryCityAddressModel.class.getClassLoader());
    }

    protected CountryCityResponseModel(Parcel in) {
        this.countryCityAddressModel = in.readParcelable(CountryCityAddressModel.class.getClassLoader());
    }

    public static final Creator<CountryCityResponseModel> CREATOR = new Creator<CountryCityResponseModel>() {
        @Override
        public CountryCityResponseModel createFromParcel(Parcel source) {
            return new CountryCityResponseModel(source);
        }

        @Override
        public CountryCityResponseModel[] newArray(int size) {
            return new CountryCityResponseModel[size];
        }
    };
}
