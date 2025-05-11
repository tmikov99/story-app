package com.coursework.story.service;

import com.coursework.story.model.*;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.StoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DraftService {

    private final StoryRepository storyRepository;
    private final PageRepository pageRepository;

    public DraftService(StoryRepository storyRepository,
                        PageRepository pageRepository) {
        this.storyRepository = storyRepository;
        this.pageRepository = pageRepository;
    }

    public Story ensureDraftExists(Story story) {
        return storyRepository.findByOriginalStoryId(story.getId())
                .orElseGet(() -> createDraftFromPublished(story, story.getUser())); //TODO: replace with published story error
    }

    public Story createDraftFromPublished(Story publishedStory, User user) {
        Story draft = getStory(publishedStory, user);

        Story savedDraft = storyRepository.save(draft);

        List<Page> copiedPages = publishedStory.getPages().stream().map(p -> {
            Page page = new Page();
            page.setTitle(p.getTitle());
            page.setPageNumber(p.getPageNumber());
            page.setParagraphs(new ArrayList<>(p.getParagraphs()));
            page.setChoices(p.getChoices().stream()
                    .map(c -> new Choice(c.getText(), c.getTargetPage()))
                    .collect(Collectors.toList()));
            page.setPositionX(p.getPositionX());
            page.setPositionY(p.getPositionY());
            page.setStory(savedDraft);
            return page;
        }).toList();

        pageRepository.saveAll(copiedPages);

        return savedDraft;
    }

    private static Story getStory(Story publishedStory, User user) {
        Story draft = new Story();
        String baseTitle = publishedStory.getTitle().replaceAll(" DRAFT$", "");
        draft.setTitle(baseTitle + " DRAFT");
        draft.setDescription(publishedStory.getDescription());
        draft.setGenres(new ArrayList<>(publishedStory.getGenres()));
        draft.setTags(new HashSet<>(publishedStory.getTags()));
        draft.setStatus(StoryStatus.DRAFT);
        draft.setUser(user);
        draft.setOriginalStory(publishedStory);
        draft.setVersion(publishedStory.getVersion() + 1);
        draft.setCoverImageUrl(publishedStory.getCoverImageUrl());
        return draft;
    }
}