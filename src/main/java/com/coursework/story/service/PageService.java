package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.*;
import com.coursework.story.repository.ItemRepository;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PageService {

    private final PageRepository pageRepository;
    private final AuthService authService;
    private final StoryRepository storyRepository;
    private final PlaythroughRepository playthroughRepository;
    private final ItemRepository itemRepository;

    public PageService(PageRepository pageRepository, AuthService authService,
                       StoryRepository storyRepository, PlaythroughRepository playthroughRepository,
                       ItemRepository itemRepository) {
        this.pageRepository = pageRepository;
        this.authService = authService;
        this.storyRepository = storyRepository;
        this.playthroughRepository = playthroughRepository;
        this.itemRepository = itemRepository;
    }

    public PageDTO getPageById(Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NotFoundException("Page not found"));
        checkDraftAccess(page.getStory());
        return new PageDTO(page);
    }

    public PageDTO getPageByStoryAndNumber(Long storyId, int pageNumber) {
        Page page = pageRepository.findByStoryIdAndPageNumber(storyId, pageNumber)
                .orElseThrow(() -> new NotFoundException("Page not found"));
        checkDraftAccess(page.getStory());
        return new PageDTO(page);
    }

    public List<PageDTO> getPagesByStoryId(Long storyId) {
        List<Page> pages = pageRepository.findAllByStoryIdOrderByPageNumber(storyId);
        if (!pages.isEmpty()) {
            checkDraftAccess(pages.getFirst().getStory());
        }
        return pages.stream()
                .map(PageDTO::new)
                .toList();
    }

    public List<PageDTO> getPagesMapByStoryId(Long storyId) {
        List<Page> pages = pageRepository.findAllByStoryId(storyId);
        if (!pages.isEmpty()) {
            checkDraftAccess(pages.getFirst().getStory());
        }
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

        applyPageProperties(page, newPage);
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
        applyPageProperties(page, newPage);

        page.setStory(story);
        page.setPageNumber(story.getFirstAvailablePageNumber());

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

        List<Page> pagesInStory = pageRepository.findAllByStoryId(story.getId());
        for (Page p : pagesInStory) {
            if (p.getChoices() != null && p.getChoices().removeIf(choice -> choice.getTargetPage().equals(page.getPageNumber()))) {
                pageRepository.save(p);
            }
        }

        if (story.getStartPageNumber() != null && story.getStartPageNumber().equals(page.getPageNumber())) {
            story.setStartPageNumber(null);
            storyRepository.save(story);
        }

        pageRepository.delete(page);
    }

    private void checkDraftAccess(Story story) {
        if (story.getStatus() == StoryStatus.DRAFT) {
            Optional<User> user = authService.getAuthenticatedUser();
            if (user.isEmpty() || !story.getUser().getId().equals(user.get().getId())) {
                throw new UnauthorizedException("Unauthorized access to draft story");
            }
        }
    }

    private void applyPageProperties(Page page, PageDTO newPage) {
        Set<Item> grantedItems = newPage.getItemsGranted().stream()
                .map(dto -> itemRepository.findById(dto.getId()).orElseThrow(() -> new NotFoundException("Item not found")))
                .collect(Collectors.toSet());

        Set<Item> removedItems = newPage.getItemsRemoved().stream()
                .map(dto -> itemRepository.findById(dto.getId()).orElseThrow(() -> new NotFoundException("Item not found")))
                .collect(Collectors.toSet());

        page.setTitle(newPage.getTitle());
        page.setParagraphs(newPage.getParagraphs());
        page.setChoices(newPage.getChoices());
        page.setItemsGranted(grantedItems);
        page.setItemsRemoved(removedItems);
        page.setEnemy(newPage.getEnemy());
        page.setStatModifiers(newPage.getStatModifiers());
        page.setPositionX(newPage.getPositionX());
        page.setPositionY(newPage.getPositionY());
    }
}