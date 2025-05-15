package com.coursework.story.dto;

import com.coursework.story.model.Notification;
import com.coursework.story.model.NotificationType;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime timestamp;
    private NotificationType type;
    private Long targetId;

    public NotificationDTO() {}

    public NotificationDTO(Notification notification) {
        id = notification.getId();
        message = notification.getMessage();
        read = notification.isRead();
        timestamp = notification.getTimestamp();
        type = notification.getType();
        targetId = notification.getTargetId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
