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
    List<Playthrough> findByUser(User user);
    Page<Playthrough> findByUserOrderByLastVisitedDesc(User user, Pageable pageable);
    void deleteByStory(Story story);
    long countByUserAndStory(User user, Story story);
    @Modifying
    @Query("UPDATE Playthrough p SET p.active = false WHERE p.user = :user AND p.story = :story AND p.active = true")
    void deactivatePlaythroughsForUserAndStory(@Param("user") User user, @Param("story") Story story);
}