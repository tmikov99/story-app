package com.coursework.story.service;

import com.coursework.story.dto.AuthResponse;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.InvalidTokenException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.model.RefreshToken;
import com.coursework.story.model.User;
import com.coursework.story.repository.UserRepository;
import com.coursework.story.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;


    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse login(String username, String password, HttpServletResponse response) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email not verified");
        }

        String token = jwtUtil.generateAccessToken(username);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return new AuthResponse(token, user);
    }

    public void logout(HttpServletResponse response, String refreshTokenValue) {
        if (refreshTokenValue != null) {
            refreshTokenService.deleteByToken(refreshTokenValue);
            Cookie cookie = new Cookie("refreshToken", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        if (refreshTokenValue == null) {
            throw new InvalidTokenException("Missing refresh token cookie.");
        }

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        String accessToken = jwtUtil.generateAccessToken(refreshToken.getUser().getUsername());
        return new AuthResponse(accessToken, refreshToken.getUser());
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