package cn.mojoup.ai.kb.service;

import cn.mojoup.ai.kb.dto.*;
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
    AuditLog recordAuditLog(RecordAuditLogRequest request);

    /**
     * 异步记录审计日志
     */
    void recordAuditLogAsync(RecordAuditLogRequest request);

    /**
     * 批量记录审计日志
     */
    List<AuditLog> batchRecordAuditLogs(List<RecordAuditLogRequest> requests);

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
    Page<AuditLogDTO> getAuditLogs(AuditLogQueryRequest request, Pageable pageable);

    /**
     * 根据用户查询审计日志
     */
    Page<AuditLogDTO> getAuditLogsByUser(String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据资源查询审计日志
     */
    Page<AuditLogDTO> getAuditLogsByResource(String resourceType, String resourceId, Pageable pageable);

    /**
     * 根据操作类型查询审计日志
     */
    Page<AuditLogDTO> getAuditLogsByOperation(AuditLog.OperationType operationType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询敏感操作日志
     */
    Page<AuditLogDTO> getSensitiveOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查询失败操作日志
     */
    Page<AuditLogDTO> getFailedOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据风险等级查询审计日志
     */
    Page<AuditLogDTO> getAuditLogsByRiskLevel(AuditLog.RiskLevel riskLevel, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 搜索审计日志
     */
    Page<AuditLogDTO> searchAuditLogs(AuditLogSearchRequest request, Pageable pageable);

    // ==================== 审计报告和分析 ====================

    /**
     * 生成审计报告
     */
    AuditReportDTO generateAuditReport(AuditReportRequest request);

    /**
     * 生成用户活动报告
     */
    UserActivityReportDTO generateUserActivityReport(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成资源访问报告
     */
    ResourceAccessReportDTO generateResourceAccessReport(String resourceType, String resourceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成安全事件报告
     */
    SecurityEventReportDTO generateSecurityEventReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分析操作模式
     */
    OperationPatternAnalysisDTO analyzeOperationPatterns(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检测异常行为
     */
    List<AnomalousActivityDTO> detectAnomalousActivity(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成合规报告
     */
    ComplianceReportDTO generateComplianceReport(String organizationId, LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 审计统计 ====================

    /**
     * 获取审计统计信息
     */
    AuditStatisticsDTO getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按操作类型统计
     */
    List<OperationTypeStatisticsDTO> getOperationTypeStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按用户统计操作次数
     */
    List<UserOperationStatisticsDTO> getUserOperationStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按资源类型统计访问次数
     */
    List<ResourceAccessStatisticsDTO> getResourceAccessStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按时间段统计操作分布
     */
    List<TimeDistributionStatisticsDTO> getTimeDistributionStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取风险操作统计
     */
    RiskOperationStatisticsDTO getRiskOperationStatistics(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 审计配置和管理 ====================

    /**
     * 更新审计配置
     */
    void updateAuditConfiguration(AuditConfigurationRequest request);

    /**
     * 获取审计配置
     */
    AuditConfigurationDTO getAuditConfiguration();

    /**
     * 设置审计规则
     */
    void setAuditRules(List<AuditRuleRequest> rules);

    /**
     * 获取审计规则
     */
    List<AuditRuleDTO> getAuditRules();

    /**
     * 清理过期审计日志
     */
    CleanupAuditLogResultDTO cleanupExpiredAuditLogs(Integer retentionDays);

    /**
     * 归档审计日志
     */
    ArchiveAuditLogResultDTO archiveAuditLogs(LocalDateTime beforeDate);

    // ==================== 审计导出和备份 ====================

    /**
     * 导出审计日志
     */
    AuditLogExportDTO exportAuditLogs(AuditLogExportRequest request);

    /**
     * 备份审计日志
     */
    AuditLogBackupDTO backupAuditLogs(AuditLogBackupRequest request);

    /**
     * 恢复审计日志
     */
    AuditLogRestoreResultDTO restoreAuditLogs(AuditLogRestoreRequest request);

    // ==================== 实时监控 ====================

    /**
     * 获取实时审计监控数据
     */
    RealTimeAuditMonitorDTO getRealTimeAuditMonitor();

    /**
     * 设置审计告警规则
     */
    void setAuditAlertRules(List<AuditAlertRuleRequest> rules);

    /**
     * 获取审计告警
     */
    List<AuditAlertDTO> getAuditAlerts(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 确认审计告警
     */
    void acknowledgeAuditAlert(String alertId, String acknowledgedBy);

    // ==================== 审计验证 ====================

    /**
     * 验证审计日志完整性
     */
    AuditIntegrityVerificationDTO verifyAuditIntegrity(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成审计证明
     */
    AuditProofDTO generateAuditProof(String auditLogId);

    /**
     * 验证审计证明
     */
    AuditProofVerificationDTO verifyAuditProof(String auditProof);
} 