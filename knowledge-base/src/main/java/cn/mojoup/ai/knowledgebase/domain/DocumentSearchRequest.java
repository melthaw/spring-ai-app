package cn.mojoup.ai.knowledgebase.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档搜索请求
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {

    /**
     * 关键词（搜索标题、内容、摘要）
     */
    private String keyword;

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类ID列表（支持多分类查询）
     */
    private List<Long> categoryIds;

    /**
     * 文档类型列表
     */
    private List<KnowledgeDocument.DocumentType> docTypes;

    /**
     * 文档状态列表
     */
    private List<KnowledgeDocument.DocumentStatus> statuses;

    /**
     * 访问级别列表
     */
    private List<KnowledgeDocument.AccessLevel> accessLevels;

    /**
     * 创建者ID
     */
    private String createdBy;

    /**
     * 标签（支持模糊匹配）
     */
    private String tag;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 是否启用
     */
    private Boolean enabled;

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
     * 最小文件大小（字节）
     */
    private Long minFileSize;

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize;

    /**
     * 最小字符数
     */
    private Integer minCharCount;

    /**
     * 最大字符数
     */
    private Integer maxCharCount;

    /**
     * 是否包含嵌入数据
     */
    private Boolean hasEmbedding;

    /**
     * 嵌入模型
     */
    private String embeddingModel;

    /**
     * 嵌入状态
     */
    private DocumentEmbedding.EmbeddingStatus embeddingStatus;

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