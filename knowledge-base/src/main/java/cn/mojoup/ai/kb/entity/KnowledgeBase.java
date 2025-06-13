package cn.mojoup.ai.kb.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 知识库实体
 * 
 * @author matt
 */
@Entity
@Table(name = "kb_knowledge_base")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "kb_id", length = 36)
    private String kbId;

    /**
     * 知识库名称
     */
    @Column(name = "kb_name", nullable = false, length = 100)
    private String kbName;

    /**
     * 知识库描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 知识库类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kb_type", nullable = false)
    private KnowledgeBaseType kbType;

    /**
     * 所属组织ID
     */
    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    /**
     * 所属部门ID
     */
    @Column(name = "department_id", length = 36)
    private String departmentId;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false, length = 36)
    private String creatorId;

    /**
     * 管理员列表（JSON存储）
     */
    @Column(name = "admin_ids", columnDefinition = "text")
    private String adminIds;

    /**
     * 知识库状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KnowledgeBaseStatus status;

    /**
     * 访问级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    private AccessLevel accessLevel;

    /**
     * 是否启用版本控制
     */
    @Column(name = "version_enabled", nullable = false)
    private Boolean versionEnabled = false;

    /**
     * 默认嵌入模型
     */
    @Column(name = "default_embedding_model", length = 100)
    private String defaultEmbeddingModel;

    /**
     * 默认分块大小
     */
    @Column(name = "default_chunk_size")
    private Integer defaultChunkSize = 1000;

    /**
     * 默认分块重叠
     */
    @Column(name = "default_chunk_overlap")
    private Integer defaultChunkOverlap = 200;

    /**
     * 文档数量
     */
    @Column(name = "document_count")
    private Integer documentCount = 0;

    /**
     * 总文件大小（字节）
     */
    @Column(name = "total_size")
    private Long totalSize = 0L;

    /**
     * 最后更新时间
     */
    @Column(name = "last_updated")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;

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
     * 文档节点列表
     */
    @OneToMany(mappedBy = "knowledgeBase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentNode> documentNodes;

    /**
     * 权限列表
     */
    @OneToMany(mappedBy = "knowledgeBase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KnowledgeBasePermission> permissions;

    /**
     * 知识库类型枚举
     */
    public enum KnowledgeBaseType {
        GENERAL("通用"), 
        TECHNICAL("技术"), 
        BUSINESS("业务"), 
        PRODUCT("产品"), 
        TRAINING("培训");

        private final String description;

        KnowledgeBaseType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 知识库状态枚举
     */
    public enum KnowledgeBaseStatus {
        ACTIVE("活跃"), 
        INACTIVE("非活跃"), 
        MAINTENANCE("维护中"), 
        ARCHIVED("已归档");

        private final String description;

        KnowledgeBaseStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 访问级别枚举
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