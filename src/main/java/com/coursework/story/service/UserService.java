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

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, FirebaseStorageService firebaseStorageService) {
        this.userRepository = userRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void registerUser(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists! Choose a different one.");
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

        userRepository.save(user);
    }

    public UserDTO getUserResponse() {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getUsername(), user.getEmail(), user.getImageUrl());
    }

    public UserDTO setUserPicture(MultipartFile file) {
        User user = getAuthenticatedUser().orElseThrow(() -> new RuntimeException("User not found"));
        String imgUrl = null;
        try {
            imgUrl = firebaseStorageService.uploadFile(file, "thumbnails/" + user.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        user.setImageUrl(imgUrl);

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
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