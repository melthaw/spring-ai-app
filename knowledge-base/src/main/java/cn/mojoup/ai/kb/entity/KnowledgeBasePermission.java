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
 * 知识库权限实体
 * 支持基于组织机构的细粒度权限控制
 * 
 * @author matt
 */
@Entity
@Table(name = "kb_knowledge_base_permission", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"kb_id", "principal_type", "principal_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBasePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id", length = 36)
    private String permissionId;

    /**
     * 关联的知识库
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kb_id", nullable = false)
    @JsonIgnore
    private KnowledgeBase knowledgeBase;

    /**
     * 权限主体类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", nullable = false)
    private PrincipalType principalType;

    /**
     * 权限主体ID
     */
    @Column(name = "principal_id", nullable = false, length = 36)
    private String principalId;

    /**
     * 权限类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;

    /**
     * 权限范围
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_scope", nullable = false)
    private PermissionScope permissionScope;

    /**
     * 是否允许读取
     */
    @Column(name = "can_read", nullable = false)
    private Boolean canRead = false;

    /**
     * 是否允许写入
     */
    @Column(name = "can_write", nullable = false)
    private Boolean canWrite = false;

    /**
     * 是否允许删除
     */
    @Column(name = "can_delete", nullable = false)
    private Boolean canDelete = false;

    /**
     * 是否允许管理
     */
    @Column(name = "can_manage", nullable = false)
    private Boolean canManage = false;

    /**
     * 是否允许嵌入操作
     */
    @Column(name = "can_embed", nullable = false)
    private Boolean canEmbed = false;

    /**
     * 是否允许查询
     */
    @Column(name = "can_query", nullable = false)
    private Boolean canQuery = false;

    /**
     * 是否允许分享
     */
    @Column(name = "can_share", nullable = false)
    private Boolean canShare = false;

    /**
     * 生效时间
     */
    @Column(name = "effective_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    @Column(name = "expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;

    /**
     * 权限状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PermissionStatus status = PermissionStatus.ACTIVE;

    /**
     * 授权者ID
     */
    @Column(name = "granted_by", nullable = false, length = 36)
    private String grantedBy;

    /**
     * 授权理由
     */
    @Column(name = "grant_reason", length = 500)
    private String grantReason;

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
     * 权限主体类型枚举
     */
    public enum PrincipalType {
        USER("用户"),
        ROLE("角色"),
        DEPARTMENT("部门"),
        ORGANIZATION("组织");

        private final String description;

        PrincipalType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        GRANT("授权"),
        DENY("拒绝");

        private final String description;

        PermissionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 权限范围枚举
     */
    public enum PermissionScope {
        SELF("仅自己"),
        DEPARTMENT("本部门"),
        SUB_DEPARTMENTS("本部门及下属部门"),
        ORGANIZATION("整个组织"),
        ALL("全部");

        private final String description;

        PermissionScope(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 权限状态枚举
     */
    public enum PermissionStatus {
        ACTIVE("有效"),
        INACTIVE("无效"),
        EXPIRED("已过期"),
        REVOKED("已撤销");

        private final String description;

        PermissionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查权限是否有效
     */
    public boolean isValid() {
        if (status != PermissionStatus.ACTIVE) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }
        
        if (expiryTime != null && now.isAfter(expiryTime)) {
            return false;
        }
        
        return true;
    }

    /**
     * 检查是否有读权限
     */
    public boolean hasReadPermission() {
        return isValid() && permissionType == PermissionType.GRANT && canRead;
    }

    /**
     * 检查是否有写权限
     */
    public boolean hasWritePermission() {
        return isValid() && permissionType == PermissionType.GRANT && canWrite;
    }

    /**
     * 检查是否有删除权限
     */
    public boolean hasDeletePermission() {
        return isValid() && permissionType == PermissionType.GRANT && canDelete;
    }

    /**
     * 检查是否有管理权限
     */
    public boolean hasManagePermission() {
        return isValid() && permissionType == PermissionType.GRANT && canManage;
    }

    /**
     * 检查是否有嵌入权限
     */
    public boolean hasEmbedPermission() {
        return isValid() && permissionType == PermissionType.GRANT && canEmbed;
    }

    /**
     * 检查是否有查询权限
     */
    public boolean hasQueryPermission() {
        return isValid() && permissionType == PermissionType.GRANT && canQuery;
    }
} 