package com.app.mederrand.models.local;

public class ContactsModel {
    private final String userName, UserPhoneNumber;

    public ContactsModel(String userName, String userPhoneNumber) {
        this.userName = userName;
        UserPhoneNumber = userPhoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhoneNumber() {
        return UserPhoneNumber;
    }
}
