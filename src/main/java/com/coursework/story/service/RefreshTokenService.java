package com.coursework.story.service;

import com.coursework.story.model.RefreshToken;
import com.coursework.story.model.User;
import com.coursework.story.repository.RefreshTokenRepository;
import com.coursework.story.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshToken(user.getUsername()));
        refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)); // 7 days
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().after(new Date()));
    }
}