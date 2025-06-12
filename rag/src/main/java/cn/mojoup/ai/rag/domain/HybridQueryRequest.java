package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HybridQueryRequest extends BaseQueryRequest {
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;
    private List<String> keywords;
    private Double keywordWeight = 0.3;
    private Double semanticWeight = 0.7;
    private Boolean enableRerank = true;
} 