package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.*;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaythroughServiceTest {

    @Mock private PlaythroughRepository playthroughRepository;
    @Mock private StoryRepository storyRepository;
    @Mock private PageRepository pageRepository;
    @Mock private AuthService authService;

    @InjectMocks private PlaythroughService playthroughService;

    private User user;
    private Story story;
    private Page startPage;
    private Playthrough mockPlaythrough;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test_user");

        startPage = new Page();
        startPage.setPageNumber(1);

        story = new Story();
        story.setId(1L);
        story.setStartPageNumber(1);
        story.setPages(List.of(startPage));
        startPage.setStory(story);

        mockPlaythrough = new Playthrough();
        mockPlaythrough.setCurrentPage(startPage);
        mockPlaythrough.setStory(story);
    }

    @Test
    void startPlaythrough_createsNewPlaythrough() {
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));
        when(playthroughRepository.countByUserAndStory(user, story)).thenReturn(0L);
        when(playthroughRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlaythroughDTO result = playthroughService.startPlaythrough(1L);

        assertEquals(1, result.getPath().getFirst());
        verify(storyRepository).save(story);
    }

    @Test
    void startPlaythrough_storyNotFound() {
        when(storyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> playthroughService.startPlaythrough(1L));
    }

    @Test
    void startPlaythrough_startPageNotFound() {
        story.setPages(List.of());
        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        assertThrows(NotFoundException.class, () -> playthroughService.startPlaythrough(1L));
    }

    @Test
    void choosePage_success() {
        Playthrough pt = new Playthrough();
        pt.setId(1L);
        pt.setUser(user);
        pt.setStory(story);
        pt.setCurrentPage(startPage);
        pt.setPath(new ArrayList<>(List.of(1)));

        Page nextPage = new Page();
        nextPage.setPageNumber(2);
        nextPage.setStory(story);

        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(pageRepository.findByStoryIdAndPageNumber(story.getId(), 2)).thenReturn(Optional.of(nextPage));

        PageDTO result = playthroughService.choosePage(1L, 2);

        assertEquals(2, result.getPageNumber());
        assertTrue(pt.isCompleted());
    }

    @Test
    void choosePage_pageNotFound() {
        Playthrough pt = new Playthrough();
        pt.setUser(user);
        pt.setStory(story);

        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(pageRepository.findByStoryIdAndPageNumber(anyLong(), anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> playthroughService.choosePage(1L, 99));
    }

    @Test
    void getPlaythroughById_success() {
        Playthrough pt = new Playthrough();
        pt.setUser(user);
        pt.setCurrentPage(new Page());
        pt.setStory(new Story());

        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        PlaythroughDTO result = playthroughService.getPlaythroughById(1L);
        assertNotNull(result);
    }

    @Test
    void getPlaythroughById_unauthorized() {
        Playthrough pt = new Playthrough();
        User another = new User();
        another.setId(999L);
        pt.setUser(another);

        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> playthroughService.getPlaythroughById(1L));
    }

    @Test
    void getPlaythroughsForUserAndStory_success() {
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));
        when(playthroughRepository.findByUserAndStory(user, story)).thenReturn(List.of(mockPlaythrough));

        List<PlaythroughDTO> result = playthroughService.getPlaythroughsForUserAndStory(1L);
        assertEquals(1, result.size());
    }

    @Test
    void loadPlaythrough_success() {
        Playthrough pt = new Playthrough();
        pt.setUser(user);
        pt.setActive(false);
        pt.setStory(story);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));

        playthroughService.loadPlaythrough(1L);
        assertTrue(pt.isActive());
    }

    @Test
    void getCurrentPageForPlaythrough_success() {
        Playthrough pt = new Playthrough();
        pt.setUser(user);
        pt.setCurrentPage(startPage);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));

        PageDTO page = playthroughService.getCurrentPageForPlaythrough(1L);
        assertEquals(startPage.getPageNumber(), page.getPageNumber());
    }

    @Test
    void getPaginatedPlaythroughsForUser_noQuery() {
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        Pageable pageable = PageRequest.of(0, 10);

        when(playthroughRepository.findByUser(user, pageable)).thenReturn(new PageImpl<>(List.of(mockPlaythrough)));
        var page = playthroughService.getPaginatedPlaythroughsForUser(null, pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void deletePlaythrough_success() {
        Playthrough pt = new Playthrough();
        pt.setUser(user);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(playthroughRepository.findById(1L)).thenReturn(Optional.of(pt));

        playthroughService.deletePlaythrough(1L);
        verify(playthroughRepository).delete(pt);
    }
}
