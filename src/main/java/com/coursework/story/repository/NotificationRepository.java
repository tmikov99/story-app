package com.coursework.story.repository;

import com.coursework.story.model.Notification;
import com.coursework.story.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id IN :ids AND n.recipient = :user")
    void markAsReadByIds(@Param("ids") List<Long> ids, @Param("user") User user);
}
