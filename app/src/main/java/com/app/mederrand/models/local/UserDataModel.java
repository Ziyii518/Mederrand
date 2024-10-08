package com.app.mederrand.models.local;

import android.os.Parcel;
import android.os.Parcelable;

public class UserDataModel implements Parcelable {
    String userId;
    String userType;
    String userVerified;
    String name;
    String username;
    String email;
    String country_code;
    String phone;
    String profileImage;
    String latitude;
    String longitude;
    String userAddress;
    String deviceToken;
    String deviceId;


    public UserDataModel() {}
    
    public UserDataModel(String userId, String userType, String userVerified, String name, String username, String email, String country_code, String phone, String profileImage, String latitude, String longitude, String userAddress, String deviceToken, String deviceId) {
        this.userId = userId;
        this.userType = userType;
        this.userVerified = userVerified;
        this.name = name;
        this.username = username;
        this.email = email;
        this.country_code = country_code;
        this.phone = phone;
        this.profileImage = profileImage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userAddress = userAddress;
        this.deviceToken = deviceToken;
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserVerified() {
        return userVerified;
    }

    public void setUserVerified(String userVerified) {
        this.userVerified = userVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.userType);
        dest.writeString(this.userVerified);
        dest.writeString(this.name);
        dest.writeString(this.username);
        dest.writeString(this.email);
        dest.writeString(this.country_code);
        dest.writeString(this.phone);
        dest.writeString(this.profileImage);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.userAddress);
        dest.writeString(this.deviceToken);
        dest.writeString(this.deviceId);
    }

    public void readFromParcel(Parcel source) {
        this.userId = source.readString();
        this.userType = source.readString();
        this.userVerified = source.readString();
        this.name = source.readString();
        this.username = source.readString();
        this.email = source.readString();
        this.country_code = source.readString();
        this.phone = source.readString();
        this.profileImage = source.readString();
        this.latitude = source.readString();
        this.longitude = source.readString();
        this.userAddress = source.readString();
        this.deviceToken = source.readString();
        this.deviceId = source.readString();
    }

    protected UserDataModel(Parcel in) {
        this.userId = in.readString();
        this.userType = in.readString();
        this.userVerified = in.readString();
        this.name = in.readString();
        this.username = in.readString();
        this.email = in.readString();
        this.country_code = in.readString();
        this.phone = in.readString();
        this.profileImage = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.userAddress = in.readString();
        this.deviceToken = in.readString();
        this.deviceId = in.readString();
    }

    public static final Parcelable.Creator<UserDataModel> CREATOR = new Parcelable.Creator<UserDataModel>() {
        @Override
        public UserDataModel createFromParcel(Parcel source) {
            return new UserDataModel(source);
        }

        @Override
        public UserDataModel[] newArray(int size) {
            return new UserDataModel[size];
        }
    };
}
