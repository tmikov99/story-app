package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "pages")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    @JsonBackReference
    private Story story;

    @Column(nullable = false)
    private int pageNumber;

    @ElementCollection
    @CollectionTable(name = "page_paragraphs", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "paragraph", length = 2000)
    private List<String> paragraphs;

    @ElementCollection
    @CollectionTable(name = "page_choices", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "choice")
    private List<Choice> choices;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

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

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices == null ? new ArrayList<>() : choices;
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

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public boolean isEndPage() {
        return choices == null || choices.isEmpty();
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
}
