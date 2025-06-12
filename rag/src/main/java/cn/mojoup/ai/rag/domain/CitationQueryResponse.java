package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CitationQueryResponse extends BaseQueryResponse {
    private String answer;
    private List<Citation> citations;
    private String knowledgeBaseId;
    private String citationStyle;
    private Integer tokensUsed;
} 