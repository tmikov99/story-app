package com.coursework.story.dto;

import com.coursework.story.model.Choice;
import com.coursework.story.model.Page;

import java.util.List;
import java.util.Objects;

public class PageDTO {

    private Long id;

    private String title;

    private Long storyId;

    private Integer pageNumber;

    private List<String> paragraphs;

    private List<Choice> choices;

    private Double positionX;

    private Double positionY;

    private boolean isEndPage;

    private boolean luckRequired;


    public PageDTO() {
    }

    public PageDTO(Page page) {
        id = page.getId();
        title = page.getTitle();
        storyId = page.getStory().getId();
        pageNumber = page.getPageNumber();
        paragraphs = page.getParagraphs();
        choices = page.getChoices();
        positionX = page.getPositionX();
        positionY = page.getPositionY();
        isEndPage = page.isEndPage();
        luckRequired = page.isLuckRequired();
    }

    public PageDTO(Page page, boolean luckRequired) {
        id = page.getId();
        title = page.getTitle();
        storyId = page.getStory().getId();
        pageNumber = page.getPageNumber();
        paragraphs = page.getParagraphs();
        choices = page.getChoices();
        positionX = page.getPositionX();
        positionY = page.getPositionY();
        isEndPage = page.isEndPage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageDTO pageDTO = (PageDTO) o;
        return isEndPage == pageDTO.isEndPage && Objects.equals(id, pageDTO.id) && Objects.equals(title, pageDTO.title) && Objects.equals(storyId, pageDTO.storyId) && Objects.equals(pageNumber, pageDTO.pageNumber) && Objects.equals(paragraphs, pageDTO.paragraphs) && Objects.equals(choices, pageDTO.choices) && Objects.equals(positionX, pageDTO.positionX) && Objects.equals(positionY, pageDTO.positionY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, storyId, pageNumber, paragraphs, choices, positionX, positionY, isEndPage);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public boolean isEndPage() {
        return isEndPage;
    }

    public void setEndPage(boolean endPage) {
        isEndPage = endPage;
    }

    public boolean isLuckRequired() {
        return luckRequired;
    }

    public void setLuckRequired(boolean luckRequired) {
        this.luckRequired = luckRequired;
    }
}
