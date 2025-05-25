package com.coursework.story.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Playthrough {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne
    @JoinColumn(name = "current_page_id")
    private Page currentPage;

    @ElementCollection
    private List<Integer> path;

    private LocalDateTime lastVisited;

    private LocalDateTime startedAt;

    private boolean completed;
    private boolean active;
    private boolean luckRequired;
    private boolean luckPassed;

    @Embedded
    private PlayerStats stats;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Page currentPage) {
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public boolean isLuckRequired() {
        return luckRequired;
    }

    public void setLuckRequired(boolean luckRequired) {
        this.luckRequired = luckRequired;
    }

    public boolean isLuckPassed() {
        return luckPassed;
    }

    public void setLuckPassed(boolean luckPassed) {
        this.luckPassed = luckPassed;
    }
}