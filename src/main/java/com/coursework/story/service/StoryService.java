package com.coursework.story.service;

import com.coursework.story.dto.LikeResponse;
import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.*;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final PageRepository pageRepository;
    private final NotificationService notificationService;
    private final FirebaseStorageService firebaseStorageService;
    private final DraftService draftService;

    public StoryService(StoryRepository storyRepository, UserRepository userRepository,
                        PageRepository pageRepository, NotificationService notificationService,
                        FirebaseStorageService firebaseStorageService, DraftService draftService) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.pageRepository = pageRepository;
        this.notificationService = notificationService;
        this.firebaseStorageService = firebaseStorageService;
        this.draftService = draftService;
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

    public List<StoryDTO> getStoriesByUser(String username) {
        Optional<User> user = getAuthenticatedUser();
        Set<Long> likedIds = user.isPresent() ? user.get().getLikedIds() : Collections.emptySet();
        Set<Long> favoriteIds = user.isPresent() ? user.get().getFavoriteIds() : Collections.emptySet();
        return storyRepository.findAllByUserUsernameOrderByCreatedAt(username).stream()
                .map(story -> {
                    StoryDTO dto = new StoryDTO(story);
                    dto.setLiked(likedIds.contains(story.getId()));
                    dto.setFavorite(favoriteIds.contains(story.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
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

    public org.springframework.data.domain.Page<StoryDTO> searchStories(String query, Pageable pageable) {
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        Optional<User> user = getAuthenticatedUser();
        Set<Long> likedIds = user.map(User::getLikedIds).orElse(Collections.emptySet());
        Set<Long> favoriteIds = user.map(User::getFavoriteIds).orElse(Collections.emptySet());

        org.springframework.data.domain.Page<Story> page = storyRepository.searchByTitleOrTagsOrDescription(query, sortedPageable);

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    public org.springframework.data.domain.Page<StoryDTO> getAllStories(Pageable pageable) {
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findAll(sortedPageable);

        Optional<User> user = getAuthenticatedUser();
        Set<Long> likedIds = user.map(User::getLikedIds).orElse(Collections.emptySet());
        Set<Long> favoriteIds = user.map(User::getFavoriteIds).orElse(Collections.emptySet());

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

//    @Cacheable("trendingStories")
    public List<Story> getTrendingStoriesRaw() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        Pageable limit = PageRequest.of(0, 100); // Cache top 100, for example
        return storyRepository.findTrendingStories(cutoff, limit).getContent();
    }

    public org.springframework.data.domain.Page<StoryDTO> getTrendingStories(Pageable pageable) {
        List<Story> cachedTrending = getTrendingStoriesRaw();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), cachedTrending.size());
        List<Story> paged = cachedTrending.subList(start, end);

        Optional<User> user = getAuthenticatedUser();
        Set<Long> likedIds = user.map(User::getLikedIds).orElse(Collections.emptySet());
        Set<Long> favoriteIds = user.map(User::getFavoriteIds).orElse(Collections.emptySet());

        return new PageImpl<>(paged.stream()
                .map(s -> {
                    StoryDTO dto = new StoryDTO(s);
                    dto.setLiked(likedIds.contains(s.getId()));
                    dto.setFavorite(favoriteIds.contains(s.getId()));
                    return dto;
                }).toList(), pageable, cachedTrending.size());
    }

    private Pageable applyDefaultSortIfMissing(Pageable pageable) {
        if (pageable.getSort().isSorted()) return pageable;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private org.springframework.data.domain.Page<StoryDTO> mapStoriesToDTOs(
            org.springframework.data.domain.Page<Story> page, Set<Long> likedIds, Set<Long> favoriteIds) {
        return page.map(story -> {
            StoryDTO dto = new StoryDTO(story);
            dto.setLiked(likedIds.contains(story.getId()));
            dto.setFavorite(favoriteIds.contains(story.getId()));
            return dto;
        });
    }

    @Transactional
    public StoryDTO saveStory(Story story, MultipartFile coverImg) {
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not found"));
        story.setUser(user);

        Story savedStory = storyRepository.save(story);

        String coverImageUrl;
        if (coverImg != null && !coverImg.isEmpty()) {
            try {
                coverImageUrl = firebaseStorageService.uploadFile(coverImg, "thumbnails/" + savedStory.getId());
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }
        } else {
            coverImageUrl = getDefaultImageForGenre(savedStory.getGenres().getFirst());
        }

        savedStory.setCoverImageUrl(coverImageUrl);
        savedStory = storyRepository.save(savedStory);

        return new StoryDTO(savedStory);
    }

    @Transactional
    public StoryDTO updateStory(Long storyId, StoryDTO updatedStory, MultipartFile coverImg) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));

        Story existingStory = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!existingStory.getUser().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }

        if (existingStory.getStatus() == StoryStatus.PUBLISHED) {
            existingStory = draftService.ensureDraftExists(existingStory);
        }

        existingStory.setTitle(updatedStory.getTitle());
        existingStory.setDescription(updatedStory.getDescription());
        existingStory.setGenres(updatedStory.getGenres());
        existingStory.setTags(updatedStory.getTags());

        if (coverImg != null && !coverImg.isEmpty()) {
            try {
                String coverImageUrl = firebaseStorageService.uploadFile(coverImg, "thumbnails/" + existingStory.getId());
                existingStory.setCoverImageUrl(coverImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

        Story savedStory = storyRepository.save(existingStory);
        return new StoryDTO(savedStory);
    }

    @Transactional
    public StoryDTO publishStory(Long storyId) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        if (!story.getUser().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }

        if (story.getStatus() == StoryStatus.PUBLISHED) {
            throw new RuntimeException("Story already published");
        }

        story.setOriginalStory(null);
        story.setStatus(StoryStatus.PUBLISHED);

        story.touch();
        Story published = storyRepository.save(story);

        return new StoryDTO(published);
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

    public org.springframework.data.domain.Page<StoryDTO> getLikedStories(Pageable pageable) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findStoriesLikedByUserId(user.getId(), sortedPageable);

        Set<Long> likedIds = user.getLikedIds();
        Set<Long> favoriteIds = user.getFavoriteIds();

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    public org.springframework.data.domain.Page<StoryDTO> getFavoriteStories(Pageable pageable) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findStoriesFavoriteByUserId(user.getId(), sortedPageable);

        Set<Long> likedIds = user.getLikedIds();
        Set<Long> favoriteIds = user.getFavoriteIds();

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    public org.springframework.data.domain.Page<StoryDTO> getUserStories(Pageable pageable) {
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findByUserId(user.getId(), sortedPageable);

        Set<Long> likedIds = user.getLikedIds();
        Set<Long> favoriteIds = user.getFavoriteIds();

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return userRepository.findByUsername(username);
        }

        return Optional.empty();
    }

    private String getDefaultImageForGenre(Genre genre) {
        String FIREBASE_DEFAULTS = String.format("https://storage.googleapis.com/%s/defaults/",
                firebaseStorageService.getBucketName());
        return switch (genre) {
            case FANTASY -> FIREBASE_DEFAULTS.concat("fantasy.jpg");
            case SCIENCE_FICTION -> FIREBASE_DEFAULTS.concat("sci_fi.jpg");
            case HORROR -> FIREBASE_DEFAULTS.concat("horror.jpg");
            case MYSTERY -> FIREBASE_DEFAULTS.concat("mystery.jpg");
            case ADVENTURE -> FIREBASE_DEFAULTS.concat("adventure.jpg");
            case ROMANCE -> FIREBASE_DEFAULTS.concat("romance.jpg");
            case COMEDY -> FIREBASE_DEFAULTS.concat("comedy.jpg");
            case DRAMA -> FIREBASE_DEFAULTS.concat("drama.jpg");
            case THRILLER -> FIREBASE_DEFAULTS.concat("thriller.jpg");
            case POST_APOCALYPTIC -> FIREBASE_DEFAULTS.concat("post_apocalyptic.jpg");
            default -> FIREBASE_DEFAULTS.concat("default.jpg");
        };
    }
}
