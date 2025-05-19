package com.coursework.story.service;

import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseStorageServiceTest {

    private FirebaseStorageService storageService;

    private Bucket mockBucket;
    private Blob mockBlob;

    @BeforeEach
    void setUp() {
        storageService = new FirebaseStorageService();
        ReflectionTestUtils.setField(storageService, "storageBucket", "test-bucket");

        mockBucket = mock(Bucket.class);
        mockBlob = mock(Blob.class);
    }

    @Test
    void uploadFile_shouldUploadAndReturnUrl() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "content".getBytes());

        try (MockedStatic<StorageClient> mockedStorageClient = mockStatic(StorageClient.class)) {
            StorageClient storageClient = mock(StorageClient.class);
            mockedStorageClient.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(mockBucket);
            when(mockBucket.create(anyString(), any(byte[].class), anyString())).thenReturn(mockBlob);
            when(mockBlob.getName()).thenReturn("some/path/image.jpg");
            when(mockBucket.getName()).thenReturn("test-bucket");

            String result = storageService.uploadFile(file, "images");

            assertTrue(result.startsWith("https://storage.googleapis.com/test-bucket/"));
            verify(mockBlob).createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        }
    }

    @Test
    void extractBlobPath_shouldReturnCorrectPath() {
        String url = "https://storage.googleapis.com/test-bucket/images/pic.jpg";
        String result = storageService.extractBlobPath(url);
        assertEquals("images/pic.jpg", result);
    }

    @Test
    void extractBlobPath_shouldThrowForInvalidUrl() {
        String invalidUrl = "https://other-bucket.s3.amazonaws.com/images/pic.jpg";
        assertThrows(IllegalArgumentException.class, () -> storageService.extractBlobPath(invalidUrl));
    }

    @Test
    void deleteFile_shouldSkipDefaultImages() {
        try (MockedStatic<StorageClient> mockedStorageClient = mockStatic(StorageClient.class)) {
            storageService.deleteFile("default-avatar.png");
            mockedStorageClient.verifyNoInteractions();
        }
    }

    @Test
    void deleteFile_shouldDeleteBlob() {
        try (MockedStatic<StorageClient> mockedStorageClient = mockStatic(StorageClient.class)) {
            StorageClient storageClient = mock(StorageClient.class);
            mockedStorageClient.when(StorageClient::getInstance).thenReturn(storageClient);
            when(storageClient.bucket()).thenReturn(mockBucket);
            when(mockBucket.get("images/pic.jpg")).thenReturn(mockBlob);

            storageService.deleteFile("images/pic.jpg");

            verify(mockBlob).delete();
        }
    }

    @Test
    void getBucketName_shouldReturnConfiguredBucketName() {
        assertEquals("test-bucket", storageService.getBucketName());
    }
}