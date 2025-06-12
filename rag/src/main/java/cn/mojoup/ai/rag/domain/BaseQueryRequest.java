package cn.mojoup.ai.rag.domain;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 基础查询请求类
 *
 * @author matt
 */
@Data
public abstract class BaseQueryRequest {

    /**
     * 查询问题
     */
    @NotBlank(message = "查询问题不能为空")
    private String question;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 返回结果数量限制
     */
    @Min(value = 1, message = "返回结果数量不能小于1")
    @Max(value = 100, message = "返回结果数量不能大于100")
    private Integer limit = 10;

    /**
     * 相似度阈值 (0.0-1.0)
     */
    @Min(value = 0, message = "相似度阈值不能小于0")
    @Max(value = 1, message = "相似度阈值不能大于1")
    private Double similarityThreshold = 0.7;

    /**
     * 是否包含元数据
     */
    private Boolean includeMetadata = true;

    /**
     * 是否包含原文内容
     */
    private Boolean includeContent = true;

    /**
     * 语言设置
     */
    private String language = "zh-CN";
} 