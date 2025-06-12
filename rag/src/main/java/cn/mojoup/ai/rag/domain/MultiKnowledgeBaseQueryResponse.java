package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultiKnowledgeBaseQueryResponse extends BaseQueryResponse {
    private String answer;
    private List<DocumentSegment> documents;
    private List<String> knowledgeBaseIds;
    private Map<String, Integer> knowledgeBaseScores;
    private String model;
    private Integer tokensUsed;
} 