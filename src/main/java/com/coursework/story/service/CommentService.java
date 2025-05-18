package com.coursework.story.service;

import com.coursework.story.dto.CommentDTO;
import com.coursework.story.model.Comment;
import com.coursework.story.model.NotificationType;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.CommentRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    public CommentService(CommentRepository commentRepository, StoryRepository storyRepository,
                          UserRepository userRepository, NotificationService notificationService,
                          AuthService authService) {
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @Transactional
    public CommentDTO addComment(Long storyId, String commentText) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        Comment comment = new Comment();

        comment.setStory(story);
        comment.setUser(user);
        comment.setText(commentText);

        if (!Objects.equals(story.getUser().getId(), comment.getUser().getId())) {
            notificationService.send(
                    story.getUser(),
                    "💬 Your story \"" + story.getTitle() + "\" received a new comment!",
                    NotificationType.NEW_COMMENT,
                    storyId
            );
        }

        return new CommentDTO(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    public Page<CommentDTO> getCommentsByStory(Long storyId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByStoryIdOrderByCreatedAt(storyId, pageable);
        return comments.map(CommentDTO::new);
    }

    public Page<CommentDTO> getUserComments(Pageable pageable) {
        User user = authService.getAuthenticatedUserOrThrow();
        Page<Comment> comments = commentRepository.findByUserId(user.getId(), pageable);
        return comments.map(comment -> new CommentDTO(comment, comment.getStory()));
    }

}