package com.coursework.story.controller;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.model.Page;
import com.coursework.story.service.PageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "page")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping("/{pageId}")
    public ResponseEntity<Page> getPageById(@PathVariable Long pageId) {
        return ResponseEntity.ok(pageService.getPageById(pageId));
    }

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<Page>> getPagesByStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(pageService.getPagesByStoryId(storyId));
    }

    @PutMapping("/{pageId}")
    public ResponseEntity<Page> updatePage(@PathVariable Long pageId, @RequestBody PageDTO newPage) {
        return ResponseEntity.ok(pageService.updatePage(pageId, newPage));
    }

    @DeleteMapping("/{pageId}")
    public ResponseEntity<Void> deletePage(@PathVariable Long pageId) {
        pageService.deletePage(pageId);
        return ResponseEntity.noContent().build();
    }
}
