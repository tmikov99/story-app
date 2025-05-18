package com.coursework.story.controller;

import com.coursework.story.dto.*;
import com.coursework.story.model.Genre;
import com.coursework.story.model.Story;
import com.coursework.story.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/trending")
    public PaginatedResponse<StoryDTO> getTrendingStories(Pageable pageable) {
        Page<StoryDTO> page = storyService.getTrendingStories(pageable);
        return PaginatedResponse.fromPage(page);
    }

    @GetMapping("/mine")
    public PaginatedResponse<StoryDTO> getUserStories(Pageable pageable) {
        Page<StoryDTO> page = storyService.getUserStories(pageable);
        return PaginatedResponse.fromPage(page);
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

    @GetMapping("/user/{username}/stories")
    public PaginatedResponse<StoryDTO> getUserStories(
            @PathVariable String username,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = switch (sort) {
            case "oldest" -> PageRequest.of(page, size, Sort.by("createdAt").ascending());
            case "most_read" -> PageRequest.of(page, size, Sort.by("reads").descending());
            default -> PageRequest.of(page, size, Sort.by("createdAt").descending());
        };

        return storyService.getPublishedStoriesByUser(username, pageable);
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

    @PatchMapping("/{storyId}/start-page")
    public ResponseEntity<Integer> updateStartPageNumber(
            @PathVariable Long storyId,
            @RequestBody UpdateStartPageDTO dto
    ) {
        int startPage = storyService.updateStartPageNumber(storyId, dto.getStartPageNumber());
        return ResponseEntity.ok(startPage);
    }

    @PostMapping("/copyAsDraft/{storyId}")
    public StoryDTO copyStoryAsDraft(@PathVariable Long storyId) {
        return storyService.copyStoryAsDraft(storyId);
    }

    @PutMapping("/publish/{storyId}")
    public ResponseEntity<StoryDTO> publishStory(@PathVariable Long storyId) {
        StoryDTO publishedStory = storyService.publishStory(storyId);
        return ResponseEntity.ok(publishedStory);
    }

    @PutMapping("/archive/{storyId}")
    public ResponseEntity<StoryDTO> archive(@PathVariable Long storyId) {
        StoryDTO archivedStory = storyService.archiveStory(storyId);
        return ResponseEntity.ok(archivedStory);
    }

    @GetMapping("/genres")
    public List<Genre> getGenres() {
        return List.of(Genre.values());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/like/{storyId}")
    public ResponseEntity<LikeResponse> likeStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.toggleLikeStory(storyId));
    }

    @PostMapping("/favorite/{storyId}")
    public ResponseEntity<Boolean> favoriteStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.toggleFavoriteStory(storyId));
    }
}
