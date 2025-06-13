package cn.mojoup.ai.knowledgebase.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 文档嵌入结果实体
 * 存储文档的嵌入处理参数和结果，关联rag模块的嵌入功能
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_embedding",
        indexes = {
                @Index(name = "idx_embedding_doc_id", columnList = "document_id"),
                @Index(name = "idx_embedding_model", columnList = "embedding_model"),
                @Index(name = "idx_embedding_status", columnList = "status"),
                @Index(name = "idx_embedding_create_time", columnList = "create_time")
        })
public class DocumentEmbedding {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的文档
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    /**
     * 嵌入模型名称
     */
    @Column(name = "embedding_model", nullable = false, length = 100)
    private String embeddingModel;

    /**
     * 嵌入模型版本
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;

    /**
     * 向量维度
     */
    @Column(name = "vector_dimension", nullable = false)
    private Integer vectorDimension;

    /**
     * 文本分块策略：FIXED_SIZE(固定大小), SENTENCE(按句子), PARAGRAPH(按段落), SEMANTIC(语义分块)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "chunk_strategy", nullable = false, length = 20)
    private ChunkStrategy chunkStrategy;

    /**
     * 分块大小（字符数）
     */
    @Column(name = "chunk_size")
    private Integer chunkSize;

    /**
     * 分块重叠大小（字符数）
     */
    @Column(name = "chunk_overlap")
    private Integer chunkOverlap;

    /**
     * 分块数量
     */
    @Column(name = "chunk_count")
    private Integer chunkCount;

    /**
     * 嵌入状态：PENDING(待处理), PROCESSING(处理中), COMPLETED(完成), FAILED(失败)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmbeddingStatus status;

    /**
     * 嵌入开始时间
     */
    @Column(name = "embedding_start_time")
    private LocalDateTime embeddingStartTime;

    /**
     * 嵌入完成时间
     */
    @Column(name = "embedding_end_time")
    private LocalDateTime embeddingEndTime;

    /**
     * 处理耗时（毫秒）
     */
    @Column(name = "processing_time")
    private Long processingTime;

    /**
     * Token消耗数量
     */
    @Column(name = "tokens_consumed")
    private Integer tokensConsumed;

    /**
     * 嵌入成本（如果适用）
     */
    @Column(name = "embedding_cost")
    private Double embeddingCost;

    /**
     * 向量存储ID（在向量数据库中的标识）
     */
    @Column(name = "vector_store_id", length = 100)
    private String vectorStoreId;

    /**
     * 向量存储索引名称
     */
    @Column(name = "vector_index_name", length = 100)
    private String vectorIndexName;

    /**
     * 嵌入质量评分（0-1之间）
     */
    @Column(name = "quality_score")
    private Double qualityScore;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 错误堆栈
     */
    @Column(name = "error_stack", columnDefinition = "TEXT")
    private String errorStack;

    /**
     * 重试次数
     */
    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    @Builder.Default
    @Column(name = "max_retry_count")
    private Integer maxRetryCount = 3;

    /**
     * 是否启用
     */
    @Builder.Default
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建者ID
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新者ID
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 嵌入参数配置（JSON格式）
     */
    @Column(name = "embedding_config", columnDefinition = "TEXT")
    private String embeddingConfig;

    /**
     * 嵌入结果统计信息（JSON格式）
     */
    @Column(name = "embedding_stats", columnDefinition = "TEXT")
    private String embeddingStats;

    /**
     * 扩展属性（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * 分块策略枚举
     */
    public enum ChunkStrategy {
        FIXED_SIZE,   // 固定大小分块
        SENTENCE,     // 按句子分块
        PARAGRAPH,    // 按段落分块
        SEMANTIC      // 语义分块
    }

    /**
     * 嵌入状态枚举
     */
    public enum EmbeddingStatus {
        PENDING,      // 待处理
        PROCESSING,   // 处理中
        COMPLETED,    // 完成
        FAILED        // 失败
    }

    /**
     * 获取文档ID
     */
    public Long getDocumentId() {
        return document != null ? document.getId() : null;
    }

    /**
     * 获取处理耗时（如果未设置则计算）
     */
    public Long getProcessingTimeMs() {
        if (processingTime != null) {
            return processingTime;
        }
        if (embeddingStartTime != null && embeddingEndTime != null) {
            return java.time.Duration.between(embeddingStartTime, embeddingEndTime).toMillis();
        }
        return null;
    }

    /**
     * 是否处理完成
     */
    public boolean isCompleted() {
        return status == EmbeddingStatus.COMPLETED;
    }

    /**
     * 是否处理失败
     */
    public boolean isFailed() {
        return status == EmbeddingStatus.FAILED;
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return isFailed() && retryCount < maxRetryCount;
    }

    /**
     * 获取平均每个分块的处理时间
     */
    public Double getAvgProcessingTimePerChunk() {
        Long totalTime = getProcessingTimeMs();
        if (totalTime != null && chunkCount != null && chunkCount > 0) {
            return totalTime.doubleValue() / chunkCount;
        }
        return null;
    }
} 