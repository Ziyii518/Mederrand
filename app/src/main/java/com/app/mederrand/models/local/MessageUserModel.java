package com.app.mederrand.models.local;

public class MessageUserModel {
    String userId;
    String name;
    String username;
    String email;
    String profileImage;

    public MessageUserModel() {
    }

    public MessageUserModel(String userId, String name, String username, String email, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
