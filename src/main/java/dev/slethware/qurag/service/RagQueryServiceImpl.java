package dev.slethware.qurag.service;

import dev.slethware.qurag.dto.request.QueryRequest;
import dev.slethware.qurag.dto.response.ChunkInfo;
import dev.slethware.qurag.dto.response.QueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagQueryServiceImpl implements RagQueryService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    @Override
    public QueryResponse query(QueryRequest request) {
        log.info("Processing query: {}", request.getQuestion());

        // Retrieve relevant chunks using vector search
        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.getQuestion())
                .topK(request.getTopK())
                .similarityThreshold(request.getSimilarityThreshold())
                .build();

        List<Document> relevantDocuments = vectorStore.similaritySearch(searchRequest);

        // Build chat client with QuestionAnswerAdvisor
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build())
                .build();

        // Get answer from LLM with context
        String answer = chatClient.prompt()
                .user(request.getQuestion())
                .call()
                .content();

        // Build chunk info with similarity scores
        List<ChunkInfo> chunksUsed = relevantDocuments.stream()
                .map(doc -> ChunkInfo.builder()
                        .content(doc.getText())
                        .similarityScore(doc.getMetadata().get("distance") != null
                                ? 1.0 - Double.parseDouble(doc.getMetadata().get("distance").toString())
                                : null)
                        .documentId(doc.getMetadata().get("document_id") != null
                                ? doc.getMetadata().get("document_id").toString()
                                : null)
                        .chunkIndex(doc.getMetadata().get("chunk_index") != null
                                ? parseChunkIndex(doc.getMetadata().get("chunk_index"))  // FIXED
                                : null)
                        .build())
                .collect(Collectors.toList());

        log.info("Query processed successfully with {} chunks", chunksUsed.size());

        return QueryResponse.builder()
                .answer(answer)
                .chunksUsed(chunksUsed)
                .totalChunks(chunksUsed.size())
                .build();
    }

    // Helper method to handle both integer and decimal strings
    private Integer parseChunkIndex(Object value) {
        if (value == null) return null;

        String strValue = value.toString();
        try {
            // Handle decimal strings like "0.0" by converting to double first
            return (int) Double.parseDouble(strValue);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse chunk_index: {}", strValue);
            return null;
        }
    }
}