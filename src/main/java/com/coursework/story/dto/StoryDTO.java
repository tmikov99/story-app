package com.coursework.story.dto;

import com.coursework.story.model.Genre;
import com.coursework.story.model.Page;
import com.coursework.story.model.Story;
import com.coursework.story.model.StoryStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
    private Integer startPage;
    private StoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StoryDTO(Story story) {
        id = story.getId();
        title = story.getTitle();
        user = new UserDTO(story.getUser());
        coverImageUrl = story.getCoverImageUrl();
        genres = story.getGenres();
        tags = story.getTags();
        description = story.getDescription();
        pageCount = story.getPageCount();
        startPage = story.getPages().getFirst().getPageNumber();
        status = story.getStatus();
        createdAt = story.getCreatedAt();
        updatedAt = story.getUpdatedAt();
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
        createdAt = story.getCreatedAt();
        updatedAt = story.getUpdatedAt();
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

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
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
}