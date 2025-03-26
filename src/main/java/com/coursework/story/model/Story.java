package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Page> pages;

    public String getTitle() {
        return title;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
        for (Page page : pages) {
            page.setStory(this);  // Ensure the bidirectional relationship is maintained
        }
    }
}
