package com.coursework.story.service;

import com.coursework.story.dto.CommentDTO;
import com.coursework.story.model.Comment;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.CommentRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, StoryRepository storyRepository,
                          UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public CommentDTO addComment(Long storyId, String commentText) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        Comment comment = new Comment();

        comment.setStory(story);
        comment.setUser(user);
        comment.setText(commentText);

        if (!Objects.equals(story.getUser().getId(), comment.getUser().getId())) {
            notificationService.send(
                    story.getUser(),
                    "💬 Your story \"" + story.getTitle() + "\" received a new comment!"
            );
        }

        return new CommentDTO(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
    }

    public List<CommentDTO> getCommentsByStory(Long storyId) {
        return commentRepository.findByStoryIdOrderByCreatedAt(storyId).stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }

    public Page<CommentDTO> getUserComments(Pageable pageable) {
        User user = getAuthenticatedUser();
        Page<Comment> comments = commentRepository.findByUserId(user.getId(), pageable);
        return comments.map(comment -> new CommentDTO(comment, comment.getStory()));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}