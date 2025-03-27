package com.coursework.story.controller;

import com.coursework.story.model.Story;
import com.coursework.story.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "story")
public class StoryController {

    @Autowired
    StoryService storyService;

    @GetMapping
    public List<Story> getStories() {
        return storyService.getStories();
    }

    @PostMapping("/create")
    public Story saveStory(@RequestBody Story story) {
        return storyService.saveStory(story);
    }
}
