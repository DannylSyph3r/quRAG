package dev.slethware.qurag.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkInfo {
    private String content;

    @JsonProperty("similarity_score")
    private Double similarityScore;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("chunk_index")
    private Integer chunkIndex;
}