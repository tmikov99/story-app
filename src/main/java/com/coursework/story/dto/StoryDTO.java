package com.coursework.story.dto;

import com.coursework.story.model.Genre;
import com.coursework.story.model.Page;
import com.coursework.story.model.Story;
import com.coursework.story.model.StoryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StoryDTO {
    private Long id;
    private String title;
    private List<PageDTO> pages;
    private UserDTO user;
    private String coverImageUrl;
    private List<Genre> genres;
    private Set<String> tags;
    private String description;
    private Integer pageCount;
    private Integer startPageNumber;
    private StoryStatus status;
    private Integer version;
    private Long originalStoryId;
    private Boolean isLiked;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likes;
    private Integer favorites;
    private Integer reads;

    public StoryDTO() {}

    public StoryDTO(Story story) {
        id = story.getId();
        title = story.getTitle();
        user = new UserDTO(story.getUser());
        coverImageUrl = story.getCoverImageUrl();
        genres = story.getGenres();
        tags = story.getTags();
        description = story.getDescription();
        pageCount = story.getPageCount();
        startPageNumber = story.getStartPageNumber();
        status = story.getStatus();
        version = story.getVersion();
        originalStoryId = story.getStatus() == StoryStatus.DRAFT && story.getOriginalStory() != null
                ? story.getOriginalStory().getId()
                : null;
        createdAt = story.getCreatedAt();
        updatedAt = story.getUpdatedAt();
        likes = story.getLikes();
        favorites = story.getFavorites();
        reads = story.getReads();
    }

    public StoryDTO(Story story, boolean isLiked, boolean isFavorite) {
        id = story.getId();
        title = story.getTitle();
        user = new UserDTO(story.getUser());
        coverImageUrl = story.getCoverImageUrl();
        genres = story.getGenres();
        tags = story.getTags();
        description = story.getDescription();
        pageCount = story.getPageCount();
        startPageNumber = story.getStartPageNumber();
        status = story.getStatus();
        version = story.getVersion();
        originalStoryId = story.getStatus() == StoryStatus.DRAFT && story.getOriginalStory() != null
                ? story.getOriginalStory().getId()
                : null;
        createdAt = story.getCreatedAt();
        updatedAt = story.getUpdatedAt();
        likes = story.getLikes();
        favorites = story.getFavorites();
        reads = story.getReads();
        this.isLiked = isLiked;
        this.isFavorite = isFavorite;
    }

    public StoryDTO(Story story, List<Page> pages) {
        id = story.getId();
        title = story.getTitle();
        user = new UserDTO(story.getUser());
        coverImageUrl = story.getCoverImageUrl();
        genres = story.getGenres();
        tags = story.getTags();
        description = story.getDescription();
        pageCount = story.getPageCount();
        status = story.getStatus();
        version = story.getVersion();
        originalStoryId = story.getStatus() == StoryStatus.DRAFT && story.getOriginalStory() != null
                ? story.getOriginalStory().getId()
                : null;
        createdAt = story.getCreatedAt();
        updatedAt = story.getUpdatedAt();
        likes = story.getLikes();
        favorites = story.getFavorites();
        reads = story.getReads();
        this.pages = pages.stream().map(PageDTO::new).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PageDTO> getPages() {
        return pages;
    }

    public void setPages(List<PageDTO> pages) {
        this.pages = pages;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getStartPageNumber() {
        return startPageNumber;
    }

    public void setStartPageNumber(Integer startPageNumber) {
        this.startPageNumber = startPageNumber;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
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

    public StoryStatus getStatus() {
        return status;
    }

    public void setStatus(StoryStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getOriginalStoryId() {
        return originalStoryId;
    }

    public void setOriginalStoryId(Long originalStoryId) {
        this.originalStoryId = originalStoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getLiked() {
        return isLiked;
    }

    public void setLiked(Boolean liked) {
        isLiked = liked;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    public Integer getReads() {
        return reads;
    }

    public void setReads(Integer reads) {
        this.reads = reads;
    }
}