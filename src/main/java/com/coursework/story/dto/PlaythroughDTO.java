package com.coursework.story.dto;

import com.coursework.story.model.Battle;
import com.coursework.story.model.Page;
import com.coursework.story.model.PlayerStats;
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
    private PageDTO page;
    private StoryDTO story;
    private Boolean completed;
    private Boolean active;
    private PlayerStats stats;
    private Boolean luckRequired;
    private Boolean luckPassed;
    private Boolean battlePending;
    private Battle battle;

    public PlaythroughDTO(Playthrough playthrough) {
        id = playthrough.getId();
        currentPage = playthrough.getCurrentPage().getPageNumber();
        path = playthrough.getPath();
        lastVisited = playthrough.getLastVisited();
        startedAt = playthrough.getStartedAt();
        completed = playthrough.isCompleted();
        active = playthrough.isActive();
        stats = playthrough.getStats();
        luckRequired = playthrough.isLuckRequired();
        luckPassed = playthrough.isLuckPassed();
        battlePending = playthrough.isBattlePending();
        battle = playthrough.getBattle();
        story = new StoryDTO();
        story.setId(playthrough.getStory().getId());
        story.setTitle(playthrough.getStory().getTitle());
        story.setPageCount(playthrough.getStory().getPageCount());
        story.setCoverImageUrl(playthrough.getStory().getCoverImageUrl());
    }

    public PlaythroughDTO(Playthrough playthrough, Page page) {
        id = playthrough.getId();
        currentPage = playthrough.getCurrentPage().getPageNumber();
        path = playthrough.getPath();
        lastVisited = playthrough.getLastVisited();
        startedAt = playthrough.getStartedAt();
        completed = playthrough.isCompleted();
        active = playthrough.isActive();
        stats = playthrough.getStats();
        luckRequired = playthrough.isLuckRequired();
        luckPassed = playthrough.isLuckPassed();
        battlePending = playthrough.isBattlePending();
        battle = playthrough.getBattle();
        story = new StoryDTO();
        story.setId(playthrough.getStory().getId());
        story.setTitle(playthrough.getStory().getTitle());
        story.setPageCount(playthrough.getStory().getPageCount());
        story.setCoverImageUrl(playthrough.getStory().getCoverImageUrl());
        this.page = new PageDTO(page);
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

    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public PageDTO getPage() {
        return page;
    }

    public void setPage(PageDTO page) {
        this.page = page;
    }

    public Boolean getLuckRequired() {
        return luckRequired;
    }

    public void setLuckRequired(Boolean luckRequired) {
        this.luckRequired = luckRequired;
    }

    public Boolean getLuckPassed() {
        return luckPassed;
    }

    public void setLuckPassed(Boolean luckPassed) {
        this.luckPassed = luckPassed;
    }

    public Boolean getBattlePending() {
        return battlePending;
    }

    public void setBattlePending(Boolean battlePending) {
        this.battlePending = battlePending;
    }

    public Battle getBattle() {
        return battle;
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
    }
}
