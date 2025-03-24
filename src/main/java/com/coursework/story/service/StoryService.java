package com.coursework.story.service;

import com.coursework.story.model.Story;
import com.coursework.story.repositories.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    public List<Story> getStories() {
        return storyRepository.findAll();
    }
}
