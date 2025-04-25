package com.coursework.story.controller;

import com.coursework.story.dto.CommentDTO;
import com.coursework.story.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/story/{storyId}")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long storyId, @RequestBody String comment) {
        return ResponseEntity.ok(commentService.addComment(storyId, comment));
    }

    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long storyId) {
        return ResponseEntity.ok(commentService.getCommentsByStory(storyId));
    }
}