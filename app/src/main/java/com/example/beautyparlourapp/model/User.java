package com.example.beautyparlourapp.model;

public class User {
    public String uid;
    public String name;
    public String email;
    public String phone;
    public String avatarUrl;
    public String joinedDate;

    public User() {}

    public User(String uid, String name, String email, String phone, String avatarUrl, String joinedDate) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.joinedDate = joinedDate;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getJoinedDate() { return joinedDate; }
}
