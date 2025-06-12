package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.List;

@Data
public class QueryHistoryResponse {
    private List<QueryHistoryItem> history;
    private Integer totalCount;
    private String userId;
} 