package com.app.mederrand.apis.models.medicines;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderDataModel implements Parcelable {
    String id, prescriptionImage, medicineId, medicineName, medicineImage, buyerId, pharmacyId, pharmacyName, driverId, orderStatus, orderQuantity, orderTotalPrice;

    public OrderDataModel() {
    }

    public OrderDataModel(String id, String prescriptionImage, String medicineId, String medicineName, String medicineImage, String buyerId, String pharmacyId, String pharmacyName, String driverId, String orderStatus, String orderQuantity, String orderTotalPrice) {
        this.id = id;
        this.prescriptionImage = prescriptionImage;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.medicineImage = medicineImage;
        this.buyerId = buyerId;
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.driverId = driverId;
        this.orderStatus = orderStatus;
        this.orderQuantity = orderQuantity;
        this.orderTotalPrice = orderTotalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrescriptionImage() {
        return prescriptionImage;
    }

    public void setPrescriptionImage(String prescriptionImage) {
        this.prescriptionImage = prescriptionImage;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineImage() {
        return medicineImage;
    }

    public void setMedicineImage(String medicineImage) {
        this.medicineImage = medicineImage;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(String orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getOrderTotalPrice() {
        return orderTotalPrice;
    }

    public void setOrderTotalPrice(String orderTotalPrice) {
        this.orderTotalPrice = orderTotalPrice;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.prescriptionImage);
        dest.writeString(this.medicineId);
        dest.writeString(this.medicineName);
        dest.writeString(this.medicineImage);
        dest.writeString(this.buyerId);
        dest.writeString(this.pharmacyId);
        dest.writeString(this.pharmacyName);
        dest.writeString(this.driverId);
        dest.writeString(this.orderStatus);
        dest.writeString(this.orderQuantity);
        dest.writeString(this.orderTotalPrice);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.prescriptionImage = source.readString();
        this.medicineId = source.readString();
        this.medicineName = source.readString();
        this.medicineImage = source.readString();
        this.buyerId = source.readString();
        this.pharmacyId = source.readString();
        this.pharmacyName = source.readString();
        this.driverId = source.readString();
        this.orderStatus = source.readString();
        this.orderQuantity = source.readString();
        this.orderTotalPrice = source.readString();
    }

    protected OrderDataModel(Parcel in) {
        this.id = in.readString();
        this.prescriptionImage = in.readString();
        this.medicineId = in.readString();
        this.medicineName = in.readString();
        this.medicineImage = in.readString();
        this.buyerId = in.readString();
        this.pharmacyId = in.readString();
        this.pharmacyName = in.readString();
        this.driverId = in.readString();
        this.orderStatus = in.readString();
        this.orderQuantity = in.readString();
        this.orderTotalPrice = in.readString();
    }

    public static final Parcelable.Creator<OrderDataModel> CREATOR = new Parcelable.Creator<OrderDataModel>() {
        @Override
        public OrderDataModel createFromParcel(Parcel source) {
            return new OrderDataModel(source);
        }

        @Override
        public OrderDataModel[] newArray(int size) {
            return new OrderDataModel[size];
        }
    };
}
