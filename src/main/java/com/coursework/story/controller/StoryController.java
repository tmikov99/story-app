package com.coursework.story.controller;

import com.coursework.story.dto.LikeResponse;
import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PaginatedResponse;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.Genre;
import com.coursework.story.model.Story;
import com.coursework.story.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/story")
public class StoryController {

    @Autowired
    StoryService storyService;

    @GetMapping
    public PaginatedResponse<StoryDTO> searchStories(
            @RequestParam(value = "q", required = false) String query,
            Pageable pageable) {
        Page<StoryDTO> result = (query != null && !query.isBlank())
                ? storyService.searchStories(query, pageable)
                : storyService.getAllStories(pageable);

        return PaginatedResponse.fromPage(result);
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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoryDTO saveStory(@RequestPart("story") Story story,
                              @RequestPart(value = "file", required = false) MultipartFile coverImage) {
        return storyService.saveStory(story, coverImage);
    }

    @PutMapping("/update/{storyId}")
    public StoryDTO updateStory(@PathVariable Long storyId,
                                @RequestPart("story") StoryDTO story,
                                @RequestPart(value = "file", required = false) MultipartFile coverImage) {
        return storyService.updateStory(storyId, story, coverImage);
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
    public PaginatedResponse<StoryDTO> getLikedStories(Pageable pageable) {
        Page<StoryDTO> page = storyService.getLikedStories(pageable);
        return PaginatedResponse.fromPage(page);
    }

    @GetMapping("/favorite")
    public PaginatedResponse<StoryDTO> getFavoriteStories(Pageable pageable) {
        Page<StoryDTO> page = storyService.getFavoriteStories(pageable);
        return PaginatedResponse.fromPage(page);
    }
}
