package com.coursework.story.service;

import com.coursework.story.dto.AuthRequest;
import com.coursework.story.dto.UserDTO;
import com.coursework.story.exception.BadRequestException;
import com.coursework.story.exception.NotFoundException;
import com.coursework.story.model.User;
import com.coursework.story.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock private FirebaseStorageService firebaseStorageService;
    @Mock private EmailService emailService;
    @Mock private AuthService authService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_success() {
        AuthRequest request = new AuthRequest("test_user", "password", "test@example.com", null);

        when(userRepository.existsByUsername("test_user")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        userService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("test_user", userCaptor.getValue().getUsername());
        assertFalse(userCaptor.getValue().isEmailVerified());
        verify(emailService).sendVerificationEmail(eq("test@example.com"), any());
    }

    @Test
    void registerUser_duplicateUsername_throwsException() {
        AuthRequest request = new AuthRequest("duplicate", "email@example.com", "pass", null);
        when(userRepository.existsByUsername("duplicate")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.registerUser(request));
    }

    @Test
    void forgottenPassword_success() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.forgottenPassword("test@example.com");

        assertNotNull(user.getResetToken());
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), any());
    }

    @Test
    void resetPassword_success() {
        User user = new User();
        user.setResetToken("valid-token");
        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(user));

        userService.resetPassword("valid-token", "newPassword");

        verify(userRepository).save(user);
        assertNull(user.getResetToken());
    }

    @Test
    void getUserResponse_success() {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setImageUrl("img.png");

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        UserDTO dto = userService.getUserResponse();

        assertEquals("user1", dto.getUsername());
        assertEquals("user1@example.com", dto.getEmail());
        assertEquals("img.png", dto.getImageUrl());
    }

    @Test
    void changePassword_success() {
        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("oldPass"));

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);

        userService.changePassword("oldPass", "newPass");

        verify(userRepository).save(user);
        assertTrue(new BCryptPasswordEncoder().matches("newPass", user.getPassword()));
    }

    @Test
    void findByUsername_userExists() {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        assertEquals(user, userService.findByUsername("john"));
    }

    @Test
    void findByUsername_notFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findByUsername("missing"));
    }

    @Test
    void setUserPicture_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        User user = new User();
        user.setId(1L);
        user.setImageUrl("old-url");

        when(authService.getAuthenticatedUserOrThrow()).thenReturn(user);
        when(firebaseStorageService.uploadFile(file, "profile_pics/1")).thenReturn("new-url");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO dto = userService.setUserPicture(file);

        verify(firebaseStorageService).deleteFile(any());
        assertEquals("new-url", dto.getImageUrl());
    }
}