package com.coursework.story.service;

import com.coursework.story.dto.PlaythroughDTO;
import com.coursework.story.model.Page;
import com.coursework.story.model.Playthrough;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.PageRepository;
import com.coursework.story.repository.PlaythroughRepository;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

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

    public PlaythroughDTO startPlaythrough(Long storyId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));

        Optional<Playthrough> existingPlaythrough = playthroughRepository.findByUserAndStory(user, story);
        if (existingPlaythrough.isPresent()) {
            return new PlaythroughDTO(existingPlaythrough.get());
        }

        Playthrough playthrough = new Playthrough();
        playthrough.setUser(user);
        playthrough.setStory(story);
        playthrough.setCurrentPage(story.getPages().getFirst());
        playthrough.setPath(Collections.singletonList(story.getPages().getFirst().getPageNumber()));
        playthrough.setCompleted(false);

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);

        return new PlaythroughDTO(savedPlaythrough);
    }

    public PlaythroughDTO updatePlaythrough(Long storyId, int pageNumber) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        Page nextPage = pageRepository.findByStoryIdAndPageNumber(storyId, pageNumber).orElseThrow(() -> new RuntimeException("Page not found"));

        Playthrough playthrough = playthroughRepository.findByUserAndStory(user, story)
                .orElseThrow(() -> new RuntimeException("Playthrough not found"));

        playthrough.setCurrentPage(nextPage);
        playthrough.getPath().add(pageNumber);

        if (nextPage.isEndPage()) {
            playthrough.setCompleted(true);
        }

        Playthrough savedPlaythrough = playthroughRepository.save(playthrough);

        return new PlaythroughDTO(savedPlaythrough);
    }

    public PlaythroughDTO getPlaythrough(Long storyId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        Playthrough playthrough = playthroughRepository.findByUserAndStory(user, story).orElseThrow(() -> new RuntimeException("Playthrough not found"));

        return new PlaythroughDTO(playthrough);
    }
}