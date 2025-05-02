package com.coursework.story.repository;

import com.coursework.story.model.Playthrough;
import com.coursework.story.model.Story;
import com.coursework.story.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaythroughRepository extends JpaRepository<Playthrough, Long> {
    Optional<Playthrough> findByUserAndStory(User user, Story story);
    List<Playthrough> findByUser(User user);
    List<Playthrough> findByUserOrderByLastVisitedDesc(User user);
}