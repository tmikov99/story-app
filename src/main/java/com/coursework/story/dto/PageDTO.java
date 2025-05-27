package com.coursework.story.dto;

import com.coursework.story.model.Choice;
import com.coursework.story.model.Enemy;
import com.coursework.story.model.Page;
import com.coursework.story.model.StatModifiers;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<ItemDTO> itemsGranted;
    private Set<ItemDTO> itemsRemoved;
    private Enemy enemy;
    private StatModifiers statModifiers;


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
        itemsGranted = page.getItemsGranted().stream().map(ItemDTO::new).collect(Collectors.toSet());
        itemsRemoved = page.getItemsRemoved().stream().map(ItemDTO::new).collect(Collectors.toSet());
        enemy = page.getEnemy();
        statModifiers = page.getStatModifiers();
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
        itemsGranted = page.getItemsGranted().stream().map(ItemDTO::new).collect(Collectors.toSet());
        itemsRemoved = page.getItemsRemoved().stream().map(ItemDTO::new).collect(Collectors.toSet());
        enemy = page.getEnemy();
        statModifiers = page.getStatModifiers();
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

    public Set<ItemDTO> getItemsGranted() {
        return itemsGranted;
    }

    public void setItemsGranted(Set<ItemDTO> itemsGranted) {
        this.itemsGranted = itemsGranted;
    }

    public Set<ItemDTO> getItemsRemoved() {
        return itemsRemoved;
    }

    public void setItemsRemoved(Set<ItemDTO> itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
    }

    public StatModifiers getStatModifiers() {
        return statModifiers;
    }

    public void setStatModifiers(StatModifiers statModifiers) {
        this.statModifiers = statModifiers;
    }
}
