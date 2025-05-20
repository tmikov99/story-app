package com.coursework.story.controller;

import com.coursework.story.dto.AuthRequest;
import com.coursework.story.dto.AuthResponse;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.InvalidTokenException;
import com.coursework.story.model.RefreshToken;
import com.coursework.story.model.User;
import com.coursework.story.security.JwtUtil;
import com.coursework.story.service.RefreshTokenService;
import com.coursework.story.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateAccessToken(request.getUsername());
        User user = userService.findByUsername(request.getUsername());
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email not verified");
        }
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response, @CookieValue("refreshToken") String refreshTokenValue) {
        if (refreshTokenValue != null) {
            refreshTokenService.deleteByToken(refreshTokenValue);
            Cookie cookie = new Cookie("refreshToken", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // expire now
            response.addCookie(cookie);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(value = "refreshToken", required = false)  String refreshTokenValue) {
        if (refreshTokenValue == null) {
            throw new InvalidTokenException("Missing refresh token cookie.");
        }

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        String accessToken = jwtUtil.generateAccessToken(refreshToken.getUser().getUsername());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getUser()));
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