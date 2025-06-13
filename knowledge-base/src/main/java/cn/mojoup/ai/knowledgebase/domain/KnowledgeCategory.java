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
 * 知识库分类实体
 * 支持树形结构的分类管理
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_category",
        indexes = {
                @Index(name = "idx_category_kb_id", columnList = "kb_id"),
                @Index(name = "idx_category_parent_id", columnList = "parent_id"),
                @Index(name = "idx_category_path", columnList = "category_path"),
                @Index(name = "idx_category_sort", columnList = "sort_order")
        })
public class KnowledgeCategory {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称
     */
    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;

    /**
     * 分类描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 分类图标
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 分类颜色
     */
    @Column(name = "color", length = 20)
    private String color;

    /**
     * 所属知识库
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kb_id", nullable = false)
    private KnowledgeBase knowledgeBase;

    /**
     * 父分类ID（支持树形结构）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 分类路径（如：/root/子分类1/子分类2）
     */
    @Column(name = "category_path", length = 1000)
    private String categoryPath;

    /**
     * 分类层级（0为根分类）
     */
    @Builder.Default
    @Column(name = "level", nullable = false)
    private Integer level = 0;

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
     * 文档数量
     */
    @Builder.Default
    @Column(name = "document_count")
    private Integer documentCount = 0;

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
     * 子分类列表（一对多关系）
     */
    @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KnowledgeCategory> children;

    /**
     * 分类下的文档列表（一对多关系）
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KnowledgeDocument> documents;

    /**
     * 获取知识库ID
     */
    public Long getKnowledgeBaseId() {
        return knowledgeBase != null ? knowledgeBase.getId() : null;
    }

    /**
     * 是否为根分类
     */
    public boolean isRootCategory() {
        return parentId == null || parentId == 0;
    }

    /**
     * 获取完整路径名
     */
    public String getFullPath() {
        if (categoryPath != null && !categoryPath.isEmpty()) {
            return categoryPath + "/" + categoryName;
        }
        return categoryName;
    }
} 