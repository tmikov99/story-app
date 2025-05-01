package com.coursework.story.repository;

import com.coursework.story.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findAllByUserUsernameOrderByCreatedAt(String username);
}
