package com.coursework.story.service;

import com.coursework.story.dto.AuthRequest;
import com.coursework.story.dto.UserDTO;
import com.coursework.story.model.User;
import com.coursework.story.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, FirebaseStorageService firebaseStorageService, EmailService emailService) {
        this.userRepository = userRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailService = emailService;
    }

    public void registerUser(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists! Choose a different one.");
        }
        if (userRepository.existsByEmail(request.getUsername())) {
            throw new RuntimeException("Email already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getImageUrl() != null) {
            user.setImageUrl(request.getImageUrl());
        }

        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        //TODO: Remove after testing
        System.out.println("Verification token: " + token);
        user.setEmailVerified(false);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public Optional<User> verifyUser(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public void forgottenPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email not found");
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        //TODO: Remove after testing
        System.out.println("Reset token: " + token);
        userRepository.save(user);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired token");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    public UserDTO getUserResponse() {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getUsername(), user.getEmail(), user.getImageUrl());
    }

    public UserDTO setUserPicture(MultipartFile file) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        String oldImageUrl = user.getImageUrl();
        String imgUrl = null;
        try {
            imgUrl = firebaseStorageService.uploadFile(file, "profile_pics/" + user.getId());
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
        user.setImageUrl(imgUrl);

        if (oldImageUrl != null) {
            String firebasePath = firebaseStorageService.extractBlobPath(oldImageUrl);
            firebaseStorageService.deleteFile(firebasePath);
        }

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public void changePassword(String currentPassword, String newPassword) {
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return userRepository.findByUsername(username);
        }

        return Optional.empty();
    }
}