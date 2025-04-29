package com.coursework.story.controller;

import com.coursework.story.dto.NotificationDTO;
import com.coursework.story.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotifications());
    }

    @PutMapping("/read")
    public void markReadNotifications(@RequestBody List<Long> notificationIds) {
        notificationService.markAsRead(notificationIds);
    }
}
