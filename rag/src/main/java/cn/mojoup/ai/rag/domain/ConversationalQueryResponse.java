package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationalQueryResponse extends BaseQueryResponse {
    private String answer;
    private List<DocumentSegment> documents;
    private String knowledgeBaseId;
    private List<ConversationMessage> updatedHistory;
    private String model;
    private Integer tokensUsed;
} 