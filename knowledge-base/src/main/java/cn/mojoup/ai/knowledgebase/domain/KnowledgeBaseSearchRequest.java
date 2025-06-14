package cn.mojoup.ai.knowledgebase.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库搜索请求
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseSearchRequest {

    /**
     * 关键词（搜索名称和描述）
     */
    private String keyword;

    /**
     * 所有者ID
     */
    private String ownerId;

    /**
     * 知识库类型列表
     */
    private List<KnowledgeBase.KnowledgeBaseType> kbTypes;

    /**
     * 访问级别列表
     */
    private List<KnowledgeBase.AccessLevel> accessLevels;

    /**
     * 状态列表
     */
    private List<KnowledgeBase.KnowledgeBaseStatus> statuses;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 创建时间范围 - 开始
     */
    private LocalDateTime createTimeStart;

    /**
     * 创建时间范围 - 结束
     */
    private LocalDateTime createTimeEnd;

    /**
     * 更新时间范围 - 开始
     */
    private LocalDateTime updateTimeStart;

    /**
     * 更新时间范围 - 结束
     */
    private LocalDateTime updateTimeEnd;

    /**
     * 最小文档数量
     */
    private Integer minDocumentCount;

    /**
     * 最大文档数量
     */
    private Integer maxDocumentCount;

    /**
     * 最小存储大小（字节）
     */
    private Long minTotalSize;

    /**
     * 最大存储大小（字节）
     */
    private Long maxTotalSize;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向
     */
    private SortDirection sortDirection;

    /**
     * 排序方向枚举
     */
    public enum SortDirection {
        ASC, DESC
    }
} 