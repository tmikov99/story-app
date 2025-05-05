package com.coursework.story.dto;

import com.coursework.story.model.Comment;
import com.coursework.story.model.Story;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String username;
    private String imageUrl;
    private String text;
    private LocalDateTime createdAt;
    private StoryDTO story;

    public CommentDTO () {}

    public CommentDTO (Comment comment) {
        this.id = comment.getId();
        this.username = comment.getUser().getUsername();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        if (comment.getUser().getImageUrl() != null) {
            this.imageUrl = comment.getUser().getImageUrl();
        }
    }

    public CommentDTO (Comment comment, Story story) {
        this.id = comment.getId();
        this.username = comment.getUser().getUsername();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        if (comment.getUser().getImageUrl() != null) {
            this.imageUrl = comment.getUser().getImageUrl();
        }
        this.story = new StoryDTO();
        this.story.setId(story.getId());
        this.story.setTitle(story.getTitle());
        this.story.setCoverImageUrl(story.getCoverImageUrl());
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public StoryDTO getStory() {
        return story;
    }

    public void setStory(StoryDTO story) {
        this.story = story;
    }
}
