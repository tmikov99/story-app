package com.coursework.story.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.http.HttpMethod.*;

@Configuration
public class SecurityConfig {


    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        //Non-authenticated
                        .requestMatchers(POST, "/api/auth/login").permitAll()
                        .requestMatchers(POST, "/api/auth/logout").permitAll()
                        .requestMatchers(POST, "/api/auth/register").permitAll()
                        .requestMatchers(GET, "/api/auth/refresh").permitAll()
                        .requestMatchers(GET, "/api/auth/verify").permitAll()

                        .requestMatchers(GET, "/api/user/{username}").permitAll()
                        .requestMatchers(POST, "/api/user/forgot-password").permitAll()
                        .requestMatchers(POST, "/api/user/reset-password").permitAll()

                        .requestMatchers(GET, "/api/story/genres").permitAll()
                        .requestMatchers(GET, "/api/comments/story/{storyId}").permitAll()

                        //Fully authenticated
                        .requestMatchers(GET, "/api/user").authenticated()
                        .requestMatchers(POST, "/api/user/picture").authenticated()
                        .requestMatchers(PUT, "/api/user/password").authenticated()

                        .requestMatchers(GET, "/api/story/liked").authenticated()
                        .requestMatchers(GET, "/api/story/favorite").authenticated()
                        .requestMatchers(GET, "/api/story/mine").authenticated()
                        .requestMatchers(POST, "/api/story/create").authenticated()
                        .requestMatchers(PUT, "/api/story/update").authenticated()
                        .requestMatchers(PATCH, "/api/story/{storyId}/start-page").authenticated()
                        .requestMatchers(POST, "/api/story/copyAsDraft/{storyId}").authenticated()
                        .requestMatchers(PUT, "/api/story/publish/{storyId}").authenticated()
                        .requestMatchers(PUT, "/api/story/archive/{storyId}").authenticated()
                        .requestMatchers(DELETE, "/api/story/{storyId}").authenticated()
                        .requestMatchers(POST, "/api/story/like/{storyId}").authenticated()
                        .requestMatchers(POST, "/api/story/favorite/{storyId}").authenticated()
                        .requestMatchers(GET, "/api/story/{storyId}/items").authenticated()
                        .requestMatchers(POST, "/api/story/{storyId}/items").authenticated()
                        .requestMatchers(PUT, "api/story/{storyId}/items/{itemId}").authenticated()
                        .requestMatchers(DELETE, "api/story/{storyId}/items/{itemId}").authenticated()

                        .requestMatchers(POST, "/api/playthrough/start/{storyId}").authenticated()
                        .requestMatchers(PATCH, "/api/playthrough/{playthroughId}/choose/{nextPage}").authenticated()
                        .requestMatchers(GET, "/api/playthrough/{playthroughId}").authenticated()
                        .requestMatchers(GET, "/api/playthrough/story/{storyId}").authenticated()
                        .requestMatchers(GET, "/api/playthrough/{playthroughId}/currentPage").authenticated()
                        .requestMatchers(POST, "/api/playthrough/{playthroughId}/load").authenticated()
                        .requestMatchers(GET, "/api/playthrough").authenticated()
                        .requestMatchers(DELETE, "/api/playthrough/{playthroughId}").authenticated()
                        .requestMatchers(GET, "/api/playthrough/{playthroughId}/testLuck").authenticated()
                        .requestMatchers(POST, "/api/playthrough/{playthroughId}/battle/start").authenticated()
                        .requestMatchers(POST, "/api/playthrough/{playthroughId}/battle/play").authenticated()
                        .requestMatchers(POST, "/api/playthrough/{playthroughId}/battle/luck").authenticated()
                        .requestMatchers(POST, "/api/playthrough/{playthroughId}/battle/continue").authenticated()
                        .requestMatchers(POST, "/api/playthrough/{playthroughId}/battle/finish").authenticated()
                        .requestMatchers(GET, "/api/playthrough/{playthroughId}/battle").authenticated()

                        .requestMatchers(PUT, "/api/page/{pageId}").authenticated()
                        .requestMatchers(POST, "/api/page/create").authenticated()
                        .requestMatchers(DELETE, "/api/page/{pageId}").authenticated()

                        .requestMatchers(GET, "/api/notifications").authenticated()
                        .requestMatchers(PUT, "/api/notifications/read").authenticated()

                        .requestMatchers(POST, "/api/comments/story/{storyId}").authenticated()
                        .requestMatchers(DELETE, "/api/comments/{commentId}").authenticated()
                        .requestMatchers(GET, "/api/comments/mine").authenticated()

                        //Optional authentication
                        .requestMatchers(GET, "/api/story").permitAll()
                        .requestMatchers(GET, "/api/story/trending").permitAll()
                        .requestMatchers(GET, "/api/story/{storyId}").permitAll()
                        .requestMatchers(GET, "/api/story/preview/{storyId}").permitAll()
                        .requestMatchers(GET, "/api/story/user/{username}").permitAll()
                        .requestMatchers(GET, "/api/story/user/{username}/stories").permitAll()

                        .requestMatchers(GET, "/api/page/{pageId}").permitAll()
                        .requestMatchers(GET, "/api/page/{storyId}/page/{pageNumber}").permitAll()
                        .requestMatchers(GET, "/api/page/story/{storyId}").permitAll()
                        .requestMatchers(GET, "/api/page/story/{storyId}/map").permitAll()

                        //Default fallback
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}