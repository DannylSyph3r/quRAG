package dev.slethware.qurag.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailResponse {
    private UUID id;
    private String filename;

    @JsonProperty("original_filename")
    private String originalFilename;

    @JsonProperty("file_type")
    private String fileType;

    @JsonProperty("file_size")
    private Long fileSize;

    @JsonProperty("chunk_count")
    private Integer chunkCount;

    @JsonProperty("uploaded_at")
    private LocalDateTime uploadedAt;

    @JsonProperty("s3_url")
    private String s3Url;

    @JsonProperty("extracted_text")
    private String extractedText;

    private List<String> chunks;
}