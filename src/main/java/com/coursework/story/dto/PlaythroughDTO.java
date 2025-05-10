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
    private LocalDateTime startedAt;
    private StoryDTO story;
    private Boolean completed;
    private Boolean active;

    public PlaythroughDTO(Playthrough playthrough) {
        id = playthrough.getId();
        currentPage = playthrough.getCurrentPage().getPageNumber();
        path = playthrough.getPath();
        lastVisited = playthrough.getLastVisited();
        startedAt = playthrough.getStartedAt();
        completed = playthrough.isCompleted();
        active = playthrough.isActive();
        story = new StoryDTO();
        story.setId(playthrough.getStory().getId());
        story.setTitle(playthrough.getStory().getTitle());
        story.setPageCount(playthrough.getStory().getPageCount());
        story.setCoverImageUrl(playthrough.getStory().getCoverImageUrl());
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public StoryDTO getStory() {
        return story;
    }

    public void setStory(StoryDTO story) {
        this.story = story;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
