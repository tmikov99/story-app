package com.coursework.story.exception;

import java.util.List;

public class StoryValidationException extends RuntimeException {
    private final List<String> errors;

    public StoryValidationException(List<String> errors) {
        super("Story validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}