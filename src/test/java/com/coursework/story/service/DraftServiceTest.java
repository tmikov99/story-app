package com.coursework.story.service;

import com.coursework.story.model.*;
import com.coursework.story.repository.PageRepository;
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
class DraftServiceTest {

    @Mock private StoryRepository storyRepository;
    @Mock private PageRepository pageRepository;

    @InjectMocks
    private DraftService draftService;

    private Story publishedStory;
    private User author;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);

        publishedStory = new Story();
        publishedStory.setId(10L);
        publishedStory.setTitle("Test Story");
        publishedStory.setDescription("Desc");
        publishedStory.setUser(author);
        publishedStory.setStartPageNumber(1);
        publishedStory.setStatus(StoryStatus.PUBLISHED);
        publishedStory.setGenres(List.of(Genre.FANTASY));
        publishedStory.setTags(new HashSet<>(List.of("magic", "quest")));
        publishedStory.setVersion(1);
        publishedStory.setCoverImageUrl("url");

        Page page = new Page();
        page.setPageNumber(1);
        page.setTitle("Page 1");
        page.setParagraphs(List.of("Para1", "Para2"));
        page.setChoices(List.of(new Choice("Go to 2", 2)));
        page.setPositionX(100.0);
        page.setPositionY(200.0);

        publishedStory.setPages(List.of(page));
    }

    @Test
    void ensureDraftExists_shouldReturnExistingDraft() {
        Story draft = new Story();
        when(storyRepository.findByOriginalStoryId(publishedStory.getId())).thenReturn(Optional.of(draft));

        Story result = draftService.ensureDraftExists(publishedStory);

        assertEquals(draft, result);
        verify(storyRepository, never()).save(any());
    }

    @Test
    void ensureDraftExists_shouldCreateNewDraftWhenMissing() {
        when(storyRepository.findByOriginalStoryId(publishedStory.getId())).thenReturn(Optional.empty());

        ArgumentCaptor<Story> storyCaptor = ArgumentCaptor.forClass(Story.class);
        Story savedDraft = new Story();
        savedDraft.setId(20L);
        when(storyRepository.save(any())).thenReturn(savedDraft);

        Story result = draftService.ensureDraftExists(publishedStory);

        assertEquals(savedDraft, result);
        verify(storyRepository).save(storyCaptor.capture());
        Story draft = storyCaptor.getValue();

        assertEquals("Test Story DRAFT", draft.getTitle());
        assertEquals(StoryStatus.DRAFT, draft.getStatus());
        assertEquals(publishedStory.getId(), draft.getOriginalStory().getId());
        verify(pageRepository).saveAll(any());
    }

    @Test
    void createDraftFromPublished_shouldCopyPagesAndChoices() {
        Story savedDraft = new Story();
        savedDraft.setId(20L);

        when(storyRepository.save(any())).thenReturn(savedDraft);

        Story result = draftService.createDraftFromPublished(publishedStory, author);

        assertEquals(savedDraft, result);
        verify(pageRepository).saveAll(argThat(pagesIterable  -> {

            List<Page> pages = new ArrayList<>();
            pagesIterable.forEach(pages::add);

            if (pages.size() != 1) return false;
            Page copied = pages.getFirst();
            return copied.getTitle().equals("Page 1") &&
                    copied.getParagraphs().equals(List.of("Para1", "Para2")) &&
                    copied.getChoices().getFirst().getText().equals("Go to 2") &&
                    copied.getChoices().getFirst().getTargetPage() == 2 &&
                    copied.getStory().getId().equals(savedDraft.getId());
        }));
    }
}