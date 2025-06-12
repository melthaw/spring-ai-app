package cn.mojoup.ai.rag.domain;

import lombok.Data;

@Data
public class QueryHistoryItem {
    private String queryId;
    private String question;
    private String answer;
    private String queryTime;
    private String queryType;
    private Boolean success;
} 