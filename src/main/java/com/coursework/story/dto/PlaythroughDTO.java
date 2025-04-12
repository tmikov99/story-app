package com.coursework.story.dto;

import com.coursework.story.model.Playthrough;

import java.time.LocalDateTime;
import java.util.List;

public class PlaythroughDTO {

    private Long id;
    private Long storyId;
    private Long currentPageId;
    private List<Long> path;
    private LocalDateTime lastVisited;

    public PlaythroughDTO(Playthrough playthrough) {
        id = playthrough.getId();
        storyId = playthrough.getStory().getId();
        currentPageId = playthrough.getCurrentPage().getId();
        path = playthrough.getPath();
        lastVisited = playthrough.getLastVisited();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public Long getCurrentPageId() {
        return currentPageId;
    }

    public void setCurrentPageId(Long currentPageId) {
        this.currentPageId = currentPageId;
    }

    public List<Long> getPath() {
        return path;
    }

    public void setPath(List<Long> path) {
        this.path = path;
    }

    public LocalDateTime getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(LocalDateTime lastVisited) {
        this.lastVisited = lastVisited;
    }
}
