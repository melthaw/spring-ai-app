package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class StructuredQueryRequest extends BaseQueryRequest {
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;
    private Map<String, Object> filters;
    private List<String> requiredFields;
    private String sortBy;
    private String sortOrder = "desc";
} 