package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.exception.BadRequestException;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @Mock
    private PageRepository pageRepository;
    @Mock
    private AuthService authService;
    @Mock
    private StoryRepository storyRepository;
    @Mock
    private PlaythroughRepository playthroughRepository;

    @InjectMocks
    private PageService pageService;

    private User user;
    private Story story;
    private Page page;
    private PageDTO pageDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        story = new Story();
        story.setId(100L);
        story.setUser(user);
        story.setStatus(StoryStatus.DRAFT);

        page = new Page();
        page.setId(200L);
        page.setStory(story);
        page.setPageNumber(1);

        pageDTO = new PageDTO(page);
    }

    @Test
    void getPageById_success() {
        when(pageRepository.findById(200L)).thenReturn(Optional.of(page));
        PageDTO result = pageService.getPageById(200L);
        assertEquals(pageDTO, result);
    }

    @Test
    void getPageById_notFound() {
        when(pageRepository.findById(200L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> pageService.getPageById(200L));
    }

    @Test
    void getPageByStoryAndNumber_success() {
        when(pageRepository.findByStoryIdAndPageNumber(100L, 1)).thenReturn(Optional.of(page));
        PageDTO result = pageService.getPageByStoryAndNumber(100L, 1);
        assertEquals(pageDTO, result);
    }

    @Test
    void getPageByStoryAndNumber_notFound() {
        when(pageRepository.findByStoryIdAndPageNumber(100L, 1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> pageService.getPageByStoryAndNumber(100L, 1));
    }

    @Test
    void getPagesByStoryId_success() {
        List<Page> pages = List.of(page);
        when(pageRepository.findAllByStoryIdOrderByPageNumber(100L)).thenReturn(pages);
        List<PageDTO> result = pageService.getPagesByStoryId(100L);
        assertEquals(List.of(pageDTO), result);
    }

    @Test
    void getPagesMapByStoryId_success() {
        page.setPositionX(1.0);
        page.setPositionY(2.0);
        when(pageRepository.findAllByStoryId(100L)).thenReturn(List.of(page));
        List<PageDTO> result = pageService.getPagesMapByStoryId(100L);
        assertEquals(1, result.size());
        assertEquals(1.0, result.getFirst().getPositionX());
    }

    @Test
    void updatePage_success() {
        PageDTO newPage = new PageDTO();
        newPage.setPageNumber(2);
        newPage.setTitle("Updated");
        newPage.setParagraphs(List.of("para"));
        newPage.setChoices(List.of());
        newPage.setPositionX(0.5);
        newPage.setPositionY(0.6);

        when(pageRepository.findById(200L)).thenReturn(Optional.of(page));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(pageRepository.save(any())).thenReturn(page);

        PageDTO result = pageService.updatePage(200L, newPage);
        assertEquals("Updated", result.getTitle());
        assertEquals(2, result.getPageNumber());
    }

    @Test
    void updatePage_unauthorized() {
        User otherUser = new User(); otherUser.setId(2L);
        story.setUser(otherUser);

        when(pageRepository.findById(200L)).thenReturn(Optional.of(page));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> pageService.updatePage(200L, new PageDTO()));
    }

    @Test
    void savePage_success() {
        PageDTO dto = new PageDTO();
        dto.setTitle("New Page");
        dto.setStoryId(100L);
        dto.setPageNumber(5);
        dto.setParagraphs(List.of("Text"));
        dto.setChoices(List.of());
        dto.setPositionX(0.0);
        dto.setPositionY(0.0);

        when(storyRepository.findById(100L)).thenReturn(Optional.of(story));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(pageRepository.existsByStoryIdAndPageNumber(anyLong(), anyInt())).thenReturn(false);
        when(pageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PageDTO result = pageService.savePage(dto);
        assertEquals("New Page", result.getTitle());
    }

    @Test
    void savePage_pageExists() {
        PageDTO dto = new PageDTO();
        dto.setStoryId(100L);
        dto.setPageNumber(1);

        when(storyRepository.findById(100L)).thenReturn(Optional.of(story));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(pageRepository.existsByStoryIdAndPageNumber(100L, 1)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> pageService.savePage(dto));
    }

    @Test
    void deletePage_success() {
        story.setStartPageNumber(1);
        when(pageRepository.findById(200L)).thenReturn(Optional.of(page));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(playthroughRepository.existsByCurrentPage(page)).thenReturn(false);

        pageService.deletePage(200L);

        verify(pageRepository).delete(page);
        verify(storyRepository).save(story);
        assertNull(story.getStartPageNumber());
    }

    @Test
    void deletePage_unauthorized() {
        story.setUser(new User() {{ setId(2L); }});

        when(pageRepository.findById(200L)).thenReturn(Optional.of(page));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        assertThrows(UnauthorizedException.class, () -> pageService.deletePage(200L));
    }

    @Test
    void deletePage_failsWhenReferencedByPlaythrough() {
        when(pageRepository.findById(200L)).thenReturn(Optional.of(page));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(playthroughRepository.existsByCurrentPage(page)).thenReturn(true);
        story.setStatus(StoryStatus.DRAFT);

        assertThrows(BadRequestException.class, () -> pageService.deletePage(200L));
    }
}