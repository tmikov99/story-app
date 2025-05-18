package com.coursework.story.service;

import com.coursework.story.exception.InvalidTokenException;
import com.coursework.story.model.User;
import com.coursework.story.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return Optional.empty();
        }

        return userRepository.findByUsername(userDetails.getUsername());
    }

    public User getAuthenticatedUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("No authentication found.");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("User is anonymous (not authenticated).");
        }

        if (!authentication.isAuthenticated()) {
            throw new InsufficientAuthenticationException("User is not authenticated.");
        }

        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new InvalidTokenException("Invalid or expired token.");
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in database."));
    }
}