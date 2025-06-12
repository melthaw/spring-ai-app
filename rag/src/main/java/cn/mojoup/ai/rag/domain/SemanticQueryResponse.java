package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SemanticQueryResponse extends BaseQueryResponse {
    private List<DocumentSegment> documents;
    private String knowledgeBaseId;
    private String embeddingModel;
    private Double averageScore;
} 