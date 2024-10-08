package com.app.mederrand.apis.models.medicines;

import android.os.Parcel;
import android.os.Parcelable;

public class MedicineDataModel implements Parcelable {
    String id, userId, userFullName, name, price, stock, image;

    public MedicineDataModel() {
    }

    public MedicineDataModel(String id, String userId, String userFullName, String name, String price, String stock, String image) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.userId);
        dest.writeString(this.userFullName);
        dest.writeString(this.name);
        dest.writeString(this.price);
        dest.writeString(this.stock);
        dest.writeString(this.image);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.userId = source.readString();
        this.userFullName = source.readString();
        this.name = source.readString();
        this.price = source.readString();
        this.stock = source.readString();
        this.image = source.readString();
    }

    protected MedicineDataModel(Parcel in) {
        this.id = in.readString();
        this.userId = in.readString();
        this.userFullName = in.readString();
        this.name = in.readString();
        this.price = in.readString();
        this.stock = in.readString();
        this.image = in.readString();
    }

    public static final Creator<MedicineDataModel> CREATOR = new Creator<MedicineDataModel>() {
        @Override
        public MedicineDataModel createFromParcel(Parcel source) {
            return new MedicineDataModel(source);
        }

        @Override
        public MedicineDataModel[] newArray(int size) {
            return new MedicineDataModel[size];
        }
    };
}
