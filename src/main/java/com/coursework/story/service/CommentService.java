package com.coursework.story.service;

import com.coursework.story.dto.CommentDTO;
import com.coursework.story.model.Comment;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.CommentRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, StoryRepository storyRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
    }

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

        return new CommentDTO(commentRepository.save(comment));
    }

    public List<CommentDTO> getCommentsByStory(Long storyId) {
        return commentRepository.findByStoryIdOrderByCreatedAt(storyId).stream()
                .map(CommentDTO::new)
                .collect(Collectors.toList());
    }
}