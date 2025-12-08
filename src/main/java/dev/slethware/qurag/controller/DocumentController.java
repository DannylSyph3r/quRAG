package dev.slethware.qurag.controller;

import dev.slethware.qurag.dto.response.ApiResponse;
import dev.slethware.qurag.dto.response.DocumentDetailResponse;
import dev.slethware.qurag.dto.response.DocumentResponse;
import dev.slethware.qurag.service.document.DocumentService;
import dev.slethware.qurag.service.S3.S3Service;
import dev.slethware.qurag.utility.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Endpoints for uploading, retrieving, and managing documents with automatic text extraction and chunking")
public class DocumentController {

    private final DocumentService documentService;
    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a document",
            description = "Uploads a document (PDF, DOCX, or TXT), extracts text using Tika, chunks the content, generates embeddings via Google Gemini, and stores them in Pinecone vector database. The original file is stored in S3."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded and processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file type or file size exceeds 200MB"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Failed to process document")
    })
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @Parameter(description = "Document file (PDF, DOCX, or TXT, max 200MB)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response = documentService.uploadDocument(file);

        return new ResponseEntity<>(
                ApiResponseUtil.created("Document uploaded and processed successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(
            summary = "List all documents",
            description = "Retrieves a list of all uploaded documents with their metadata including filename, file type, file size, chunk count, and upload timestamp."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {
        List<DocumentResponse> documents = documentService.getAllDocuments();

        return new ResponseEntity<>(
                ApiResponseUtil.successFull("Documents retrieved successfully", documents),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get document details",
            description = "Retrieves detailed information about a specific document including extracted text, individual chunks, and metadata. The document is fetched from S3 and re-processed for display."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Failed to extract document details")
    })
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocumentById(
            @Parameter(description = "Document ID", required = true)
            @PathVariable UUID id) {

        DocumentDetailResponse response = documentService.getDocumentById(id);

        return new ResponseEntity<>(
                ApiResponseUtil.successFull("Document details retrieved successfully", response),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}/download")
    @Operation(
            summary = "Download original document",
            description = "Downloads the original uploaded document from S3 storage"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<byte[]> downloadDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable UUID id) {

        DocumentResponse document = documentService.getDocumentMetadata(id);
        byte[] fileBytes = s3Service.downloadFile(document.getFilename());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getFileType()));
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(document.getOriginalFilename())
                        .build()
        );

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}