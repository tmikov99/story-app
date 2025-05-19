package com.coursework.story.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);

        setField("domainUrl", "https://backend.test.com");
        setField("frontendUrl", "https://frontend.test.com");
    }

    private void setField(String fieldName, String value) {
        try {
            var field = EmailService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(emailService, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendVerificationEmail_shouldSendCorrectMessage() {
        String to = "user@example.com";
        String token = "verify-token-123";

        emailService.sendVerificationEmail(to, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals(to, message.getTo()[0]);
        assertEquals("Verify your email", message.getSubject());
        assertTrue(message.getText().contains("https://backend.test.com/api/auth/verify?token=" + token));
    }

    @Test
    void sendPasswordResetEmail_shouldSendCorrectMessage() {
        String to = "user@example.com";
        String token = "reset-token-456";

        emailService.sendPasswordResetEmail(to, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals(to, message.getTo()[0]);
        assertEquals("Reset Your Password", message.getSubject());
        assertTrue(message.getText().contains("https://frontend.test.com/reset-password?token=" + token));
    }
}