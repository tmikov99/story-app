package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "playthrough_items",
            joinColumns = @JoinColumn(name = "playthrough_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<Item> inventory = new HashSet<>();

    @OneToOne(mappedBy = "playthrough", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Battle battle;
    private boolean battlePending;

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

    public Set<Item> getInventory() {
        return inventory;
    }

    public void setInventory(Set<Item> inventory) {
        this.inventory = inventory;
    }

    public Battle getBattle() {
        return battle;
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
    }

    public boolean isBattlePending() {
        return battlePending;
    }

    public void setBattlePending(boolean battlePending) {
        this.battlePending = battlePending;
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