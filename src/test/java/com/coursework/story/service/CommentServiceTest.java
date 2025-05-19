package com.coursework.story.service;

import com.coursework.story.dto.CommentDTO;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.*;
import com.coursework.story.repository.CommentRepository;
import com.coursework.story.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private StoryRepository storyRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuthService authService;

    @InjectMocks private CommentService commentService;

    private User user;
    private Story story;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test_user");

        story = new Story();
        story.setId(100L);
        story.setTitle("Test Story");
        story.setUser(new User());
        story.getUser().setId(2L);

        comment = new Comment();
        comment.setId(10L);
        comment.setText("Nice story!");
        comment.setUser(user);
        comment.setStory(story);
    }

    @Test
    void addComment_shouldCreateAndReturnCommentDTO() {
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(storyRepository.findById(100L)).thenReturn(Optional.of(story));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        CommentDTO result = commentService.addComment(100L, "Great!");

        assertEquals("Great!", result.getText());
        assertEquals(user.getUsername(), result.getUsername());
        verify(notificationService).send(
                eq(story.getUser()), anyString(), eq(NotificationType.NEW_COMMENT), eq(100L)
        );
    }

    @Test
    void addComment_shouldThrowIfStoryNotFound() {
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(storyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.addComment(999L, "Comment"));
    }

    @Test
    void deleteComment_shouldDeleteIfOwner() {
        comment.setUser(user);
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(10L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_shouldThrowIfNotOwner() {
        comment.setUser(new User());
        comment.getUser().setId(99L);
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThrows(UnauthorizedException.class, () -> commentService.deleteComment(10L));
    }

    @Test
    void deleteComment_shouldThrowIfNotFound() {
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(commentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.deleteComment(10L));
    }

    @Test
    void getCommentsByStory_shouldReturnPagedComments() {
        Pageable pageable = PageRequest.of(0, 10);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test Comment");
        comment.setUser(user);

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(commentRepository.findByStoryIdOrderByCreatedAt(100L, pageable)).thenReturn(commentPage);

        Page<CommentDTO> result = commentService.getCommentsByStory(100L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Comment", result.getContent().getFirst().getText());
    }

    @Test
    void getUserComments_shouldReturnUserComments() {
        Pageable pageable = PageRequest.of(0, 10);
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("User's comment");
        comment.setUser(user);
        comment.setStory(story);

        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(commentRepository.findByUserId(user.getId(), pageable)).thenReturn(commentPage);

        Page<CommentDTO> result = commentService.getUserComments(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("User's comment", result.getContent().getFirst().getText());
    }
}
