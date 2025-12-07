package dev.slethware.qurag.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    @NotBlank(message = "Question cannot be blank")
    private String question;

    @Builder.Default
    private Integer topK = 5;

    @Builder.Default
    private Double similarityThreshold = 0.3;
}