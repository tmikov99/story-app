package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.model.Page;
import com.coursework.story.model.Story;
import com.coursework.story.model.StoryStatus;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.StoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PageService {

    private final PageRepository pageRepository;
    private final AuthService authService;
    private final StoryRepository storyRepository;
    private final DraftService draftService;

    public PageService(PageRepository pageRepository, AuthService authService,
                       StoryRepository storyRepository, DraftService draftService) {
        this.pageRepository = pageRepository;
        this.authService = authService;
        this.storyRepository = storyRepository;
        this.draftService = draftService;
    }

    public Page getPageById(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));
    }

    public Page getPageByStoryAndNumber(Long storyId, int pageNumber) {
        return pageRepository.findByStoryIdAndPageNumber(storyId, pageNumber)
                .orElseThrow(() -> new RuntimeException("Page not found"));
    }

    public List<Page> getPagesByStoryId(Long storyId) {
        return pageRepository.findAllByStoryIdOrderByPageNumber(storyId);
    }

    public List<PageDTO> getPagesMapByStoryId(Long storyId) {
        List<Page> pages = pageRepository.findAllByStoryId(storyId);
        List<PageDTO> pagesDTOs = new ArrayList<>();
        for (Page page : pages) {
            PageDTO pageDTO = new PageDTO(page);
            pageDTO.setPositionX(page.getPositionX());
            pageDTO.setPositionY(page.getPositionY());
            pagesDTOs.add(pageDTO);
        }

        return pagesDTOs;
    }

    @Transactional
    public Page updatePage(Long pageId, PageDTO newPage) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        Story story = page.getStory();
        User author = story.getUser();
        User currentUser = authService.getAuthenticatedUserOrThrow();

        if (!author.getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to edit this page");
        }

        if (story.getStatus() == StoryStatus.PUBLISHED) {
            story = draftService.ensureDraftExists(story);
            page = pageRepository.findByStoryIdAndPageNumber(story.getId(), page.getPageNumber())
                    .orElseThrow(() -> new RuntimeException("Page not found in draft"));
        }

        page.setTitle(newPage.getTitle());
        page.setPageNumber(newPage.getPageNumber());
        page.setParagraphs(newPage.getParagraphs());
        page.setChoices((newPage.getChoices()));
        page.setPositionX(newPage.getPositionX());
        page.setPositionY(newPage.getPositionY());
        return pageRepository.save(page);
    }

    @Transactional
    public PageDTO savePage(PageDTO newPage) {
        Story story = storyRepository.findById(newPage.getStoryId())
                .orElseThrow(() -> new EntityNotFoundException("Story not found"));

        User currentUser = authService.getAuthenticatedUserOrThrow();
        if (!story.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to add a page to this story");
        }

        if (story.getStatus() == StoryStatus.PUBLISHED) {
            story = draftService.ensureDraftExists(story);
            newPage.setStoryId(story.getId());
        }

        boolean pageExists = pageRepository.existsByStoryIdAndPageNumber(newPage.getStoryId(), newPage.getPageNumber());
        if (pageExists) {
            throw new IllegalArgumentException("Page number already exists for this story");
        }

        Page page = new Page();
        page.setTitle(newPage.getTitle());
        page.setPageNumber(story.getFirstAvailablePageNumber());
        page.setParagraphs(newPage.getParagraphs());
        page.setChoices(newPage.getChoices());
        page.setPositionX(newPage.getPositionX());
        page.setPositionY(newPage.getPositionY());
        page.setStory(story);

        Page savedPage = pageRepository.save(page);
        return new PageDTO(savedPage);
    }

    @Transactional
    public void deletePage(Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        Story story = page.getStory();
        User author = story.getUser();

        User currentUser = authService.getAuthenticatedUserOrThrow();

        if (!author.getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to delete this page");
        }

        if (story.getStatus() == StoryStatus.PUBLISHED) {
            story = draftService.ensureDraftExists(story);
            page = pageRepository.findByStoryIdAndPageNumber(story.getId(), page.getPageNumber())
                    .orElseThrow(() -> new RuntimeException("Page not found in draft"));
        }

        if (story.getStartPageNumber() != null && story.getStartPageNumber().equals(page.getPageNumber())) {
            story.setStartPageNumber(null);
            storyRepository.save(story);
        }

        pageRepository.delete(page);
    }
}