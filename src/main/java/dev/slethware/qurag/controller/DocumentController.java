package dev.slethware.qurag.controller;

import dev.slethware.qurag.dto.response.ApiResponse;
import dev.slethware.qurag.dto.response.DocumentDetailResponse;
import dev.slethware.qurag.dto.response.DocumentResponse;
import dev.slethware.qurag.service.DocumentService;
import dev.slethware.qurag.utility.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response = documentService.uploadDocument(file);

        return new ResponseEntity<>(
                ApiResponseUtil.created("Document uploaded and processed successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {
        List<DocumentResponse> documents = documentService.getAllDocuments();

        return new ResponseEntity<>(
                ApiResponseUtil.successFull("Documents retrieved successfully", documents),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocumentById(
            @PathVariable UUID id) {

        DocumentDetailResponse response = documentService.getDocumentById(id);

        return new ResponseEntity<>(
                ApiResponseUtil.successFull("Document details retrieved successfully", response),
                HttpStatus.OK
        );
    }
}