package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class StructuredQueryResponse extends BaseQueryResponse {
    private String answer;
    private List<DocumentSegment> documents;
    private String knowledgeBaseId;
    private Map<String, Object> appliedFilters;
    private Integer totalMatches;
} 