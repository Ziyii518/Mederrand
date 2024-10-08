package com.app.mederrand.apis.models.auth;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CountryCityAddressModel implements Parcelable {
    @SerializedName("county")
    private String county;
    @SerializedName("city")
    private String city;
    @SerializedName("country")
    private String country;

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getCounty() {
        return county;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.county);
        dest.writeString(this.city);
        dest.writeString(this.country);
    }

    public void readFromParcel(Parcel source) {
        this.county = source.readString();
        this.city = source.readString();
        this.country = source.readString();
    }

    public CountryCityAddressModel() {
    }

    protected CountryCityAddressModel(Parcel in) {
        this.county = in.readString();
        this.city = in.readString();
        this.country = in.readString();
    }

    public static final Creator<CountryCityAddressModel> CREATOR = new Creator<CountryCityAddressModel>() {
        @Override
        public CountryCityAddressModel createFromParcel(Parcel source) {
            return new CountryCityAddressModel(source);
        }

        @Override
        public CountryCityAddressModel[] newArray(int size) {
            return new CountryCityAddressModel[size];
        }
    };
}
