package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.Page;
import com.coursework.story.model.Playthrough;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaythroughService {
    private final PlaythroughRepository playthroughRepository;
    private final StoryRepository storyRepository;
    private final PageRepository pageRepository;
    private final AuthService authService;

    public PlaythroughService(PlaythroughRepository playthroughRepository, StoryRepository storyRepository,
                              PageRepository pageRepository, AuthService authService) {
        this.playthroughRepository = playthroughRepository;
        this.storyRepository = storyRepository;
        this.pageRepository = pageRepository;
        this.authService = authService;
    }

    @Transactional
    public PlaythroughDTO startPlaythrough(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));
        playthroughRepository.deactivatePlaythroughsForUserAndStory(user, story);

        boolean isFirstPlaythrough = playthroughRepository.countByUserAndStory(user, story) == 0;

        Integer startPageNumber = story.getStartPageNumber();
        Page startPage = story.getPages().stream()
                .filter(p -> p.getPageNumber() == startPageNumber)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Start page not found"));

        Playthrough playthrough = new Playthrough();
        playthrough.setUser(user);
        playthrough.setStory(story);
        playthrough.setCurrentPage(startPage);
        playthrough.setPath(new ArrayList<>(List.of(startPageNumber)));
        playthrough.setActive(true);
        playthrough.setCompleted(false);
        playthrough.setStartedAt(LocalDateTime.now());
        playthrough.setLastVisited(LocalDateTime.now());

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);

        if (isFirstPlaythrough) {
            story.incrementReads();
            storyRepository.save(story);
        }

        return new PlaythroughDTO(savedPlaythrough);
    }

    @Transactional
    public PageDTO choosePage(Long playthroughId, int pageNumber) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);

        Page nextPage = pageRepository.findByStoryIdAndPageNumber(playthrough.getStory().getId(), pageNumber)
                .orElseThrow(() -> new NotFoundException("Page not found"));

        playthrough.setCurrentPage(nextPage);
        playthrough.getPath().add(pageNumber);
        playthrough.setLastVisited(LocalDateTime.now());

        if (nextPage.isEndPage()) {
            playthrough.setCompleted(true);
        }
        playthroughRepository.save(playthrough);

        return new PageDTO(nextPage);
    }

    public PlaythroughDTO getPlaythroughById(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        return new PlaythroughDTO(playthrough);
    }

    public List<PlaythroughDTO> getPlaythroughsForUserAndStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));
        List<Playthrough> playthroughs = playthroughRepository.findByUserAndStoryOrderByLastVisitedDesc(user, story);
        return playthroughs.stream().map(PlaythroughDTO::new).toList();
    }

    @Transactional
    public void loadPlaythrough(Long playthroughId) {
        User user = authService.getAuthenticatedUserOrThrow();
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        if (playthrough.isActive()) return;

        playthroughRepository.deactivatePlaythroughsForUserAndStory(user, playthrough.getStory());
        playthrough.setActive(true);
        playthrough.setLastVisited(LocalDateTime.now());
        playthroughRepository.save(playthrough);
    }

    public PageDTO getCurrentPageForPlaythrough(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        return new PageDTO(playthrough.getCurrentPage());
    }

    public org.springframework.data.domain.Page<PlaythroughDTO> getPaginatedPlaythroughsForUser(String query, Pageable pageable) {
        User user = authService.getAuthenticatedUserOrThrow();
        org.springframework.data.domain.Page<Playthrough> page = (query != null && !query.isBlank())
                ? playthroughRepository.searchByUserAndStoryTitle(user, query, pageable)
                : playthroughRepository.findByUser(user, pageable);
        return page.map(PlaythroughDTO::new);
    }

    @Transactional
    public void deletePlaythrough(Long playthroughId) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        playthroughRepository.delete(playthrough);
    }

    private Playthrough getPlaythroughOwnedByUser(Long playthroughId) {
        Playthrough playthrough = playthroughRepository.findById(playthroughId)
                .orElseThrow(() -> new NotFoundException("Playthrough not found"));
        User currentUser = authService.getAuthenticatedUserOrThrow();
        if (!playthrough.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You do not own this playthrough.");
        }
        return playthrough;
    }
}