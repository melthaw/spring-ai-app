package cn.mojoup.ai.kb.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 文档嵌入记录实体
 * 记录文档的嵌入操作参数和结果
 * 
 * @author matt
 */
@Entity
@Table(name = "kb_document_embedding")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "embedding_id", length = 36)
    private String embeddingId;

    /**
     * 关联的文档节点
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    @JsonIgnore
    private DocumentNode documentNode;

    /**
     * 嵌入任务ID
     */
    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    /**
     * 使用的嵌入模型
     */
    @Column(name = "embedding_model", nullable = false, length = 100)
    private String embeddingModel;

    /**
     * 分块大小
     */
    @Column(name = "chunk_size", nullable = false)
    private Integer chunkSize;

    /**
     * 分块重叠
     */
    @Column(name = "chunk_overlap", nullable = false)
    private Integer chunkOverlap;

    /**
     * 分块策略
     */
    @Column(name = "chunk_strategy", length = 50)
    private String chunkStrategy;

    /**
     * 预处理参数（JSON格式）
     */
    @Column(name = "preprocessing_params", columnDefinition = "text")
    private String preprocessingParams;

    /**
     * 嵌入参数（JSON格式）
     */
    @Column(name = "embedding_params", columnDefinition = "text")
    private String embeddingParams;

    /**
     * 嵌入状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmbeddingStatus status = EmbeddingStatus.PENDING;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    @Column(name = "processing_time")
    private Long processingTime;

    /**
     * 生成的文档段数量
     */
    @Column(name = "segment_count")
    private Integer segmentCount;

    /**
     * 生成的向量数量
     */
    @Column(name = "vector_count")
    private Integer vectorCount;

    /**
     * 平均向量维度
     */
    @Column(name = "vector_dimension")
    private Integer vectorDimension;

    /**
     * 使用的Token数量
     */
    @Column(name = "token_usage")
    private Integer tokenUsage;

    /**
     * 处理结果（JSON格式）
     */
    @Column(name = "result", columnDefinition = "text")
    private String result;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    /**
     * 错误堆栈
     */
    @Column(name = "error_stack", columnDefinition = "text")
    private String errorStack;

    /**
     * 重试次数
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    @Column(name = "max_retry", nullable = false)
    private Integer maxRetry = 3;

    /**
     * 是否为当前版本
     */
    @Column(name = "current_version", nullable = false)
    private Boolean currentVersion = true;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 执行者ID
     */
    @Column(name = "executor_id", nullable = false, length = 36)
    private String executorId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 嵌入状态枚举
     */
    public enum EmbeddingStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        SUCCESS("成功"),
        FAILED("失败"),
        CANCELLED("已取消");

        private final String description;

        EmbeddingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return status == EmbeddingStatus.FAILED && retryCount < maxRetry;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    /**
     * 标记为开始处理
     */
    public void markAsProcessing() {
        this.status = EmbeddingStatus.PROCESSING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 标记为处理成功
     */
    public void markAsSuccess(Integer segmentCount, Integer vectorCount, Integer vectorDimension, Integer tokenUsage, String result) {
        this.status = EmbeddingStatus.SUCCESS;
        this.endTime = LocalDateTime.now();
        this.processingTime = java.time.Duration.between(startTime, endTime).toMillis();
        this.segmentCount = segmentCount;
        this.vectorCount = vectorCount;
        this.vectorDimension = vectorDimension;
        this.tokenUsage = tokenUsage;
        this.result = result;
    }

    /**
     * 标记为处理失败
     */
    public void markAsFailed(String errorMessage, String errorStack) {
        this.status = EmbeddingStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.processingTime = java.time.Duration.between(startTime, endTime).toMillis();
        this.errorMessage = errorMessage;
        this.errorStack = errorStack;
    }
} 