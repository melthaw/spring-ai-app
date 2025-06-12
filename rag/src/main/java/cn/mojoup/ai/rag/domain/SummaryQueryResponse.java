package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SummaryQueryResponse extends BaseQueryResponse {
    private String summary;
    private List<DocumentSegment> sourceDocuments;
    private String knowledgeBaseId;
    private String summaryType;
    private Integer summaryLength;
} 