package com.coursework.story.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Choice {

    private String text;
    private Integer targetPage;

    public Choice() {}

    public Choice(String text, Integer targetPage) {
        this.text = text;
        this.targetPage = targetPage;
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
}