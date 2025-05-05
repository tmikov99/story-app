package com.coursework.story.repository;

import com.coursework.story.model.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findAllByUserUsernameOrderByCreatedAt(String username);

    @Query("SELECT s FROM stories s LEFT JOIN s.tags t " +
            "WHERE LOWER(s.title) LIKE %:query% " +
            "OR LOWER(s.description) LIKE %:query% " +
            "OR LOWER(t) LIKE %:query%")
    List<Story> searchByQuery(@Param("query") String query);

    @Query("SELECT s FROM stories s " +
            "WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR EXISTS (SELECT t FROM s.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Story> searchByTitleOrTagsOrDescription(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM stories  s JOIN s.likedByUsers u WHERE u.id = :userId")
    Page<Story> findStoriesLikedByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM stories s JOIN s.favoriteByUsers u WHERE u.id = :userId")
    Page<Story> findStoriesFavoriteByUserId(@Param("userId") Long userId, Pageable pageable);

}
