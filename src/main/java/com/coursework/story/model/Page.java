package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.List;

@Entity(name = "pages")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    @JsonBackReference
    private Story story;

    @ElementCollection
    @CollectionTable(name = "page_paragraphs", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "paragraph", length = 2000)
    private List<String> paragraphs;

    @ElementCollection
    @CollectionTable(name = "page_choices", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "choice")
    private List<Choice> choices;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
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
}
