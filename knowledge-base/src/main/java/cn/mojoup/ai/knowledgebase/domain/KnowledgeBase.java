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
 * 知识库实体
 * 企业级知识库的根节点，支持树形结构管理
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_base",
        indexes = {
                @Index(name = "idx_kb_code", columnList = "kb_code"),
                @Index(name = "idx_kb_owner", columnList = "owner_id"),
                @Index(name = "idx_kb_status", columnList = "status"),
                @Index(name = "idx_kb_create_time", columnList = "create_time")
        })
public class KnowledgeBase {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 知识库唯一编码
     */
    @Column(name = "kb_code", unique = true, nullable = false, length = 50)
    private String kbCode;

    /**
     * 知识库名称
     */
    @Column(name = "kb_name", nullable = false, length = 200)
    private String kbName;

    /**
     * 知识库描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 知识库图标
     */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * 知识库封面
     */
    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    /**
     * 所有者ID
     */
    @Column(name = "owner_id", nullable = false, length = 50)
    private String ownerId;

    /**
     * 所有者名称
     */
    @Column(name = "owner_name", length = 100)
    private String ownerName;

    /**
     * 知识库类型：PERSONAL(个人), TEAM(团队), ORGANIZATION(组织)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kb_type", nullable = false, length = 20)
    private KnowledgeBaseType kbType;

    /**
     * 知识库状态：ACTIVE(活跃), ARCHIVED(归档), DELETED(删除)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private KnowledgeBaseStatus status;

    /**
     * 是否公开
     */
    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = false;

    /**
     * 访问级别：PRIVATE(私有), INTERNAL(内部), PUBLIC(公开)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 20)
    private AccessLevel accessLevel;

    /**
     * 总文档数量
     */
    @Builder.Default
    @Column(name = "document_count")
    private Integer documentCount = 0;

    /**
     * 总文件大小（字节）
     */
    @Builder.Default
    @Column(name = "total_size")
    private Long totalSize = 0L;

    /**
     * 最后更新时间
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

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
     * 知识库分类列表（一对多关系）
     */
    @OneToMany(mappedBy = "knowledgeBase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KnowledgeCategory> categories;

    /**
     * 知识库类型枚举
     */
    public enum KnowledgeBaseType {
        PERSONAL,      // 个人知识库
        TEAM,         // 团队知识库
        ORGANIZATION  // 组织知识库
    }

    /**
     * 知识库状态枚举
     */
    public enum KnowledgeBaseStatus {
        ACTIVE,    // 活跃
        ARCHIVED,  // 归档
        DELETED    // 删除
    }

    /**
     * 访问级别枚举
     */
    public enum AccessLevel {
        PRIVATE,   // 私有
        INTERNAL,  // 内部
        PUBLIC     // 公开
    }
} 