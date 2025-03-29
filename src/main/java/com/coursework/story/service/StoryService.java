package com.coursework.story.service;

import com.coursework.story.dto.StoryDTO;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import com.coursework.story.repository.StoryRepository;
import com.coursework.story.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    public StoryService(StoryRepository storyRepository, UserRepository userRepository) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
    }

    public List<StoryDTO> getStories() {
        List<Story> stories = storyRepository.findAll();
        List<StoryDTO> storyDTOs = new ArrayList<>();
        for (Story story : stories) {
            storyDTOs.add(new StoryDTO(story.getId(), story.getTitle(), story.getPages(), story.getUser().getId()));
        }
        return storyDTOs;
    }

    @Transactional
    public Story saveStory(Story story) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        story.setUser(user);

        return storyRepository.save(story);
    }
}
