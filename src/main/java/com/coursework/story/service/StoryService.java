package com.coursework.story.service;

import com.coursework.story.dto.PageDTO;
import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.Page;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
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

    public StoryService(StoryRepository storyRepository, UserRepository userRepository, PageRepository pageRepository) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.pageRepository = pageRepository;
    }

    public List<StoryDTO> getStories() {
        List<Story> stories = storyRepository.findAll();
        List<StoryDTO> storyDTOs = new ArrayList<>();
        for (Story story : stories) {
            storyDTOs.add(new StoryDTO(story));
        }
        return storyDTOs;
    }

    public StoryDTO getStoryById(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        return new StoryDTO(story, story.getPages());
    }

    public StoryDTO getStoryPreviewById(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        return new StoryDTO(story);
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
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        story.setUser(user);

        Story savedStory = storyRepository.save(story);
        return new StoryDTO(savedStory);
    }
}
