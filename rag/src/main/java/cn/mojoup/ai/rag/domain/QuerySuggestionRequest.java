package cn.mojoup.ai.rag.domain;

import lombok.Data;

@Data
public class QuerySuggestionRequest {
    private String partialQuery;
    private String knowledgeBaseId;
    private String userId;
    private Integer limit = 5;
} 