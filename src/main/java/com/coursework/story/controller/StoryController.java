package com.coursework.story.controller;

import com.coursework.story.model.Story;
import com.coursework.story.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "story")
public class StoryController {

    @Autowired
    StoryService storyService;

    @GetMapping("/")
    public List<Story> getStories() {
        return storyService.getStories();
    }
}
