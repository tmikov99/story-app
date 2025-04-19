package com.coursework.story.repository;

import com.coursework.story.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findByStoryId(Long storyId);

    Optional<Page> findByStoryIdAndPageNumber(Long storyId, int pageNumber);

    List<Page> findAllByStoryIdOrderByPageNumber(Long storyId);

    List<Page> findAllByStoryId(Long storyId);

    boolean existsByStoryIdAndPageNumber(Long storyId, int pageNumber);

}
