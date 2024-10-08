package com.app.mederrand.models.local;

import android.os.Parcel;
import android.os.Parcelable;

public class MedicineDayDataModel implements Parcelable {
    private final String name;
    private boolean isSelected;

    public MedicineDayDataModel(String name, boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
    }

    protected MedicineDayDataModel(Parcel in) {
        this.name = in.readString();
        this.isSelected = in.readByte() != 0;
    }

    public static final Parcelable.Creator<MedicineDayDataModel> CREATOR = new Parcelable.Creator<>() {
        @Override
        public MedicineDayDataModel createFromParcel(Parcel source) {
            return new MedicineDayDataModel(source);
        }

        @Override
        public MedicineDayDataModel[] newArray(int size) {
            return new MedicineDayDataModel[size];
        }
    };
}
