package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class SummaryQueryRequest extends BaseQueryRequest {
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;
    private String summaryType = "extractive"; // extractive, abstractive
    private Integer summaryLength = 200;
    private Double temperature = 0.3;
} 