package cn.mojoup.ai.kb.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 记录所有对知识库的操作，支持企业级审计
 * 
 * @author matt
 */
@Entity
@Table(name = "kb_audit_log",
       indexes = {
           @Index(name = "idx_audit_user_time", columnList = "user_id, operation_time"),
           @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"),
           @Index(name = "idx_audit_operation", columnList = "operation_type, operation_time"),
           @Index(name = "idx_audit_status", columnList = "operation_status, operation_time")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", length = 36)
    private String logId;

    /**
     * 操作用户ID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * 操作用户名
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * 用户真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;

    /**
     * 用户部门ID
     */
    @Column(name = "department_id", length = 36)
    private String departmentId;

    /**
     * 用户部门名称
     */
    @Column(name = "department_name", length = 200)
    private String departmentName;

    /**
     * 用户组织ID
     */
    @Column(name = "organization_id", length = 36)
    private String organizationId;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    /**
     * 操作分类
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_category", nullable = false)
    private OperationCategory operationCategory;

    /**
     * 资源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    /**
     * 资源ID
     */
    @Column(name = "resource_id", length = 36)
    private String resourceId;

    /**
     * 资源名称
     */
    @Column(name = "resource_name", length = 500)
    private String resourceName;

    /**
     * 父资源ID
     */
    @Column(name = "parent_resource_id", length = 36)
    private String parentResourceId;

    /**
     * 操作描述
     */
    @Column(name = "operation_description", length = 1000)
    private String operationDescription;

    /**
     * 操作状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status", nullable = false)
    private OperationStatus operationStatus;

    /**
     * 操作前数据（JSON格式）
     */
    @Column(name = "before_data", columnDefinition = "text")
    private String beforeData;

    /**
     * 操作后数据（JSON格式）
     */
    @Column(name = "after_data", columnDefinition = "text")
    private String afterData;

    /**
     * 变更内容（JSON格式）
     */
    @Column(name = "change_data", columnDefinition = "text")
    private String changeData;

    /**
     * 请求IP地址
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 请求URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 64)
    private String sessionId;

    /**
     * 追踪ID
     */
    @Column(name = "trace_id", length = 64)
    private String traceId;

    /**
     * 操作耗时（毫秒）
     */
    @Column(name = "duration")
    private Long duration;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    /**
     * 风险等级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel = RiskLevel.LOW;

    /**
     * 业务标识
     */
    @Column(name = "business_key", length = 100)
    private String businessKey;

    /**
     * 额外属性（JSON格式）
     */
    @Column(name = "extra_attributes", columnDefinition = "text")
    private String extraAttributes;

    /**
     * 操作时间
     */
    @CreationTimestamp
    @Column(name = "operation_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        CREATE("创建"),
        UPDATE("更新"),
        DELETE("删除"),
        READ("读取"),
        UPLOAD("上传"),
        DOWNLOAD("下载"),
        EMBED("嵌入"),
        QUERY("查询"),
        SHARE("分享"),
        PERMISSION_GRANT("授权"),
        PERMISSION_REVOKE("撤销权限"),
        LOGIN("登录"),
        LOGOUT("登出"),
        EXPORT("导出"),
        IMPORT("导入"),
        BACKUP("备份"),
        RESTORE("恢复");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 操作分类枚举
     */
    public enum OperationCategory {
        KNOWLEDGE_BASE("知识库管理"),
        DOCUMENT("文档管理"),
        PERMISSION("权限管理"),
        EMBEDDING("嵌入处理"),
        SYSTEM("系统管理"),
        SECURITY("安全相关");

        private final String description;

        OperationCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        KNOWLEDGE_BASE("知识库"),
        DOCUMENT_NODE("文档节点"),
        DOCUMENT_EMBEDDING("文档嵌入"),
        KB_PERMISSION("知识库权限"),
        NODE_PERMISSION("节点权限"),
        USER("用户"),
        SYSTEM("系统");

        private final String description;

        ResourceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 操作状态枚举
     */
    public enum OperationStatus {
        SUCCESS("成功"),
        FAILED("失败"),
        PARTIAL_SUCCESS("部分成功"),
        CANCELLED("已取消");

        private final String description;

        OperationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险"),
        CRITICAL("严重风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 构建审计日志的静态方法
     */
    public static AuditLogBuilder createAuditLog(String userId, OperationType operationType, 
                                                 ResourceType resourceType, String resourceId) {
        return AuditLog.builder()
                .userId(userId)
                .operationType(operationType)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .operationStatus(OperationStatus.SUCCESS)
                .riskLevel(RiskLevel.LOW);
    }

    /**
     * 判断是否为敏感操作
     */
    public boolean isSensitiveOperation() {
        return operationType == OperationType.DELETE ||
               operationType == OperationType.PERMISSION_GRANT ||
               operationType == OperationType.PERMISSION_REVOKE ||
               riskLevel == RiskLevel.HIGH ||
               riskLevel == RiskLevel.CRITICAL;
    }

    /**
     * 判断是否为失败操作
     */
    public boolean isFailedOperation() {
        return operationStatus == OperationStatus.FAILED;
    }
} 