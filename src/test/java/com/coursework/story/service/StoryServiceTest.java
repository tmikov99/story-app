package com.coursework.story.service;

import com.coursework.story.dto.StoryDTO;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.StoryValidationException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.*;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock StoryRepository storyRepository;
    @Mock UserRepository userRepository;
    @Mock PageRepository pageRepository;
    @Mock PlaythroughRepository playthroughRepository;
    @Mock NotificationService notificationService;
    @Mock FirebaseStorageService firebaseStorageService;
    @Mock DraftService draftService;
    @Mock AuthService authService;

    @InjectMocks
    StoryService storyService;

    User mockUser;
    Story mockStory;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");

        mockStory = new Story();
        mockStory.setId(1L);
        mockStory.setUser(mockUser);
        mockStory.setStatus(StoryStatus.DRAFT);
        mockStory.setTitle("Sample Story");
    }

    @Test
    void getStoryById_found() {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryDTO dto = storyService.getStoryById(1L);

        assertEquals("Sample Story", dto.getTitle());
    }

    @Test
    void getStoryById_notFound() {
        when(storyRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> storyService.getStoryById(2L));
    }

    @Test
    void getStoryPreviewById_unauthorizedDraft() {
        mockStory.setStatus(StoryStatus.DRAFT);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));
        when(authService.getAuthenticatedUser()).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> storyService.getStoryPreviewById(1L));
    }

    @Test
    void updateStartPageNumber_success() {
        mockStory.setStartPageNumber(1);

        Page page = new Page();
        page.setPageNumber(5);
        mockStory.setPages(List.of(page));

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));
        when(storyRepository.save(any())).thenReturn(mockStory);

        int updated = storyService.updateStartPageNumber(1L, 5);

        assertEquals(5, updated);
    }

    @Test
    void updateStartPageNumber_unauthorized() {
        User otherUser = new User();
        otherUser.setId(99L);
        mockStory.setUser(otherUser);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        assertThrows(UnauthorizedException.class, () -> storyService.updateStartPageNumber(1L, 5));
    }

    @Test
    void updateStartPageNumber_noPages_throws() {
        mockStory.setPages(new ArrayList<>());

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        assertThrows(BadRequestException.class, () -> storyService.updateStartPageNumber(1L, 1));
    }

    @Test
    void updateStartPageNumber_invalidPageNumber_throws() {
        Page page = new Page();
        page.setPageNumber(3);
        mockStory.setPages(List.of(page));

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        assertThrows(BadRequestException.class, () -> storyService.updateStartPageNumber(1L, 99)); // Not in story
    }

    @Test
    void copyStoryAsDraft_success() {
        mockStory.setStatus(StoryStatus.PUBLISHED);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));
        when(draftService.createDraftFromPublished(mockStory, mockUser)).thenReturn(mockStory);

        StoryDTO dto = storyService.copyStoryAsDraft(1L);

        assertEquals(mockStory.getTitle(), dto.getTitle());
    }

    @Test
    void copyStoryAsDraft_onDraft_throws() {
        mockStory.setStatus(StoryStatus.DRAFT);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        assertThrows(BadRequestException.class, () -> storyService.copyStoryAsDraft(1L));
    }

    @Test
    void publishStory_validStory_succeeds() {
        Page p1 = new Page(); p1.setPageNumber(1);
        Choice choice = new Choice(); choice.setTargetPage(2);
        p1.setChoices(List.of(choice));

        Page p2 = new Page(); p2.setPageNumber(2); p2.setChoices(new ArrayList<>());

        mockStory.setPages(List.of(p1, p2));
        mockStory.setStartPageNumber(1);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));
        when(storyRepository.save(any())).thenReturn(mockStory);

        StoryDTO dto = storyService.publishStory(1L);
        assertEquals(mockStory.getTitle(), dto.getTitle());
    }

    @Test
    void publishStory_alreadyPublished_throws() {
        mockStory.setStatus(StoryStatus.PUBLISHED);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        assertThrows(BadRequestException.class, () -> storyService.publishStory(1L));
    }

    @Test
    void publishStory_noPages_throws() {
        mockStory.setPages(new ArrayList<>());
        mockStory.setStartPageNumber(1);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryValidationException ex = assertThrows(
                StoryValidationException.class,
                () -> storyService.publishStory(1L)
        );
        List<String> errors = ex.getErrors();
        List<String> expected = List.of("A story must have at least one page.");
        assertEquals(errors, expected);
    }

    @Test
    void publishStory_nullStartPage_throws() {
        Page page = new Page();
        page.setPageNumber(1);
        page.setChoices(new ArrayList<>());

        mockStory.setPages(List.of(page));
        mockStory.setStartPageNumber(null);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryValidationException ex = assertThrows(
                StoryValidationException.class,
                () -> storyService.publishStory(1L)
        );
        List<String> errors = ex.getErrors();
        List<String> expected = List.of("Start page must be set before publishing.");
        assertEquals(errors, expected);
    }

    @Test
    void publishStory_startPageNotInPages_throws() {
        Page page = new Page();
        page.setPageNumber(2);
        page.setChoices(new ArrayList<>());

        mockStory.setPages(List.of(page));
        mockStory.setStartPageNumber(1);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryValidationException ex = assertThrows(
                StoryValidationException.class,
                () -> storyService.publishStory(1L)
        );
        List<String> errors = ex.getErrors();
        List<String> expected = List.of("Start page must be one of the story's pages.");
        assertEquals(errors, expected);
    }

    @Test
    void publishStory_duplicatePages_throws() {
        Page p1 = new Page(); p1.setPageNumber(1); p1.setChoices(new ArrayList<>());
        Page p2 = new Page(); p2.setPageNumber(1); p2.setChoices(new ArrayList<>());

        mockStory.setPages(List.of(p1, p2));
        mockStory.setStartPageNumber(1);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryValidationException ex = assertThrows(
                StoryValidationException.class,
                () -> storyService.publishStory(1L)
        );
        List<String> errors = ex.getErrors();
        List<String> expected = List.of("Duplicate page numbers found: [1]");
        assertEquals(errors, expected);
    }

    @Test
    void publishStory_choiceToInvalidPage_throws() {
        Page p1 = new Page(); p1.setPageNumber(1);
        Choice choice = new Choice(); choice.setTargetPage(99);
        p1.setChoices(List.of(choice));

        mockStory.setPages(List.of(p1));
        mockStory.setStartPageNumber(1);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryValidationException ex = assertThrows(
                StoryValidationException.class,
                () -> storyService.publishStory(1L)
        );
        List<String> errors = ex.getErrors();
        List<String> expected = List.of("Choice on page 1 points to a non-existent page: 99");
        assertEquals(errors, expected);
    }

    @Test
    void publishStory_unreachablePage_throws() {
        Page p1 = new Page(); p1.setPageNumber(1); p1.setChoices(new ArrayList<>());
        Page p2 = new Page(); p2.setPageNumber(2); p2.setChoices(new ArrayList<>());

        mockStory.setPages(List.of(p1, p2));
        mockStory.setStartPageNumber(1);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        StoryValidationException ex = assertThrows(
                StoryValidationException.class,
                () -> storyService.publishStory(1L)
        );
        List<String> errors = ex.getErrors();
        List<String> expected = List.of("The following pages are unreachable from the start page: [2]" );
        assertEquals(errors, expected);
    }

    @Test
    void deleteStory_success() {
        mockStory.setLikedByUsers(new HashSet<>());
        mockStory.setFavoriteByUsers(new HashSet<>());
        mockStory.setDrafts(new ArrayList<>());
        mockStory.setCoverImageUrl("https://firebase.com/img123");

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));
        when(storyRepository.countByCoverImageUrlExcludingStory(any(), any())).thenReturn(0L);

        storyService.deleteStory(1L);

        verify(playthroughRepository).deleteByStory(mockStory);
        verify(storyRepository).delete(mockStory);
    }

    @Test
    void deleteStory_unauthorized_throws() {
        User other = new User();
        other.setId(2L);
        mockStory.setUser(other);

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(mockUser);
        when(storyRepository.findById(1L)).thenReturn(Optional.of(mockStory));

        assertThrows(UnauthorizedException.class, () -> storyService.deleteStory(1L));
    }
}