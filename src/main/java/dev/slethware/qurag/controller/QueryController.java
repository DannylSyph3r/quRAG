package dev.slethware.qurag.controller;

import dev.slethware.qurag.dto.request.QueryRequest;
import dev.slethware.qurag.dto.response.ApiResponse;
import dev.slethware.qurag.dto.response.QueryResponse;
import dev.slethware.qurag.service.RagQueryService;
import dev.slethware.qurag.utility.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
public class QueryController {

    private final RagQueryService ragQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<QueryResponse>> query(
            @Valid @RequestBody QueryRequest request) {

        QueryResponse response = ragQueryService.query(request);

        return new ResponseEntity<>(
                ApiResponseUtil.successFull("Query processed successfully", response),
                HttpStatus.OK
        );
    }
}