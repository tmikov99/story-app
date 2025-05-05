package com.coursework.story.repository;

import com.coursework.story.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByStoryId(Long storyId);

    List<Comment> findByStoryIdOrderByCreatedAt(Long storyId);
    Page<Comment> findByUserId(Long userId, Pageable pageable);
}