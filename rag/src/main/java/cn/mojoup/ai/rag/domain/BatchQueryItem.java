package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.List;

@Data
public class BatchQueryItem {
    private String question;
    private String answer;
    private List<DocumentSegment> documents;
    private Boolean success;
    private String errorMessage;
    private Long processingTime;
} 