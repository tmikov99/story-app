package com.coursework.story.dto;

import com.coursework.story.model.User;

public class UserDTO {
    private String username;

    public UserDTO(User user) {
        username = user.getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
