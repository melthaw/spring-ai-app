package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultiKnowledgeBaseQueryRequest extends BaseQueryRequest {
    @NotEmpty(message = "知识库ID列表不能为空")
    private List<String> knowledgeBaseIds;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private String strategy = "weighted"; // weighted, sequential, parallel
} 