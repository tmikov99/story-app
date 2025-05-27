package com.coursework.story.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity(name = "choices")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private Integer targetPage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    @JsonBackReference
    private Page page;

    private boolean requiresLuckCheck;

    public Choice() {}

    public Choice(String text, Integer targetPage) {
        this.text = text;
        this.targetPage = targetPage;
    }

    public Choice(Choice choice) {
        this.text = choice.getText();
        this.targetPage = choice.getTargetPage();
        this.requiresLuckCheck = choice.getRequiresLuckCheck();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getTargetPage() {
        return targetPage;
    }

    public void setTargetPage(Integer targetPage) {
        this.targetPage = targetPage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Boolean getRequiresLuckCheck() {
        return requiresLuckCheck;
    }

    public void setRequiresLuckCheck(boolean requiresLuckCheck) {
        this.requiresLuckCheck = requiresLuckCheck;
    }
}