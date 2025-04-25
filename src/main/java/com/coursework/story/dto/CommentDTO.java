package com.coursework.story.dto;

import com.coursework.story.model.Comment;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String username;
    private String text;
    private LocalDateTime createdAt;

    public CommentDTO () {}

    public CommentDTO (Comment comment) {
        this.id = comment.getId();
        this.username = comment.getUser().getUsername();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
