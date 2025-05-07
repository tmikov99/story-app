package com.coursework.story.repository;

import com.coursework.story.model.Story;
import com.coursework.story.model.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findAllByUserUsernameOrderByCreatedAt(String username);

    Page<Story> findAllByStatus(StoryStatus status, Pageable pageable);

    @Query("SELECT s FROM stories s " +
            "WHERE s.status = 'PUBLISHED' AND (" +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR EXISTS (SELECT t FROM s.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Story> searchByTitleOrTagsOrDescription(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM stories  s JOIN s.likedByUsers u WHERE u.id = :userId AND s.status IN ('PUBLISHED', 'ARCHIVED')")
    Page<Story> findStoriesLikedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM stories s JOIN s.favoriteByUsers u WHERE u.id = :userId AND s.status IN ('PUBLISHED', 'ARCHIVED')")
    Page<Story> findStoriesFavoriteByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT s FROM stories s
            WHERE s.createdAt > :cutoff
            AND s.status = 'PUBLISHED'
            ORDER BY 
              (s.likes * 2 + s.favorites * 3 + s.reads * 1) / POWER(TIMESTAMPDIFF(HOUR, s.createdAt, CURRENT_TIMESTAMP) + 2, 1.5)
            DESC
            """)
    Page<Story> findTrendingStories(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    Page<Story> findByUserUsernameAndStatus(String username, StoryStatus status, Pageable pageable);

    Page<Story> findByUserId(Long userId, Pageable pageable);

    Optional<Story> findByOriginalStoryId (Long storyId);
}
