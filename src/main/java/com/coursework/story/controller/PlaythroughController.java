package com.coursework.story.controller;

import com.coursework.story.model.Playthrough;
import com.coursework.story.service.PlaythroughService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/playthrough")
public class PlaythroughController {
    private final PlaythroughService playthroughService;

    public PlaythroughController(PlaythroughService playthroughService) {
        this.playthroughService = playthroughService;
    }

    @PostMapping("/{storyId}/start")
    public ResponseEntity<Playthrough> startPlaythrough(@PathVariable Long storyId) {
        return ResponseEntity.ok(playthroughService.startPlaythrough(storyId));
    }

    @PutMapping("/{storyId}/choose/{nextPageId}")
    public ResponseEntity<Playthrough> updatePlaythrough(@PathVariable Long storyId, @PathVariable Long nextPageId) {
        return ResponseEntity.ok(playthroughService.updatePlaythrough(storyId, nextPageId));
    }

    @GetMapping("/{storyId}")
    public ResponseEntity<Playthrough> getPlaythrough(@PathVariable Long storyId) {
        Optional<Playthrough> playthrough = playthroughService.getPlaythrough(storyId);
        return playthrough.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}