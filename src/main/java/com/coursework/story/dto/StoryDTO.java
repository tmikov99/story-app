package com.coursework.story.dto;

import com.coursework.story.model.Page;

import java.util.List;

public class StoryDTO {
    private Long id;
    private String title;
    private List<Page> pages;
    private Long userId;

    public StoryDTO(Long id, String title, List<Page> pages, Long userId) {
        this.id = id;
        this.title = title;
        this.pages = pages;
        this.userId = userId;
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

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}