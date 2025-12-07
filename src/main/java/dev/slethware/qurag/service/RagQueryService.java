package dev.slethware.qurag.service;

import dev.slethware.qurag.dto.request.QueryRequest;
import dev.slethware.qurag.dto.response.QueryResponse;

public interface RagQueryService {
    QueryResponse query(QueryRequest request);
}