package com.coursework.story.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${app.domain.url}")
    private String domainUrl;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify Your Email Address";
        String verificationLink = domainUrl + "/api/auth/verify?token=" + token;
        String body = """
            Hi there,

            Thank you for signing up! Please verify your email address by clicking the link below:

            %s

            If you did not request this, you can safely ignore this email.

            Best regards,
            The ForkLore Team
            """.formatted(verificationLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String body = """
            Hi there,

            We received a request to reset your password. You can reset it by clicking the link below:

            %s

            If you did not request this, you can safely ignore this email.

            Stay safe,
            The ForkLore Team
            """.formatted(link);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset Your Password");
        message.setText(body);
        mailSender.send(message);
    }
}