package com.coursework.story.dto;

public class LikeResponse {
    boolean result;
    int likes;

    public LikeResponse() {}

    public LikeResponse(boolean result, int likes) {
        this.result = result;
        this.likes = likes;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
