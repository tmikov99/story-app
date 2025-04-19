package com.coursework.story.controller;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.Genre;
import com.coursework.story.model.Story;
import com.coursework.story.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/story")
public class StoryController {

    @Autowired
    StoryService storyService;

    @GetMapping
    public List<StoryDTO> getStories() {
        return storyService.getStories();
    }

    @GetMapping("/{storyId}")
    public StoryDTO getStoryById(@PathVariable Long storyId) {
        return storyService.getStoryById(storyId);
    }

    @GetMapping("/preview/{storyId}")
    public StoryDTO getStoryPreviewById(@PathVariable Long storyId) {
        return storyService.getStoryPreviewById(storyId);
    }

    @PostMapping("/create")
    public StoryDTO saveStory(@RequestBody Story story) {
        return storyService.saveStory(story);
    }

    @GetMapping("/genres")
    public List<Genre> getGenres() {
        return List.of(Genre.values());
    }

    @PutMapping("/pages/{storyId}")
    public void updatePagesBysStoryId(@PathVariable Long storyId, @RequestBody List<PageDTO> pages) {
        storyService.updatePages(storyId, pages);
    }
}
