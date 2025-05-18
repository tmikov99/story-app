package com.coursework.story.controller;

import com.coursework.story.dto.PasswordChangeRequest;
import com.coursework.story.dto.ResetPasswordRequest;
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

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(new UserDTO(userService.findByUsername(username)));
    }

    @PostMapping("/picture")
    public ResponseEntity<UserDTO> setPicture(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.setUserPicture(file));
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
            userService.forgottenPassword(email);
        return ResponseEntity.ok("Password reset link sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }
}
