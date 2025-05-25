package com.coursework.story.controller;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PaginatedResponse;
import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.dto.StatCheckResult;
import com.coursework.story.service.PlaythroughService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/{playthroughId}/choice/{choiceId}")
    public ResponseEntity<PlaythroughDTO> resolveChoice(@PathVariable Long playthroughId, @PathVariable Long choiceId) {
        return ResponseEntity.ok(playthroughService.resolveChoice(playthroughId, choiceId));
    }

    @GetMapping("/{playthroughId}/testLuck")
    public ResponseEntity<StatCheckResult> testPlayerLuck(@PathVariable Long playthroughId) {
        return ResponseEntity.ok(playthroughService.testPlayerLuck(playthroughId));
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
    public PaginatedResponse<PlaythroughDTO> getUserPlaythroughs(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        Page<PlaythroughDTO> page = playthroughService.getPaginatedPlaythroughsForUser(q, pageable);
        return PaginatedResponse.fromPage(page);
    }

    @DeleteMapping("/{playthroughId}")
    public ResponseEntity<Void> deletePlaythrough(@PathVariable Long playthroughId) {
        playthroughService.deletePlaythrough(playthroughId);
        return ResponseEntity.noContent().build();
    }
}