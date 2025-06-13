package cn.mojoup.ai.kb.service;

import cn.mojoup.ai.kb.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计服务接口
 * 负责记录和查询系统的审计日志
 *
 * @author matt
 */
public interface AuditService {

    // ==================== 审计日志记录 ====================

    /**
     * 记录审计日志
     */
    AuditLog recordAuditLog(String userId, String operationType, String resourceId, String description);

    /**
     * 异步记录审计日志
     */
    void recordAuditLogAsync(String userId, String operationType, String resourceId, String description);

    /**
     * 记录知识库操作日志
     */
    AuditLog recordKnowledgeBaseOperation(String userId, String kbId, AuditLog.OperationType operationType, 
                                         String description, Object beforeData, Object afterData);

    /**
     * 记录文档节点操作日志
     */
    AuditLog recordDocumentNodeOperation(String userId, String nodeId, AuditLog.OperationType operationType, 
                                        String description, Object beforeData, Object afterData);

    /**
     * 记录权限操作日志
     */
    AuditLog recordPermissionOperation(String userId, String resourceId, String resourceType, 
                                      AuditLog.OperationType operationType, String description);

    /**
     * 记录嵌入操作日志
     */
    AuditLog recordEmbeddingOperation(String userId, String nodeId, AuditLog.OperationType operationType, 
                                     String description, Map<String, Object> operationData);

    // ==================== 审计日志查询 ====================

    /**
     * 分页查询审计日志
     */
    Page<AuditLog> getAuditLogs(Pageable pageable);

    /**
     * 根据用户查询审计日志
     */
    Page<AuditLog> getAuditLogsByUser(String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据资源查询审计日志
     */
    Page<AuditLog> getAuditLogsByResource(String resourceType, String resourceId, Pageable pageable);

    /**
     * 根据操作类型查询审计日志
     */
    Page<AuditLog> getAuditLogsByOperation(AuditLog.OperationType operationType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询敏感操作日志
     */
    Page<AuditLog> getSensitiveOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询失败操作日志
     */
    Page<AuditLog> getFailedOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据风险等级查询审计日志
     */
    Page<AuditLog> getAuditLogsByRiskLevel(AuditLog.RiskLevel riskLevel, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // ==================== 审计统计 ====================

    /**
     * 统计用户操作次数
     */
    Long countUserOperations(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计操作类型次数
     */
    Long countOperationsByType(String operationType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期审计日志
     */
    void cleanupExpiredAuditLogs(Integer retentionDays);
} 