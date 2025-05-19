package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.Page;
import com.coursework.story.model.Story;
import com.coursework.story.model.StoryStatus;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {

    private final PageRepository pageRepository;
    private final AuthService authService;
    private final StoryRepository storyRepository;
    private final PlaythroughRepository playthroughRepository;

    public PageService(PageRepository pageRepository, AuthService authService,
                       StoryRepository storyRepository, PlaythroughRepository playthroughRepository) {
        this.pageRepository = pageRepository;
        this.authService = authService;
        this.storyRepository = storyRepository;
        this.playthroughRepository = playthroughRepository;
    }

    public PageDTO getPageById(Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NotFoundException("Page not found"));
        return new PageDTO(page);
    }

    public PageDTO getPageByStoryAndNumber(Long storyId, int pageNumber) {
        Page page = pageRepository.findByStoryIdAndPageNumber(storyId, pageNumber)
                .orElseThrow(() -> new NotFoundException("Page not found"));
        return new PageDTO(page);
    }

    public List<PageDTO> getPagesByStoryId(Long storyId) {
        List<Page> pages = pageRepository.findAllByStoryIdOrderByPageNumber(storyId);
        return pages.stream()
                .map(PageDTO::new)
                .toList();
    }

    public List<PageDTO> getPagesMapByStoryId(Long storyId) {
        List<Page> pages = pageRepository.findAllByStoryId(storyId);
        return pages.stream()
                .map(PageDTO::new)
                .toList();
    }

    @Transactional
    public PageDTO updatePage(Long pageId, PageDTO newPage) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NotFoundException("Page not found"));

        Story story = page.getStory();
        User author = story.getUser();
        User currentUser = authService.getAuthenticatedUserOrThrow();

        if (!author.getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not allowed to edit this page");
        }

        if (story.getStatus() != StoryStatus.DRAFT) {
            throw new BadRequestException("Pages can only be updated in drafts");
        }

        page.setTitle(newPage.getTitle());
        page.setPageNumber(newPage.getPageNumber());
        page.setParagraphs(newPage.getParagraphs());
        page.setChoices(newPage.getChoices());
        page.setPositionX(newPage.getPositionX());
        page.setPositionY(newPage.getPositionY());
        return new PageDTO(pageRepository.save(page));
    }

    @Transactional
    public PageDTO savePage(PageDTO newPage) {
        Story story = storyRepository.findById(newPage.getStoryId())
                .orElseThrow(() -> new NotFoundException("Story not found"));

        User currentUser = authService.getAuthenticatedUserOrThrow();
        if (!story.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not allowed to add a page to this story");
        }

        if (story.getStatus() != StoryStatus.DRAFT) {
            throw new BadRequestException("Pages can only be added to drafts");
        }

        boolean pageExists = pageRepository.existsByStoryIdAndPageNumber(newPage.getStoryId(), newPage.getPageNumber());
        if (pageExists) {
            throw new BadRequestException("Page number already exists for this story");
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
                .orElseThrow(() -> new NotFoundException("Page not found"));

        Story story = page.getStory();

        if (story.getStatus() != StoryStatus.DRAFT) {
            throw new BadRequestException("Pages can only be deleted from drafts");
        }

        User author = story.getUser();
        User currentUser = authService.getAuthenticatedUserOrThrow();

        if (!author.getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not allowed to delete this page");
        }

        boolean isInUse = playthroughRepository.existsByCurrentPage(page);
        if (isInUse) {
            throw new BadRequestException("Cannot delete page: it is currently referenced by a playthrough");
        }

        if (story.getStartPageNumber() != null && story.getStartPageNumber().equals(page.getPageNumber())) {
            story.setStartPageNumber(null);
            storyRepository.save(story);
        }

        pageRepository.delete(page);
    }
}