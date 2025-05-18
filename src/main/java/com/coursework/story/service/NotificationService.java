package com.coursework.story.service;

import com.coursework.story.dto.NotificationDTO;
import com.coursework.story.model.Notification;
import com.coursework.story.model.NotificationType;
import com.coursework.story.model.User;
import com.coursework.story.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public NotificationService(NotificationRepository notificationRepository, AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    public void send(User recipient, String message, NotificationType type, Long targetId) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTargetId(targetId);
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getNotifications() {
        User user = authService.getAuthenticatedUserOrThrow();
        return user.getNotifications()
                .stream()
                .sorted(Comparator.comparing(Notification::getTimestamp).reversed())
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(List<Long> notificationIds) {
        User user = authService.getAuthenticatedUserOrThrow();
        notificationRepository.markAsReadByIds(notificationIds, user);
    }
}