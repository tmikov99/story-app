package com.coursework.story.controller;

import com.coursework.story.dto.UserDTO;
import com.coursework.story.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getUser() {
        return ResponseEntity.ok(userService.getUserResponse());
    }

    @PostMapping("/picture")
    public ResponseEntity<UserDTO> setPicture(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.setUserPicture(file));
    }

    @PutMapping("/password")
    public ResponseEntity<Boolean> updatePassword() {
        return ResponseEntity.ok(true);
    }
}
