package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * 简单查询请求
 *
 * @author matt
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleQueryRequest extends BaseQueryRequest {

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;

    /**
     * 温度参数 (0.0-2.0)
     */
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    private Integer maxTokens = 2000;
} 