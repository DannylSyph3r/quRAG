package dev.slethware.qurag.service;

import dev.slethware.qurag.dto.response.DocumentDetailResponse;
import dev.slethware.qurag.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse uploadDocument(MultipartFile file);
    List<DocumentResponse> getAllDocuments();
    DocumentDetailResponse getDocumentById(UUID documentId);
}