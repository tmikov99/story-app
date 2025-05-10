package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.model.Page;
import com.coursework.story.model.Playthrough;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaythroughService {
    private final PlaythroughRepository playthroughRepository;
    private final StoryRepository storyRepository;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;

    public PlaythroughService(PlaythroughRepository playthroughRepository, StoryRepository storyRepository, PageRepository pageRepository, UserRepository userRepository) {
        this.playthroughRepository = playthroughRepository;
        this.storyRepository = storyRepository;
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PlaythroughDTO startPlaythrough(Long storyId) {
        User user = getCurrentUser();
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        playthroughRepository.deactivatePlaythroughsForUserAndStory(user, story);

        Playthrough playthrough = new Playthrough();
        playthrough.setUser(user);
        playthrough.setStory(story);
        playthrough.setCurrentPage(story.getPages().getFirst());
        playthrough.setPath(new ArrayList<>(List.of(story.getPages().getFirst().getPageNumber())));
        playthrough.setActive(true);
        playthrough.setCompleted(false);
        playthrough.setStartedAt(LocalDateTime.now());
        playthrough.setLastVisited(LocalDateTime.now());

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);

        story.incrementReads();
        storyRepository.save(story);

        return new PlaythroughDTO(savedPlaythrough);
    }

    @Transactional
    public PageDTO choosePage(Long playthroughId, int pageNumber) {
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);

        Page nextPage = pageRepository.findByStoryIdAndPageNumber(playthrough.getStory().getId(), pageNumber)
                .orElseThrow(() -> new RuntimeException("Page not found"));

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
        User user = getCurrentUser();
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        List<Playthrough> playthroughs = playthroughRepository.findByUserAndStory(user, story);
        return playthroughs.stream().map(PlaythroughDTO::new).toList();
    }

    @Transactional
    public void loadPlaythrough(Long playthroughId) {
        User user = getCurrentUser();
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        if (playthrough.isActive()) {
            return;
        }

        playthroughRepository.deactivatePlaythroughsForUserAndStory(user, playthrough.getStory());
        playthrough.setActive(true);
        playthroughRepository.save(playthrough);
    }

    public PageDTO getCurrentPageForPlaythrough(Long playthroughId) {
        getCurrentUser();
        Playthrough playthrough = getPlaythroughOwnedByUser(playthroughId);
        return new PageDTO(playthrough.getCurrentPage());
    }

    public List<PlaythroughDTO> getAllPlaythroughsForUser() {
        User user = getCurrentUser();
        return playthroughRepository.findByUserOrderByLastVisitedDesc(user)
                .stream()
                .map(PlaythroughDTO::new)
                .toList();
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Playthrough getPlaythroughOwnedByUser(Long playthroughId) {
        Playthrough playthrough = playthroughRepository.findById(playthroughId)
                .orElseThrow(() -> new RuntimeException("Playthrough not found"));
        User currentUser = getCurrentUser();
        if (!playthrough.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this playthrough.");
        }
        return playthrough;
    }

}