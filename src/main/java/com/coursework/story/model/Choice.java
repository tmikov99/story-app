package com.coursework.story.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Choice {

    private String text;
    private Long targetPage;

    public Choice() {}

    public Choice(String text, Long targetPage) {
        this.text = text;
        this.targetPage = targetPage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTargetPage() {
        return targetPage;
    }

    public void setTargetPage(Long targetPage) {
        this.targetPage = targetPage;
    }
}