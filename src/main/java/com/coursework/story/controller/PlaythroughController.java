package com.coursework.story.controller;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.service.PlaythroughService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playthrough")
public class PlaythroughController {
    private final PlaythroughService playthroughService;

    public PlaythroughController(PlaythroughService playthroughService) {
        this.playthroughService = playthroughService;
    }

    @PostMapping("/start/{storyId}")
    public ResponseEntity<PlaythroughDTO> startPlaythrough(@PathVariable Long storyId) {
        return ResponseEntity.ok(playthroughService.startPlaythrough(storyId));
    }

    @PatchMapping("/{playthroughId}/choose/{nextPage}")
    public ResponseEntity<PageDTO> updatePlaythrough(@PathVariable Long playthroughId, @PathVariable int nextPage) {
        return ResponseEntity.ok(playthroughService.choosePage(playthroughId, nextPage));
    }

    @GetMapping("/{playthroughId}")
    public ResponseEntity<PlaythroughDTO> getPlaythrough(@PathVariable Long playthroughId) {
        return ResponseEntity.ok(playthroughService.getPlaythroughById(playthroughId));
    }

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<PlaythroughDTO>> getUserPlaythroughsForStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(playthroughService.getPlaythroughsForUserAndStory(storyId));
    }

    @GetMapping("/{playthroughId}/currentPage")
    public ResponseEntity<PageDTO> getCurrentPage(@PathVariable Long playthroughId) {
        return ResponseEntity.ok(playthroughService.getCurrentPageForPlaythrough(playthroughId));
    }

    @PostMapping("/{playthroughId}/load")
    public ResponseEntity<Void> loadPlaythrough(@PathVariable Long playthroughId) {
        playthroughService.loadPlaythrough(playthroughId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PlaythroughDTO>> getUserPlaythroughs() {
        return ResponseEntity.ok(playthroughService.getAllPlaythroughsForUser());
    }
}