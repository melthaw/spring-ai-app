package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.List;

@Data
public class RelatedQueryResponse {
    private List<String> relatedQueries;
    private String knowledgeBaseId;
} 