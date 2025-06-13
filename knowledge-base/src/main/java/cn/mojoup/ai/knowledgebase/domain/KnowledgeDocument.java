package cn.mojoup.ai.knowledgebase.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库文档实体
 * 关联upload模块的文件信息和rag模块的嵌入处理
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_document",
        indexes = {
                @Index(name = "idx_doc_category_id", columnList = "category_id"),
                @Index(name = "idx_doc_kb_id", columnList = "kb_id"),
                @Index(name = "idx_doc_file_id", columnList = "file_id"),
                @Index(name = "idx_doc_status", columnList = "status"),
                @Index(name = "idx_doc_create_time", columnList = "create_time")
        })
public class KnowledgeDocument {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文档标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * 文档描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 所属分类
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private KnowledgeCategory category;

    /**
     * 所属知识库ID（冗余字段，便于查询）
     */
    @Column(name = "kb_id", nullable = false)
    private Long kbId;

    /**
     * 关联的文件ID（来自upload模块）
     */
    @Column(name = "file_id", nullable = false, length = 36)
    private String fileId;

    /**
     * 文档类型：TEXT(文本), PDF, WORD, EXCEL, PPT, IMAGE, VIDEO, AUDIO, OTHER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 20)
    private DocumentType docType;

    /**
     * 文档状态：UPLOADING(上传中), PROCESSING(处理中), EMBEDDING(嵌入中), COMPLETED(完成), FAILED(失败)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    /**
     * 文档大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文档页数（如果适用）
     */
    @Column(name = "page_count")
    private Integer pageCount;

    /**
     * 文档字符数
     */
    @Column(name = "char_count")
    private Integer charCount;

    /**
     * 提取的文本内容（用于搜索和预览）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 文档摘要
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * 文档标签（JSON数组格式）
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * 文档访问权限：PUBLIC(公开), PRIVATE(私有), RESTRICTED(受限)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 20)
    private AccessLevel accessLevel;

    /**
     * 排序顺序
     */
    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用
     */
    @Builder.Default
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 文档处理开始时间
     */
    @Column(name = "process_start_time")
    private LocalDateTime processStartTime;

    /**
     * 文档处理完成时间
     */
    @Column(name = "process_end_time")
    private LocalDateTime processEndTime;

    /**
     * 处理错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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
     * 扩展属性（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * 文档的嵌入结果列表（一对多关系）
     */
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentEmbedding> embeddings;

    /**
     * 文档类型枚举
     */
    public enum DocumentType {
        TEXT,      // 纯文本
        PDF,       // PDF文档
        WORD,      // Word文档
        EXCEL,     // Excel表格
        PPT,       // PowerPoint演示
        IMAGE,     // 图片
        VIDEO,     // 视频
        AUDIO,     // 音频
        OTHER      // 其他类型
    }

    /**
     * 文档状态枚举
     */
    public enum DocumentStatus {
        UPLOADING,   // 上传中
        PROCESSING,  // 处理中
        EMBEDDING,   // 嵌入中
        COMPLETED,   // 完成
        FAILED       // 失败
    }

    /**
     * 访问级别枚举
     */
    public enum AccessLevel {
        PUBLIC,      // 公开
        PRIVATE,     // 私有
        RESTRICTED   // 受限
    }

    /**
     * 获取分类ID
     */
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    /**
     * 获取处理耗时（毫秒）
     */
    public Long getProcessingTime() {
        if (processStartTime != null && processEndTime != null) {
            return java.time.Duration.between(processStartTime, processEndTime).toMillis();
        }
        return null;
    }

    /**
     * 是否处理完成
     */
    public boolean isProcessed() {
        return status == DocumentStatus.COMPLETED;
    }

    /**
     * 是否处理失败
     */
    public boolean isFailed() {
        return status == DocumentStatus.FAILED;
    }
} 