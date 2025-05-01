package com.coursework.story.controller;

import com.coursework.story.dto.LikeResponse;
import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.Genre;
import com.coursework.story.model.Story;
import com.coursework.story.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/user/{userId}")
    public List<StoryDTO> getStoriesByUser(@PathVariable String username) {
        return storyService.getStoriesByUser(username);
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

    @PostMapping("/like/{storyId}")
    public ResponseEntity<LikeResponse> likeStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.toggleLikeStory(storyId));
    }

    @PostMapping("/favorite/{storyId}")
    public ResponseEntity<Boolean> favoriteStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.toggleFavoriteStory(storyId));
    }

    @GetMapping("/liked")
    public List<StoryDTO> getLikedStories() {
        return storyService.getLikedStories();
    }

    @GetMapping("/favorite")
    public List<StoryDTO> getFavoriteStories() {
        return storyService.getFavoriteStories();
    }
}
