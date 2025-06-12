package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HybridQueryResponse extends BaseQueryResponse {
    private String answer;
    private List<DocumentSegment> documents;
    private String knowledgeBaseId;
    private List<String> matchedKeywords;
    private Double hybridScore;
} 