package dev.slethware.qurag.service;

import dev.slethware.qurag.dto.response.DocumentDetailResponse;
import dev.slethware.qurag.dto.response.DocumentResponse;
import dev.slethware.qurag.entity.Document;
import dev.slethware.qurag.exception.BadRequestException;
import dev.slethware.qurag.exception.ResourceNotFoundException;
import dev.slethware.qurag.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;
    private final S3Service s3Service;

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024; // 200MB

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "_" + originalFilename;

        try {
            // Upload to S3
            String s3Url = s3Service.uploadFile(file, filename);

            // Extract text using Tika
            DocumentReader reader = new TikaDocumentReader(new ByteArrayResource(file.getBytes()));
            List<org.springframework.ai.document.Document> documents = reader.get();

            if (documents.isEmpty()) {
                throw new BadRequestException("Failed to extract text from document");
            }

            // Chunk the documents
            TextSplitter textSplitter = new TokenTextSplitter();
            List<org.springframework.ai.document.Document> chunks = textSplitter.apply(documents);

            Document document = Document.builder()
                    .filename(filename)
                    .originalFilename(originalFilename)
                    .s3Url(s3Url)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .chunkCount(chunks.size())
                    .build();

            documentRepository.save(document);

            String documentId = document.getId().toString();
            for (int i = 0; i < chunks.size(); i++) {
                org.springframework.ai.document.Document chunk = chunks.get(i);
                chunk.getMetadata().put("document_id", documentId);
                chunk.getMetadata().put("chunk_index", i);
                chunk.getMetadata().put("filename", originalFilename);
            }

            log.info("About to store {} chunks in Pinecone vector store", chunks.size());

            vectorStore.add(chunks);

            log.info("Document uploaded successfully: {} with {} chunks", originalFilename, chunks.size());

            return mapToDocumentResponse(document);

        } catch (IOException e) {
            log.error("Failed to process document: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to process document: " + e.getMessage());
        }
    }

    @Override
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDetailResponse getDocumentById(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Download file from S3
        String fileKey = document.getFilename();
        byte[] fileBytes = s3Service.downloadFile(fileKey);

        try {
            // Extract text again for display
            DocumentReader reader = new TikaDocumentReader(new ByteArrayResource(fileBytes));
            List<org.springframework.ai.document.Document> documents = reader.get();

            String extractedText = documents.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .collect(Collectors.joining("\n\n"));

            // Chunk the documents for display
            TextSplitter textSplitter = new TokenTextSplitter();
            List<org.springframework.ai.document.Document> chunks = textSplitter.apply(documents);

            List<String> chunkContents = chunks.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .collect(Collectors.toList());

            return DocumentDetailResponse.builder()
                    .id(document.getId())
                    .filename(document.getFilename())
                    .originalFilename(document.getOriginalFilename())
                    .fileType(document.getFileType())
                    .fileSize(document.getFileSize())
                    .chunkCount(document.getChunkCount())
                    .uploadedAt(document.getUploadedAt())
                    .s3Url(document.getS3Url())
                    .extractedText(extractedText)
                    .chunks(chunkContents)
                    .build();

        } catch (Exception e) {
            log.error("Failed to extract document details: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to extract document details: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size of 200MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Allowed types: PDF, DOCX, TXT");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BadRequestException("Filename cannot be empty");
        }
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .filename(document.getFilename())
                .originalFilename(document.getOriginalFilename())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .chunkCount(document.getChunkCount())
                .uploadedAt(document.getUploadedAt())
                .s3Url(document.getS3Url())
                .build();
    }
}