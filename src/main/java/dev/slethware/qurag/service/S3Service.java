package dev.slethware.qurag.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String filename);
    byte[] downloadFile(String fileKey);
}