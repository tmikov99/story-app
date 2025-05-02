package com.coursework.story.controller;

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

    @PostMapping("/{storyId}/start")
    public ResponseEntity<PlaythroughDTO> startPlaythrough(@PathVariable Long storyId) {
        return ResponseEntity.ok(playthroughService.startPlaythrough(storyId));
    }

    @PutMapping("/{storyId}/choose/{nextPage}")
    public ResponseEntity<PlaythroughDTO> updatePlaythrough(@PathVariable Long storyId, @PathVariable int nextPage) {
        return ResponseEntity.ok(playthroughService.updatePlaythrough(storyId, nextPage));
    }

    @GetMapping("/{storyId}")
    public ResponseEntity<PlaythroughDTO> getPlaythrough(@PathVariable Long storyId) {
       return ResponseEntity.ok(playthroughService.getPlaythrough(storyId));
    }

    @GetMapping
    public ResponseEntity<List<PlaythroughDTO>> getUserPlaythroughs() {
        return ResponseEntity.ok(playthroughService.getAllPlaythroughsForUser());
    }
}