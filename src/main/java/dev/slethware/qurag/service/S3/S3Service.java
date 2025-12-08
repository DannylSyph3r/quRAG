package dev.slethware.qurag.service.S3;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String filename);
    byte[] downloadFile(String fileKey);
}