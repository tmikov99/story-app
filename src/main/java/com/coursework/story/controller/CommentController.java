package com.coursework.story.controller;

import com.coursework.story.dto.CommentDTO;
import com.coursework.story.dto.PaginatedResponse;
import com.coursework.story.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/story/{storyId}")
    public ResponseEntity<PaginatedResponse<CommentDTO>> getComments(
            @PathVariable Long storyId,
            Pageable pageable) {
        Page<CommentDTO> page = commentService.getCommentsByStory(storyId, pageable);
        return ResponseEntity.ok(PaginatedResponse.fromPage(page));
    }

    @GetMapping("/mine")
    public ResponseEntity<PaginatedResponse<CommentDTO>> getUserComments(Pageable pageable) {
        Page<CommentDTO> page = commentService.getUserComments(pageable);
        return ResponseEntity.ok(PaginatedResponse.fromPage(page));
    }
}