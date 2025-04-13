package com.coursework.story.dto;

import com.coursework.story.model.Choice;
import com.coursework.story.model.Page;

import java.util.List;

public class PageDTO {

    private Long id;

    private Long storyId;

    private Integer pageNumber;

    private List<String> paragraphs;

    private List<Choice> choices;

    public PageDTO() {
    }

    public PageDTO(Page page) {
        id = page.getId();
        storyId = page.getStory().getId();
        pageNumber = page.getPageNumber();
        paragraphs = page.getParagraphs();
        choices = page.getChoices();
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(List<String> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public Long getStoryId() {
        return storyId;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }
}
