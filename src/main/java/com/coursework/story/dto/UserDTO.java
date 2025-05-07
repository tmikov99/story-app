package com.coursework.story.dto;

import com.coursework.story.model.User;

public class UserDTO {
    private String username;
    private String email;
    private String imageUrl;
    private Integer storyCount;

    public UserDTO(User user) {
        username = user.getUsername();
        imageUrl = user.getImageUrl();
    }

    public UserDTO(String username, String email, String imageUrl) {
        this.username = username;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStoryCount() {
        return storyCount;
    }

    public void setStoryCount(Integer storyCount) {
        this.storyCount = storyCount;
    }
}
