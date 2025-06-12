package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.List;

@Data
public class BatchQueryRequest {
    private List<String> questions;
    private String knowledgeBaseId;
    private String userId;
    private String sessionId;
    private Integer limit = 10;
    private Double similarityThreshold = 0.7;
    private Boolean parallel = true;
} 