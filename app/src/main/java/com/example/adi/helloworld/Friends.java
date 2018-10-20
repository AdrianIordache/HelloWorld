package com.example.adi.helloworld;

public class Friends
{
    public String username, status, profileImage, state, userID;

    public Friends()
    {
        this.username = "Unknown";
        this.status = "Unknown";
        this.profileImage = "Unknown";
        this.state = "Unknown";
        this.userID = "Unknown";

    }

    public Friends(String username, String status, String profileImage, String state ,String userId)
    {
        this.username = username;
        this.status = status;
        this.profileImage = profileImage;
        this.state = state;
        this.userID = userId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
