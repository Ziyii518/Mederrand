package com.app.mederrand.apis.models.inbox;

import com.app.mederrand.models.local.UserDataModel;

public class InboxDataModel {
    private String message;
    private String messageTime;
    private UserDataModel userDataModel;

    public InboxDataModel() {
    }

    public InboxDataModel(String message, String messageTime, UserDataModel userDataModel) {
        this.message = message;
        this.messageTime = messageTime;
        this.userDataModel = userDataModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public UserDataModel getUserDataModel() {
        return userDataModel;
    }

    public void setUserDataModel(UserDataModel userDataModel) {
        this.userDataModel = userDataModel;
    }
}
