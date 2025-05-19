package com.coursework.story.service;

import com.coursework.story.model.RefreshToken;
import com.coursework.story.model.User;
import com.coursework.story.repository.RefreshTokenRepository;
import com.coursework.story.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private RefreshTokenService refreshTokenService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_user");
    }

    @Test
    void createRefreshToken_newToken() {
        String generatedToken = "new.refresh.token";

        when(refreshTokenRepository.findByUser(mockUser)).thenReturn(Optional.empty());
        when(jwtUtil.generateRefreshToken("test_user")).thenReturn(generatedToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(mockUser);

        assertEquals(mockUser, token.getUser());
        assertEquals(generatedToken, token.getToken());
        assertTrue(token.getExpiryDate().after(new Date()));
    }

    @Test
    void createRefreshToken_existingToken() {
        RefreshToken existingToken = new RefreshToken();
        existingToken.setUser(mockUser);
        existingToken.setToken("old.token");
        existingToken.setExpiryDate(new Date());

        String newToken = "updated.token";

        when(refreshTokenRepository.findByUser(mockUser)).thenReturn(Optional.of(existingToken));
        when(jwtUtil.generateRefreshToken("test_user")).thenReturn(newToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshToken updatedToken = refreshTokenService.createRefreshToken(mockUser);

        assertEquals(newToken, updatedToken.getToken());
        assertEquals(mockUser, updatedToken.getUser());
        assertTrue(updatedToken.getExpiryDate().after(new Date()));
    }

    @Test
    void verifyRefreshToken_valid() {
        RefreshToken validToken = new RefreshToken();
        validToken.setToken("valid.token");
        validToken.setExpiryDate(new Date(System.currentTimeMillis() + 100000)); // not expired

        when(refreshTokenRepository.findByToken("valid.token")).thenReturn(Optional.of(validToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid.token");

        assertEquals(validToken, result);
    }

    @Test
    void verifyRefreshToken_expired() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired.token");
        expiredToken.setExpiryDate(new Date(System.currentTimeMillis() - 100000)); // expired

        when(refreshTokenRepository.findByToken("expired.token")).thenReturn(Optional.of(expiredToken));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.verifyRefreshToken("expired.token"));

        assertEquals("Invalid or expired refresh token", ex.getMessage());
    }

    @Test
    void verifyRefreshToken_notFound() {
        when(refreshTokenRepository.findByToken("missing.token")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.verifyRefreshToken("missing.token"));

        assertEquals("Invalid or expired refresh token", ex.getMessage());
    }
}