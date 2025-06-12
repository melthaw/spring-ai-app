package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class SemanticQueryRequest extends BaseQueryRequest {
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;
    private String embeddingModel = "text-embedding-ada-002";
    private Boolean rerank = true;
} 