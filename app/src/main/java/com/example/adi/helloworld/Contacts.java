package com.example.adi.helloworld;

public class Contacts
{
    public String username, status, profileImage, userID;

    public Contacts()
    {
        this.username = "Unknown";
        this.status = "Unknown";
        this.profileImage = "Unknown";
        this.userID = "Unknown";
    }

    public Contacts(String username, String status, String profileImage, String userId)
    {
        this.username = username;
        this.status = status;
        this.profileImage = profileImage;
        this.userID = userId;
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
