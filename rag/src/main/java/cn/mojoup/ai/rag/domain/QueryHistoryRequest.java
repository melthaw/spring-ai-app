package cn.mojoup.ai.rag.domain;

import lombok.Data;

@Data
public class QueryHistoryRequest {
    private String userId;
    private String sessionId;
    private Integer limit = 20;
    private String startDate;
    private String endDate;
} 