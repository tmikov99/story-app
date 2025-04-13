package com.coursework.story.dto;

import com.coursework.story.model.Playthrough;

import java.time.LocalDateTime;
import java.util.List;

public class PlaythroughDTO {

    private Long id;
    private Long storyId;
    private Integer currentPage;
    private List<Integer> path;
    private LocalDateTime lastVisited;

    public PlaythroughDTO(Playthrough playthrough) {
        id = playthrough.getId();
        storyId = playthrough.getStory().getId();
        currentPage = playthrough.getCurrentPage().getPageNumber();
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

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    public LocalDateTime getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(LocalDateTime lastVisited) {
        this.lastVisited = lastVisited;
    }
}
