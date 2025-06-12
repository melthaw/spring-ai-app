package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntelligentQueryResponse extends BaseQueryResponse {
    private String answer;
    private List<DocumentSegment> documents;
    private List<String> usedKnowledgeBases;
    private String detectedIntent;
    private String selectedStrategy;
    private Map<String, Object> optimizations;
    private Integer tokensUsed;
} 