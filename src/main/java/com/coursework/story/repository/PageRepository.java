package com.coursework.story.repository;

import com.coursework.story.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findByStoryId(Long storyId);
}
