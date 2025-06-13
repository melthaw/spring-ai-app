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
import java.util.List;

/**
 * 文档节点实体 - 支持树形结构
 * 
 * @author matt
 */
@Entity
@Table(name = "kb_document_node")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "node_id", length = 36)
    private String nodeId;

    /**
     * 节点名称
     */
    @Column(name = "node_name", nullable = false, length = 200)
    private String nodeName;

    /**
     * 节点类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    /**
     * 节点路径（用于快速查找）
     */
    @Column(name = "node_path", length = 1000)
    private String nodePath;

    /**
     * 层级深度
     */
    @Column(name = "level", nullable = false)
    private Integer level = 0;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 所属知识库
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kb_id", nullable = false)
    @JsonIgnore
    private KnowledgeBase knowledgeBase;

    /**
     * 父节点
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private DocumentNode parent;

    /**
     * 子节点列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentNode> children;

    /**
     * 关联的文件ID（来自upload模块）
     */
    @Column(name = "file_id", length = 36)
    private String fileId;

    /**
     * 文件名称
     */
    @Column(name = "file_name", length = 200)
    private String fileName;

    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", length = 10)
    private String fileExtension;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * 文档描述
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * 标签（JSON存储）
     */
    @Column(name = "tags", columnDefinition = "text")
    private String tags;

    /**
     * 扩展属性（JSON存储）
     */
    @Column(name = "attributes", columnDefinition = "text")
    private String attributes;

    /**
     * 节点状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NodeStatus status = NodeStatus.ACTIVE;

    /**
     * 访问级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    private AccessLevel accessLevel = AccessLevel.INTERNAL;

    /**
     * 是否已嵌入
     */
    @Column(name = "embedded", nullable = false)
    private Boolean embedded = false;

    /**
     * 嵌入状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "embedding_status")
    private EmbeddingStatus embeddingStatus = EmbeddingStatus.PENDING;

    /**
     * 嵌入时间
     */
    @Column(name = "embedding_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime embeddingTime;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false, length = 36)
    private String creatorId;

    /**
     * 最后修改者ID
     */
    @Column(name = "last_modifier_id", length = 36)
    private String lastModifierId;

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
     * 软删除标识
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 文档嵌入记录列表
     */
    @OneToMany(mappedBy = "documentNode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentEmbedding> embeddings;

    /**
     * 权限列表
     */
    @OneToMany(mappedBy = "documentNode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentNodePermission> permissions;

    /**
     * 节点类型枚举
     */
    public enum NodeType {
        FOLDER("文件夹"),
        DOCUMENT("文档");

        private final String description;

        NodeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 节点状态枚举
     */
    public enum NodeStatus {
        ACTIVE("正常"),
        PROCESSING("处理中"),
        ERROR("错误"),
        ARCHIVED("已归档");

        private final String description;

        NodeStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 嵌入状态枚举
     */
    public enum EmbeddingStatus {
        PENDING("待嵌入"),
        PROCESSING("嵌入中"),
        SUCCESS("已成功"),
        FAILED("已失败");

        private final String description;

        EmbeddingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 访问级别枚举（复用知识库的）
     */
    public enum AccessLevel {
        PUBLIC("公开"),
        INTERNAL("内部"),
        RESTRICTED("受限"),
        CONFIDENTIAL("机密");

        private final String description;

        AccessLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 