package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntelligentQueryRequest extends BaseQueryRequest {
    private List<String> knowledgeBaseIds;
    private String queryIntent; // qa, search, summary, analysis
    private Map<String, Object> preferences;
    private Boolean autoOptimize = true;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
} 