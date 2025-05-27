package com.coursework.story.repository;

import com.coursework.story.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStoryId(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM page_granted_items WHERE item_id = :itemId", nativeQuery = true)
    void removeFromGrantedPages(@Param("itemId") Long itemId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM page_removed_items WHERE item_id = :itemId", nativeQuery = true)
    void removeFromRemovedPages(@Param("itemId") Long itemId);
}
