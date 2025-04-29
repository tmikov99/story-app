package com.coursework.story.controller;

import com.coursework.story.service.FirebaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FirebaseStorageService firebaseStorageService;

    public FileUploadController(FirebaseStorageService firebaseStorageService) {
        this.firebaseStorageService = firebaseStorageService;
    }

    @PostMapping("/upload-thumbnail")
    public ResponseEntity<Map<String, String>> uploadThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {

        try {
            String downloadUrl = firebaseStorageService.uploadFile(file, "thumbnails/" + userId);
            Map<String, String> response = Map.of("url", downloadUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed"));
        }
    }
}