package com.coursework.story.controller;

import com.coursework.story.dto.AuthRequest;
import com.coursework.story.dto.AuthResponse;
import com.coursework.story.model.User;
import com.coursework.story.service.AuthService;
import com.coursework.story.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request.getUsername(), request.getPassword(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, @CookieValue("refreshToken") String refreshTokenValue) {
        authService.logout(response, refreshTokenValue);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(value = "refreshToken", required = false)  String refreshTokenValue) {
        AuthResponse authResponse = authService.refreshToken(refreshTokenValue);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        Optional<User> userOpt = userService.verifyUser(token);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok("Email verified!");
        }
        return ResponseEntity.badRequest().body("Invalid or expired token");
    }
}