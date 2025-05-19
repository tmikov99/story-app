package com.coursework.story.controller;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.model.Page;
import com.coursework.story.service.PageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/page")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/{pageId}")
    public ResponseEntity<PageDTO> getPageById(@PathVariable Long pageId) {
        return ResponseEntity.ok(pageService.getPageById(pageId));
    }

    @GetMapping("/{storyId}/page/{pageNumber}")
    public ResponseEntity<PageDTO> getPageByStoryAndNumber(@PathVariable Long storyId, @PathVariable int pageNumber) {
        return ResponseEntity.ok(pageService.getPageByStoryAndNumber(storyId, pageNumber));
    }

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<PageDTO>> getPagesByStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(pageService.getPagesByStoryId(storyId));
    }

    @GetMapping("/story/{storyId}/map")
    public ResponseEntity<List<PageDTO>> getPagesMapByStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(pageService.getPagesMapByStoryId(storyId));
    }

    @PutMapping("/{pageId}")
    public ResponseEntity<PageDTO> updatePage(@PathVariable Long pageId, @RequestBody PageDTO newPage) {
        return ResponseEntity.ok(pageService.updatePage(pageId, newPage));
    }

    @PostMapping("/create")
    public ResponseEntity<PageDTO> createPage(@RequestBody PageDTO page) {
        PageDTO savedPage = pageService.savePage(page);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPage);
    }

    @DeleteMapping("/{pageId}")
    public ResponseEntity<Void> deletePage(@PathVariable Long pageId) {
        pageService.deletePage(pageId);
        return ResponseEntity.noContent().build();
    }
}
