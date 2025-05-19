package com.coursework.story.service;

import com.coursework.story.dto.LikeResponse;
import com.coursework.story.dto.PaginatedResponse;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.exception.StoryValidationException;
import com.coursework.story.exception.UnauthorizedException;
import com.coursework.story.model.*;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final PlaythroughRepository playthroughRepository;
    private final NotificationService notificationService;
    private final FirebaseStorageService firebaseStorageService;
    private final DraftService draftService;
    private final AuthService authService;

    public StoryService(StoryRepository storyRepository, UserRepository userRepository,
                        PlaythroughRepository playthroughRepository, NotificationService notificationService,
                        FirebaseStorageService firebaseStorageService,
                        DraftService draftService, AuthService authService) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.playthroughRepository = playthroughRepository;
        this.notificationService = notificationService;
        this.firebaseStorageService = firebaseStorageService;
        this.draftService = draftService;
        this.authService = authService;
    }

    public List<StoryDTO> getStories() {
        Optional<User> user = authService.getAuthenticatedUser();
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
                .orElseThrow(() -> new NotFoundException("Story not found"));
        return new StoryDTO(story, story.getPages());
    }

    public StoryDTO getStoryPreviewById(Long storyId) {
        Optional<User> user = authService.getAuthenticatedUser();
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (story.getStatus() == StoryStatus.DRAFT) {
            if (user.isEmpty() || !story.getUser().getId().equals(user.get().getId())) {
                throw new UnauthorizedException("Unauthorized access to draft story");
            }
        }

        boolean isLiked = user.isPresent() && user.get().getLikedStories().contains(story);
        boolean isFavorite = user.isPresent() && user.get().getFavoriteStories().contains(story);

        return new StoryDTO(story, isLiked, isFavorite);
    }

    public List<StoryDTO> getStoriesByUser(String username) {
        Optional<User> user = authService.getAuthenticatedUser();
        Set<Long> likedIds = user.isPresent() ? user.get().getLikedIds() : Collections.emptySet();
        Set<Long> favoriteIds = user.isPresent() ? user.get().getFavoriteIds() : Collections.emptySet();
        return storyRepository.findAllByUserUsernameOrderByCreatedAt(username).stream()
                .filter(story -> story.getStatus() == StoryStatus.PUBLISHED)
                .map(story -> {
                    StoryDTO dto = new StoryDTO(story);
                    dto.setLiked(likedIds.contains(story.getId()));
                    dto.setFavorite(favoriteIds.contains(story.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<StoryDTO> searchStories(String query, Pageable pageable) {
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        Optional<User> user = authService.getAuthenticatedUser();
        Set<Long> likedIds = user.map(User::getLikedIds).orElse(Collections.emptySet());
        Set<Long> favoriteIds = user.map(User::getFavoriteIds).orElse(Collections.emptySet());

        org.springframework.data.domain.Page<Story> page = storyRepository.searchByTitleOrTagsOrDescription(query, sortedPageable);

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    public org.springframework.data.domain.Page<StoryDTO> getAllStories(Pageable pageable) {
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findAllByStatus(StoryStatus.PUBLISHED, sortedPageable);

        Optional<User> user = authService.getAuthenticatedUser();
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

        Optional<User> user = authService.getAuthenticatedUser();
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

    public PaginatedResponse<StoryDTO> getPublishedStoriesByUser(String username, Pageable pageable) {
        Optional<User> currentUser = authService.getAuthenticatedUser();
        Set<Long> likedIds = currentUser.map(User::getLikedIds).orElse(Collections.emptySet());
        Set<Long> favoriteIds = currentUser.map(User::getFavoriteIds).orElse(Collections.emptySet());

        org.springframework.data.domain.Page<Story> stories = storyRepository.findByUserUsernameAndStatus(username, StoryStatus.PUBLISHED, pageable);

        return PaginatedResponse.fromPage(mapStoriesToDTOs(stories, likedIds, favoriteIds));
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
        User user = authService.getAuthenticatedUserOrThrow();
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
        User user = authService.getAuthenticatedUserOrThrow();

        Story existingStory = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (!existingStory.getUser().equals(user)) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (existingStory.getStatus() == StoryStatus.PUBLISHED) {
            throw new BadRequestException("Cannot edit a published story. Please create a draft copy first.");
        }

        existingStory.setTitle(updatedStory.getTitle());
        existingStory.setDescription(updatedStory.getDescription());
        existingStory.setGenres(updatedStory.getGenres());
        existingStory.setTags(updatedStory.getTags());

        if (coverImg != null && !coverImg.isEmpty()) {
            String oldImageUrl = existingStory.getCoverImageUrl();
            try {
                String coverImageUrl = firebaseStorageService.uploadFile(coverImg, "thumbnails/" + existingStory.getId());
                existingStory.setCoverImageUrl(coverImageUrl);

                if (oldImageUrl != null && !oldImageUrl.toLowerCase().contains("default")) {
                    long count = storyRepository.countByCoverImageUrlExcludingStory(oldImageUrl, existingStory.getId());
                    if (count == 0) {
                        String firebasePath = firebaseStorageService.extractBlobPath(oldImageUrl);
                        firebaseStorageService.deleteFile(firebasePath);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

        Story savedStory = storyRepository.save(existingStory);
        return new StoryDTO(savedStory);
    }

    @Transactional
    public int updateStartPageNumber(Long storyId, int startPageNumber) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (!story.getUser().equals(user)) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (story.getStatus() == StoryStatus.PUBLISHED) {
            throw new BadRequestException("Cannot modify a published story.");
        }

        List<Page> pages = story.getPages();
        if (pages == null || pages.isEmpty()) {
            throw new BadRequestException("Story must have at least one page.");
        }

        boolean pageExists = pages.stream()
                .anyMatch(page -> page.getPageNumber() == startPageNumber);
        if (!pageExists) {
            throw new BadRequestException("Start page number must match one of the story's page numbers.");
        }

        story.setStartPageNumber(startPageNumber);
        return storyRepository.save(story).getStartPageNumber();
    }

    @Transactional
    public StoryDTO copyStoryAsDraft(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story original = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Original story not found"));

        if (!original.getUser().equals(user)) {
            throw new UnauthorizedException("Unauthorized to copy this story");
        }

        if (original.getStatus() == StoryStatus.DRAFT) {
            throw new BadRequestException("Only published stories can be copied");
        }

        Story draft = draftService.createDraftFromPublished(original, user);

        return new StoryDTO(draft);
    }

    @Transactional
    public void deleteStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (!story.getUser().equals(user)) {
            throw new UnauthorizedException("Unauthorized to delete this story");
        }

        if (story.getLikedByUsers() != null) {
            for (User u : story.getLikedByUsers()) {
                u.getLikedStories().remove(story);
            }
        }

        if (story.getFavoriteByUsers() != null) {
            for (User u : story.getFavoriteByUsers()) {
                u.getFavoriteStories().remove(story);
            }
        }

        for (Story draft : story.getDrafts()) {
            draft.setOriginalStory(null);
        }
        storyRepository.saveAll(story.getDrafts());

        String imageUrl = story.getCoverImageUrl();
        if (imageUrl != null && !imageUrl.toLowerCase().contains("default")) {
            long count = storyRepository.countByCoverImageUrlExcludingStory(imageUrl, story.getId());
            if (count == 0) {
                String firebasePath = firebaseStorageService.extractBlobPath(imageUrl);
                firebaseStorageService.deleteFile(firebasePath);
            }
        }

        playthroughRepository.deleteByStory(story);
        storyRepository.delete(story);
    }

    @Transactional
    public StoryDTO publishStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (!story.getUser().equals(user)) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (story.getStatus() == StoryStatus.PUBLISHED) {
            throw new BadRequestException("Story already published");
        }

        validateStoryBeforePublish(story);

        story.setOriginalStory(null);
        story.setStatus(StoryStatus.PUBLISHED);

        Story published = storyRepository.save(story);

        return new StoryDTO(published);
    }

    @Transactional
    public StoryDTO archiveStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

        if (!story.getUser().equals(user)) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (story.getStatus() != StoryStatus.PUBLISHED) {
            throw new BadRequestException("Story needs to be published before being archived");
        }

        story.setStatus(StoryStatus.ARCHIVED);

        Story published = storyRepository.save(story);

        return new StoryDTO(published);
    }

    @Transactional
    public LikeResponse toggleLikeStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

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
                        "🎉 Your story \"" + story.getTitle() + "\" just reached 10 likes!",
                        NotificationType.ACHIEVED_VIEWS,
                        storyId
                );
            }
        }

        userRepository.save(user);
        Story savedStory = storyRepository.save(story);
        return new LikeResponse(response, savedStory.getLikes());
    }

    @Transactional
    public boolean toggleFavoriteStory(Long storyId) {
        User user = authService.getAuthenticatedUserOrThrow();

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("Story not found"));

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
        User user = authService.getAuthenticatedUserOrThrow();
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findStoriesLikedByUserId(user.getId(), sortedPageable);

        Set<Long> likedIds = user.getLikedIds();
        Set<Long> favoriteIds = user.getFavoriteIds();

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    public org.springframework.data.domain.Page<StoryDTO> getFavoriteStories(Pageable pageable) {
        User user = authService.getAuthenticatedUserOrThrow();
        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findStoriesFavoriteByUserId(user.getId(), sortedPageable);

        Set<Long> likedIds = user.getLikedIds();
        Set<Long> favoriteIds = user.getFavoriteIds();

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
    }

    public org.springframework.data.domain.Page<StoryDTO> getUserStories(Pageable pageable) {
        User user = authService.getAuthenticatedUserOrThrow();

        Pageable sortedPageable = applyDefaultSortIfMissing(pageable);

        org.springframework.data.domain.Page<Story> page = storyRepository.findByUserId(user.getId(), sortedPageable);

        Set<Long> likedIds = user.getLikedIds();
        Set<Long> favoriteIds = user.getFavoriteIds();

        return mapStoriesToDTOs(page, likedIds, favoriteIds);
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

    private void validateStoryBeforePublish(Story story) {
        List<String> errors = new ArrayList<>();

        List<Page> pages = story.getPages();
        if (pages == null || pages.isEmpty()) {
            errors.add("A story must have at least one page.");
            throw new StoryValidationException(errors);
        }

        Set<Integer> pageNumbers = pages.stream()
                .map(Page::getPageNumber)
                .collect(Collectors.toSet());

        Integer startPage = story.getStartPageNumber();

        if (startPage == null) {
            errors.add("Start page must be set before publishing.");
        } else {
            if (!pageNumbers.contains(startPage)) {
                errors.add("Start page must be one of the story's pages.");
            }
        }

        Map<Integer, Long> pageNumberCounts = pages.stream()
                .collect(Collectors.groupingBy(Page::getPageNumber, Collectors.counting()));

        List<Integer> duplicates = pageNumberCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicates.isEmpty()) {
            errors.add("Duplicate page numbers found: " + duplicates);
        }

        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (Page page : pages) {
            int from = page.getPageNumber();
            graph.putIfAbsent(from, new ArrayList<>());
            for (Choice choice : page.getChoices()) {
                int to = choice.getTargetPage();
                if (!pageNumbers.contains(to)) {
                    errors.add("Choice on page " + from + " points to a non-existent page: " + to);
                }
                graph.get(from).add(to);
            }
        }

        if (startPage != null && pageNumbers.contains(startPage)) {
            Set<Integer> visited = new HashSet<>();
            Queue<Integer> queue = new LinkedList<>();
            queue.add(startPage);
            visited.add(startPage);

            while (!queue.isEmpty()) {
                int current = queue.poll();
                for (int neighbor : graph.getOrDefault(current, List.of())) {
                    if (visited.add(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }

            Set<Integer> unreachable = new HashSet<>(pageNumbers);
            unreachable.removeAll(visited);
            if (!unreachable.isEmpty()) {
                errors.add("The following pages are unreachable from the start page: " + unreachable);
            }
        }

        if (!errors.isEmpty()) {
            throw new StoryValidationException(errors);
        }
    }
}
