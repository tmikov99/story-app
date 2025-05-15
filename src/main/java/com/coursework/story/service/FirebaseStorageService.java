package com.coursework.story.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService {
    @Value("${story.app.firebaseStorageBucket}")
    private String storageBucket;

    public String getBucketName() {
        return storageBucket;
    }

    public String uploadFile(MultipartFile file, String path) throws IOException {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String blobPath = path + "/" + fileName;

        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(blobPath, file.getBytes(), file.getContentType());

        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), blob.getName());
    }

    public String extractBlobPath(String publicUrl) {
        String prefix = "https://storage.googleapis.com/" + storageBucket + "/";
        if (publicUrl.startsWith(prefix)) {
            return publicUrl.substring(prefix.length());
        }
        throw new IllegalArgumentException("Invalid Firebase Storage URL: " + publicUrl);
    }

    public void deleteFile(String blobPath) {
        if (blobPath.toLowerCase().contains("default")) {
            return;
        }

        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(blobPath);
        blob.delete();
    }
}