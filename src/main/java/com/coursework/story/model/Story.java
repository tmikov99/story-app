package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Page> pages = new ArrayList<>();

    private String coverImageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<Genre> genres = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "story_tags", joinColumns = @JoinColumn(name = "story_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(length = 1000)
    private String description;

    private Integer pageCount;

    private int startPageNumber;

    @Enumerated(EnumType.STRING)
    private StoryStatus status = StoryStatus.DRAFT;

    private int version = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_story_id")
    private Story originalStory;

    @OneToMany(mappedBy = "originalStory")
    private List<Story> drafts = new ArrayList<>();

    @ManyToMany(mappedBy = "likedStories")
    private Set<User> likedByUsers;

    @ManyToMany(mappedBy = "favoriteStories")
    private Set<User> favoriteByUsers;

    private int likes;

    private int favorites;

    private int reads;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getFirstAvailablePageNumber() {
        Set<Integer> usedPageNumbers = new HashSet<>();
        for (Page page : pages) {
            usedPageNumbers.add(page.getPageNumber());
        }

        int pageNumber = 1;
        while (usedPageNumbers.contains(pageNumber)) {
            pageNumber++;
        }

        return pageNumber;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        this.likes = Math.max(0, this.likes - 1);
    }

    public void incrementFavorites() {
        this.favorites++;
    }

    public void decrementFavorites() {
        this.favorites = Math.max(0, this.favorites - 1);
    }

    public void incrementReads() {
        this.reads++;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
        for (Page page : pages) {
            page.setStory(this);  // Ensure the bidirectional relationship is maintained
        }
        setPageCount();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPageCount() {
        return pages.size();
    }

    public void setPageCount() {
        this.pageCount = pages.size();
    }

    public Integer getStartPageNumber() {
        return startPageNumber;
    }

    public void setStartPageNumber(int startPageNumber) {
        this.startPageNumber = startPageNumber;
    }

    public StoryStatus getStatus() {
        return status;
    }

    public void setStatus(StoryStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Story getOriginalStory() {
        return originalStory;
    }

    public void setOriginalStory(Story originalStory) {
        this.originalStory = originalStory;
    }

    public List<Story> getDrafts() {
        return drafts;
    }

    public void setDrafts(List<Story> drafts) {
        this.drafts = drafts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getReads() {
        return reads;
    }

    public void setReads(int reads) {
        this.reads = reads;
    }

    public Set<User> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(Set<User> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    public Set<User> getFavoriteByUsers() {
        return favoriteByUsers;
    }

    public void setFavoriteByUsers(Set<User> favoriteByUsers) {
        this.favoriteByUsers = favoriteByUsers;
    }
}
