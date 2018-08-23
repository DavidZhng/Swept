package com.dz.swept.models;

public class User {

    private String user_id;
    private String email;
    private String profile_img_url;
    private String username;

    public User(String user_id, String email, String profile_img_url, String username) {
        this.user_id = user_id;
        this.email = email;
        this.profile_img_url = profile_img_url;
        this.username = username;
    }
    public User() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_img_url() {
        return profile_img_url;
    }

    public void setProfile_img_url(String profile_img_url) {
        this.profile_img_url = profile_img_url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", profile_img_url='" + profile_img_url + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
