package com.coursework.story.service;

import com.coursework.story.dto.NotificationDTO;
import com.coursework.story.model.Notification;
import com.coursework.story.model.NotificationType;
import com.coursework.story.model.User;
import com.coursework.story.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private AuthService authService;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setNotifications(new ArrayList<>());
    }

    @Test
    void send_shouldSaveNotification() {
        NotificationType type = NotificationType.NEW_COMMENT;
        String message = "New comment";
        Long targetId = 123L;

        notificationService.send(user, message, type, targetId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(message, saved.getMessage());
        assertEquals(user, saved.getRecipient());
        assertEquals(type, saved.getType());
        assertEquals(targetId, saved.getTargetId());
    }

    @Test
    void getNotifications_shouldReturnSortedDTOs() {
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setMessage("First");
        n1.setTimestamp(LocalDateTime.now().minusHours(1));

        Notification n2 = new Notification();
        n2.setId(2L);
        n2.setMessage("Second");
        n2.setTimestamp(LocalDateTime.now());

        user.setNotifications(List.of(n1, n2));
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        List<NotificationDTO> result = notificationService.getNotifications();

        assertEquals(2, result.size());
        assertEquals("Second", result.get(0).getMessage());
        assertEquals("First", result.get(1).getMessage());
    }

    @Test
    void markAsRead_shouldCallRepository() {
        List<Long> ids = List.of(1L, 2L, 3L);
        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        notificationService.markAsRead(ids);

        verify(notificationRepository).markAsReadByIds(ids, user);
    }
}