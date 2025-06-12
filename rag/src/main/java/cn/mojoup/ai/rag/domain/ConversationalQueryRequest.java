package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationalQueryRequest extends BaseQueryRequest {
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;
    private List<ConversationMessage> conversationHistory;
    private Integer maxHistoryLength = 10;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
} 