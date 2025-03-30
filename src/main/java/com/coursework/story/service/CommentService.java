package com.coursework.story.service;

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

    public Comment addComment(Long storyId, Comment comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        comment.setStory(story);
        comment.setUser(user);
        comment.setText(comment.getText());

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByStory(Long storyId) {
        return commentRepository.findByStoryId(storyId);
    }
}