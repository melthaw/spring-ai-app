package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.List;

@Data
public class QuerySuggestionResponse {
    private List<String> suggestions;
    private String knowledgeBaseId;
} 