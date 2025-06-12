package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class CitationQueryRequest extends BaseQueryRequest {
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;
    private String citationStyle = "apa"; // apa, mla, chicago
    private Boolean includePage = true;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
} 