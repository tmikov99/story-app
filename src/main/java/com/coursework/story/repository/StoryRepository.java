package com.coursework.story.repository;

import com.coursework.story.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
}
