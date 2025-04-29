package com.coursework.story.service;

import com.coursework.story.dto.LikeResponse;
import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.Page;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final PageRepository pageRepository;
    private final NotificationService notificationService;

    public StoryService(StoryRepository storyRepository, UserRepository userRepository,
                        PageRepository pageRepository, NotificationService notificationService) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.pageRepository = pageRepository;
        this.notificationService = notificationService;
    }

    public List<StoryDTO> getStories() {
        Optional<User> user = getAuthenticatedUser();
        Set<Long> likedIds = user.map(u -> u.getLikedStories()
                        .stream()
                        .map(Story::getId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        Set<Long> favoriteIds = user.map(u -> u.getFavoriteStories()
                        .stream()
                        .map(Story::getId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        return storyRepository.findAll().stream()
                .map(story -> {
                    StoryDTO dto = new StoryDTO(story);
                    dto.setLiked(likedIds.contains(story.getId()));
                    dto.setFavorite(favoriteIds.contains(story.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public StoryDTO getStoryById(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        return new StoryDTO(story, story.getPages());
    }

    public StoryDTO getStoryPreviewById(Long storyId) {
        Optional<User> user = getAuthenticatedUser();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        boolean isLiked = user.isPresent() && user.get().getLikedStories().contains(story);
        boolean isFavorite = user.isPresent() && user.get().getFavoriteStories().contains(story);

        return new StoryDTO(story, isLiked, isFavorite);
    }

    @Transactional
    public void updatePages(Long storyId, List<PageDTO> updatedPages) {
        Optional<Story> optionalStory = storyRepository.findById(storyId);
        if (optionalStory.isEmpty()) {
//            return ResponseEntity.notFound().build();
            return;
        }

        Story story = optionalStory.get();

        // Map of updated page IDs (or null for new ones)
        Set<Long> updatedIds = updatedPages.stream()
                .map(PageDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Remove pages that are not in the updated list
        List<Page> pagesToRemove = story.getPages().stream()
                .filter(p -> p.getId() != null && !updatedIds.contains(p.getId()))
                .toList();

        for (Page pageToRemove : pagesToRemove) {
            pageRepository.delete(pageToRemove);
        }

        for (PageDTO dto : updatedPages) {
            Page page;

            if (dto.getId() != null) {
                // Update existing page
                page = story.getPages().stream()
                        .filter(p -> p.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(); // Optional: handle missing ID gracefully
            } else {
                // New page
                page = new Page();
                page.setStory(story);
            }

            page.setTitle(dto.getTitle());
            page.setPageNumber(dto.getPageNumber());
            page.setParagraphs(dto.getParagraphs());
//            page.setChoices(dto.getChoices().stream() //TODO: Update along with UI
//                    .map(c -> new Choice(c.getText(), c.getTargetPage()))
//                    .toList());
            page.setPositionX(dto.getPositionX());
            page.setPositionY(dto.getPositionY());

            pageRepository.save(page);
        }
        story.touch();
        storyRepository.save(story);
    }

    @Transactional
    public StoryDTO saveStory(Story story) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));;
        story.setUser(user);

        Story savedStory = storyRepository.save(story);
        return new StoryDTO(savedStory);
    }

    @Transactional
    public LikeResponse toggleLikeStory(Long storyId) {
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        Set<Story> likedStories = user.getLikedStories();
        boolean response;

        if (likedStories.contains(story)) {
            likedStories.remove(story);
            story.decrementLikes();
            response = false;
        } else {
            likedStories.add(story);
            story.incrementLikes();
            response = true;
            if (story.getLikes() == 10) {
                notificationService.send(
                        story.getUser(),
                        "🎉 Your story \"" + story.getTitle() + "\" just reached 10 likes!"
                );
            }
        }

        userRepository.save(user);
        Story savedStory = storyRepository.save(story);
        return new LikeResponse(response, savedStory.getLikes());
    }

    @Transactional
    public boolean toggleFavoriteStory(Long storyId) {
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        Set<Story> favoriteStories = user.getFavoriteStories();
        boolean response;

        if (favoriteStories.contains(story)) {
            favoriteStories.remove(story);
            story.decrementFavorites();
            response = false;
        } else {
            favoriteStories.add(story);
            story.incrementFavorites();
            response = true;
        }

        userRepository.save(user);
        storyRepository.save(story);
        return response;
    }

    public List<StoryDTO> getLikedStories() {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        return user.getLikedStories()
                .stream()
                .map(StoryDTO::new)
                .toList();
    }

    public List<StoryDTO> getFavoriteStories() {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        return user.getFavoriteStories()
                .stream()
                .map(StoryDTO::new)
                .toList();
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return userRepository.findByUsername(username);
        }

        return Optional.empty();
    }
}
