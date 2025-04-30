package com.coursework.story.dto;

import com.coursework.story.model.User;

public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String imageUrl;

    public AuthResponse(String token) {
        this.token = token;
    }

    public AuthResponse(String token, User user) {
        this.token = token;
        this.username = user.getUsername();
        this.email = user.getEmail();
        if (user.getImageUrl() != null) {
            this.imageUrl = user.getImageUrl();
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}