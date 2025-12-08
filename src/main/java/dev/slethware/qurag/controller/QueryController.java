package dev.slethware.qurag.controller;

import dev.slethware.qurag.dto.request.QueryRequest;
import dev.slethware.qurag.dto.response.ApiResponse;
import dev.slethware.qurag.dto.response.QueryResponse;
import dev.slethware.qurag.service.RAG.RagQueryService;
import dev.slethware.qurag.utility.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
@Tag(name = "RAG Query", description = "Retrieval Augmented Generation endpoint for querying documents using semantic search and AI responses")
public class QueryController {

    private final RagQueryService ragQueryService;

    @PostMapping
    @Operation(
            summary = "Query documents using RAG",
            description = "Performs a Retrieval Augmented Generation (RAG) query"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Query processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query request - question cannot be blank")
    })
    public ResponseEntity<ApiResponse<QueryResponse>> query(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Query request containing the question, topK (default: 5), and similarity threshold (default: 0.7)",
                    required = true
            )
            @Valid @RequestBody QueryRequest request) {

        QueryResponse response = ragQueryService.query(request);

        return new ResponseEntity<>(
                ApiResponseUtil.successFull("Query processed successfully", response),
                HttpStatus.OK
        );
    }
}