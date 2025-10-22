package com.example.appquanlytimtro.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;

    public RegisterResponse() {}

    public RegisterResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
