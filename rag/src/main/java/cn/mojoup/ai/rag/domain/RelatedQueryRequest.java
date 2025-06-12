package cn.mojoup.ai.rag.domain;

import lombok.Data;

@Data
public class RelatedQueryRequest {
    private String currentQuery;
    private String knowledgeBaseId;
    private String userId;
    private Integer limit = 5;
} 