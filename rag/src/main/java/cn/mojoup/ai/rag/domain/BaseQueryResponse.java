package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基础查询响应类
 *
 * @author matt
 */
@Data
public abstract class BaseQueryResponse {

    /**
     * 查询ID
     */
    private String queryId;

    /**
     * 原始问题
     */
    private String question;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 查询类型
     */
    private String queryType;

    /**
     * 是否成功
     */
    private Boolean success = true;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 扩展信息
     */
    private Map<String, Object> metadata;
} 