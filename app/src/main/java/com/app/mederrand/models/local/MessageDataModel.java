package com.app.mederrand.models.local;

public class MessageDataModel {
    String message;
    String time;
    String senderId;
    String receiverId;
    MessageUserModel user;

    public MessageDataModel() {
    }

    public MessageDataModel(String message, String time, String senderId, String receiverId, MessageUserModel user) {
        this.message = message;
        this.time = time;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public MessageUserModel getUser() {
        return user;
    }

    public void setUser(MessageUserModel user) {
        this.user = user;
    }

}
