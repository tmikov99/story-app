package com.coursework.story.service;

import com.coursework.story.model.User;
import com.coursework.story.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Test
    void loadUserByUsername_success() {
        User user = new User();
        user.setUsername("test_user");
        user.setPassword("pass");

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(user));

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);
        UserDetails details = service.loadUserByUsername("test_user");

        assertEquals("test_user", details.getUsername());
        assertEquals("pass", details.getPassword());
    }

    @Test
    void loadUserByUsername_notFound() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername("not_found")).thenReturn(Optional.empty());

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("not_found"));
    }
}