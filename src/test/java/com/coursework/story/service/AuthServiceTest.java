package com.coursework.story.service;

import com.coursework.story.exception.InvalidTokenException;
import com.coursework.story.model.User;
import com.coursework.story.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedUser_shouldReturnUserIfAuthenticated() {
        User user = new User();
        user.setUsername("test_user");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test_user");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(user));

        Optional<User> result = authService.getAuthenticatedUser();
        assertTrue(result.isPresent());
        assertEquals("test_user", result.get().getUsername());
    }

    @Test
    void getAuthenticatedUser_shouldReturnEmptyIfNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(auth);

        Optional<User> result = authService.getAuthenticatedUser();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAuthenticatedUser_shouldReturnEmptyIfPrincipalNotUserDetails() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("not-a-userdetails");
        when(securityContext.getAuthentication()).thenReturn(auth);

        Optional<User> result = authService.getAuthenticatedUser();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAuthenticatedUser_shouldReturnEmptyIfAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);
        Optional<User> result = authService.getAuthenticatedUser();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAuthenticatedUserOrThrow_shouldReturnUserIfValid() {
        User user = new User();
        user.setUsername("test_user");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test_user");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(user));

        User result = authService.getAuthenticatedUserOrThrow();
        assertEquals("test_user", result.getUsername());
    }

    @Test
    void getAuthenticatedUserOrThrow_shouldThrowIfAuthenticationNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> authService.getAuthenticatedUserOrThrow());
    }

    @Test
    void getAuthenticatedUserOrThrow_shouldThrowIfAnonymousToken() {
        Authentication auth = mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> authService.getAuthenticatedUserOrThrow());
    }

    @Test
    void getAuthenticatedUserOrThrow_shouldThrowIfNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertThrows(InsufficientAuthenticationException.class,
                () -> authService.getAuthenticatedUserOrThrow());
    }

    @Test
    void getAuthenticatedUserOrThrow_shouldThrowIfPrincipalInvalid() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("invalid");
        when(securityContext.getAuthentication()).thenReturn(auth);

        assertThrows(InvalidTokenException.class,
                () -> authService.getAuthenticatedUserOrThrow());
    }

    @Test
    void getAuthenticatedUserOrThrow_shouldThrowIfUserNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test_user");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.getAuthenticatedUserOrThrow());
    }
}