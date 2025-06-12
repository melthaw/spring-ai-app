package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.List;

@Data
public class BatchQueryResponse {
    private String batchId;
    private List<BatchQueryItem> results;
    private Integer totalQuestions;
    private Integer successCount;
    private Integer failureCount;
    private Long totalProcessingTime;
} 