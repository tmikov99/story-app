package com.coursework.story.repository;

import com.coursework.story.model.Playthrough;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaythroughRepository extends JpaRepository<Playthrough, Long> {
    List<Playthrough> findByUserAndStory(User user, Story story);
    List<Playthrough> findByUserAndStoryOrderByLastVisitedDesc(User user, Story story);
    Page<Playthrough> findByUser(User user, Pageable pageable);
    void deleteByStory(Story story);
    long countByUserAndStory(User user, Story story);
    boolean existsByCurrentPage(com.coursework.story.model.Page page);
    @Modifying
    @Query("UPDATE Playthrough p SET p.active = false WHERE p.user = :user AND p.story = :story AND p.active = true")
    void deactivatePlaythroughsForUserAndStory(@Param("user") User user, @Param("story") Story story);

    @Query("SELECT p FROM Playthrough p WHERE p.user = :user AND LOWER(p.story.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Playthrough> searchByUserAndStoryTitle(@Param("user") User user, @Param("query") String query, Pageable pageable);
}