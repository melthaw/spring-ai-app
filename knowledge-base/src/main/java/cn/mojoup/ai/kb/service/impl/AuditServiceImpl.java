package cn.mojoup.ai.kb.service.impl;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.AuditLog;
import cn.mojoup.ai.kb.repository.AuditLogRepository;
import cn.mojoup.ai.kb.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审计服务实现
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog recordAuditLog(RecordAuditLogRequest request) {
        log.debug("Recording audit log: {}", request.getOperationType());
        
        AuditLog auditLog = AuditLog.builder()
                .userId(request.getUserId())
                .operationType(request.getOperationType())
                .operationCategory(request.getOperationCategory())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .description(request.getDescription())
                .beforeData(request.getBeforeData())
                .afterData(request.getAfterData())
                .operationData(request.getOperationData())
                .ipAddress(getCurrentIpAddress())
                .userAgent(getCurrentUserAgent())
                .sessionId(getCurrentSessionId())
                .riskLevel(determineRiskLevel(request.getOperationType(), request.getOperationCategory()))
                .success(request.getSuccess())
                .errorMessage(request.getErrorMessage())
                .executionTime(request.getExecutionTime())
                .build();
        
        auditLog = auditLogRepository.save(auditLog);
        
        log.debug("Audit log recorded successfully: {}", auditLog.getId());
        return auditLog;
    }

    @Override
    @Async
    @Transactional
    public void recordAuditLogAsync(RecordAuditLogRequest request) {
        try {
            recordAuditLog(request);
        } catch (Exception e) {
            log.error("Failed to record audit log asynchronously", e);
        }
    }

    @Override
    @Transactional
    public List<AuditLog> batchRecordAuditLogs(List<RecordAuditLogRequest> requests) {
        log.debug("Recording batch audit logs: {} items", requests.size());
        
        List<AuditLog> auditLogs = requests.stream()
                .map(this::recordAuditLog)
                .collect(Collectors.toList());
        
        log.debug("Batch audit logs recorded successfully: {} items", auditLogs.size());
        return auditLogs;
    }

    @Override
    @Transactional
    public AuditLog recordKnowledgeBaseOperation(String userId, String kbId, AuditLog.OperationType operationType, 
                                                String description, Object beforeData, Object afterData) {
        log.debug("Recording knowledge base operation: userId={}, kbId={}, operation={}", userId, kbId, operationType);
        
        RecordAuditLogRequest request = RecordAuditLogRequest.builder()
                .userId(userId)
                .operationType(operationType)
                .operationCategory(AuditLog.OperationCategory.KNOWLEDGE_BASE)
                .resourceType("KNOWLEDGE_BASE")
                .resourceId(kbId)
                .description(description)
                .beforeData(beforeData)
                .afterData(afterData)
                .success(true)
                .build();
        
        return recordAuditLog(request);
    }

    @Override
    @Transactional
    public AuditLog recordDocumentNodeOperation(String userId, String nodeId, AuditLog.OperationType operationType, 
                                               String description, Object beforeData, Object afterData) {
        log.debug("Recording document node operation: userId={}, nodeId={}, operation={}", userId, nodeId, operationType);
        
        RecordAuditLogRequest request = RecordAuditLogRequest.builder()
                .userId(userId)
                .operationType(operationType)
                .operationCategory(AuditLog.OperationCategory.DOCUMENT)
                .resourceType("DOCUMENT_NODE")
                .resourceId(nodeId)
                .description(description)
                .beforeData(beforeData)
                .afterData(afterData)
                .success(true)
                .build();
        
        return recordAuditLog(request);
    }

    @Override
    @Transactional
    public AuditLog recordPermissionOperation(String userId, String resourceId, String resourceType, 
                                             AuditLog.OperationType operationType, String description) {
        log.debug("Recording permission operation: userId={}, resourceId={}, operation={}", userId, resourceId, operationType);
        
        RecordAuditLogRequest request = RecordAuditLogRequest.builder()
                .userId(userId)
                .operationType(operationType)
                .operationCategory(AuditLog.OperationCategory.SECURITY)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .success(true)
                .build();
        
        return recordAuditLog(request);
    }

    @Override
    @Transactional
    public AuditLog recordEmbeddingOperation(String userId, String nodeId, AuditLog.OperationType operationType, 
                                           String description, Map<String, Object> operationData) {
        log.debug("Recording embedding operation: userId={}, nodeId={}, operation={}", userId, nodeId, operationType);
        
        RecordAuditLogRequest request = RecordAuditLogRequest.builder()
                .userId(userId)
                .operationType(operationType)
                .operationCategory(AuditLog.OperationCategory.EMBEDDING)
                .resourceType("DOCUMENT_NODE")
                .resourceId(nodeId)
                .description(description)
                .operationData(operationData)
                .success(true)
                .build();
        
        return recordAuditLog(request);
    }

    @Override
    public Page<AuditLogDTO> getAuditLogs(AuditLogQueryRequest request, Pageable pageable) {
        log.debug("Getting audit logs with query: {}", request);
        
        Page<AuditLog> auditLogs = auditLogRepository.findAuditLogs(
                request.getUserId(),
                request.getOperationType(),
                request.getOperationCategory(),
                request.getResourceType(),
                request.getResourceId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getSuccess(),
                request.getRiskLevel(),
                pageable
        );
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> getAuditLogsByUser(String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        log.debug("Getting audit logs by user: userId={}", userId);
        
        Page<AuditLog> auditLogs = auditLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId, startTime, endTime, pageable);
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> getAuditLogsByResource(String resourceType, String resourceId, Pageable pageable) {
        log.debug("Getting audit logs by resource: resourceType={}, resourceId={}", resourceType, resourceId);
        
        Page<AuditLog> auditLogs = auditLogRepository.findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
                resourceType, resourceId, pageable);
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> getAuditLogsByOperation(AuditLog.OperationType operationType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        log.debug("Getting audit logs by operation: operationType={}", operationType);
        
        Page<AuditLog> auditLogs = auditLogRepository.findByOperationTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                operationType, startTime, endTime, pageable);
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> getSensitiveOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        log.debug("Getting sensitive operation logs");
        
        List<AuditLog.OperationType> sensitiveOperations = List.of(
                AuditLog.OperationType.DELETE,
                AuditLog.OperationType.GRANT_PERMISSION,
                AuditLog.OperationType.REVOKE_PERMISSION,
                AuditLog.OperationType.TRANSFER_OWNERSHIP,
                AuditLog.OperationType.EXPORT,
                AuditLog.OperationType.BACKUP,
                AuditLog.OperationType.RESTORE
        );
        
        Page<AuditLog> auditLogs = auditLogRepository.findByOperationTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
                sensitiveOperations, startTime, endTime, pageable);
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> getFailedOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        log.debug("Getting failed operation logs");
        
        Page<AuditLog> auditLogs = auditLogRepository.findBySuccessFalseAndCreatedAtBetweenOrderByCreatedAtDesc(
                startTime, endTime, pageable);
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> getAuditLogsByRiskLevel(AuditLog.RiskLevel riskLevel, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        log.debug("Getting audit logs by risk level: riskLevel={}", riskLevel);
        
        Page<AuditLog> auditLogs = auditLogRepository.findByRiskLevelAndCreatedAtBetweenOrderByCreatedAtDesc(
                riskLevel, startTime, endTime, pageable);
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public Page<AuditLogDTO> searchAuditLogs(AuditLogSearchRequest request, Pageable pageable) {
        log.debug("Searching audit logs with keyword: {}", request.getKeyword());
        
        Page<AuditLog> auditLogs = auditLogRepository.searchAuditLogs(
                request.getKeyword(),
                request.getStartTime(),
                request.getEndTime(),
                pageable
        );
        
        return auditLogs.map(this::convertToDTO);
    }

    @Override
    public AuditReportDTO generateAuditReport(AuditReportRequest request) {
        log.info("Generating audit report: {}", request.getReportType());
        
        List<AuditLog> auditLogs = auditLogRepository.findByCreatedAtBetween(
                request.getStartTime(), request.getEndTime());
        
        long totalOperations = auditLogs.size();
        long successfulOperations = auditLogs.stream()
                .mapToLong(log -> log.getSuccess() ? 1 : 0)
                .sum();
        long failedOperations = totalOperations - successfulOperations;
        
        Map<AuditLog.OperationType, Long> operationCounts = auditLogs.stream()
                .collect(Collectors.groupingBy(AuditLog::getOperationType, Collectors.counting()));
        
        Map<String, Long> userActivityCounts = auditLogs.stream()
                .collect(Collectors.groupingBy(AuditLog::getUserId, Collectors.counting()));
        
        Map<AuditLog.RiskLevel, Long> riskLevelCounts = auditLogs.stream()
                .collect(Collectors.groupingBy(AuditLog::getRiskLevel, Collectors.counting()));
        
        return AuditReportDTO.builder()
                .reportType(request.getReportType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalOperations(totalOperations)
                .successfulOperations(successfulOperations)
                .failedOperations(failedOperations)
                .successRate(totalOperations > 0 ? (double) successfulOperations / totalOperations * 100 : 0)
                .operationCounts(operationCounts)
                .userActivityCounts(userActivityCounts)
                .riskLevelCounts(riskLevelCounts)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public AuditStatisticsDTO getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Getting audit statistics: startTime={}, endTime={}", startTime, endTime);
        
        List<AuditLog> auditLogs = auditLogRepository.findByCreatedAtBetween(startTime, endTime);
        
        long totalOperations = auditLogs.size();
        long uniqueUsers = auditLogs.stream()
                .map(AuditLog::getUserId)
                .distinct()
                .count();
        long uniqueResources = auditLogs.stream()
                .map(log -> log.getResourceType() + ":" + log.getResourceId())
                .distinct()
                .count();
        
        double averageExecutionTime = auditLogs.stream()
                .filter(log -> log.getExecutionTime() != null)
                .mapToLong(AuditLog::getExecutionTime)
                .average()
                .orElse(0.0);
        
        return AuditStatisticsDTO.builder()
                .startTime(startTime)
                .endTime(endTime)
                .totalOperations(totalOperations)
                .uniqueUsers(uniqueUsers)
                .uniqueResources(uniqueResources)
                .averageExecutionTime(averageExecutionTime)
                .build();
    }

    // 辅助方法
    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get IP address", e);
        }
        return "unknown";
    }
    
    private String getCurrentUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Failed to get user agent", e);
        }
        return "unknown";
    }
    
    private String getCurrentSessionId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getSession().getId();
            }
        } catch (Exception e) {
            log.warn("Failed to get session ID", e);
        }
        return "unknown";
    }
    
    private AuditLog.RiskLevel determineRiskLevel(AuditLog.OperationType operationType, AuditLog.OperationCategory category) {
        // 根据操作类型和类别确定风险级别
        switch (operationType) {
            case DELETE:
            case REVOKE_PERMISSION:
            case TRANSFER_OWNERSHIP:
                return AuditLog.RiskLevel.HIGH;
            case GRANT_PERMISSION:
            case UPDATE:
            case MOVE:
                return AuditLog.RiskLevel.MEDIUM;
            case CREATE:
            case READ:
                return AuditLog.RiskLevel.LOW;
            default:
                return AuditLog.RiskLevel.MEDIUM;
        }
    }
    
    private AuditLogDTO convertToDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .operationType(auditLog.getOperationType())
                .operationCategory(auditLog.getOperationCategory())
                .resourceType(auditLog.getResourceType())
                .resourceId(auditLog.getResourceId())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .sessionId(auditLog.getSessionId())
                .riskLevel(auditLog.getRiskLevel())
                .success(auditLog.getSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .executionTime(auditLog.getExecutionTime())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    // 省略其他接口方法的实现...
    @Override public UserActivityReportDTO generateUserActivityReport(String userId, LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public ResourceAccessReportDTO generateResourceAccessReport(String resourceType, String resourceId, LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public SecurityEventReportDTO generateSecurityEventReport(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public OperationPatternAnalysisDTO analyzeOperationPatterns(String userId, LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<AnomalousActivityDTO> detectAnomalousActivity(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public ComplianceReportDTO generateComplianceReport(String organizationId, LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<OperationTypeStatisticsDTO> getOperationTypeStatistics(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<UserOperationStatisticsDTO> getUserOperationStatistics(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<ResourceAccessStatisticsDTO> getResourceAccessStatistics(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<TimeDistributionStatisticsDTO> getTimeDistributionStatistics(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public RiskOperationStatisticsDTO getRiskOperationStatistics(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void updateAuditConfiguration(AuditConfigurationRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditConfigurationDTO getAuditConfiguration() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void setAuditRules(List<AuditRuleRequest> rules) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<AuditRuleDTO> getAuditRules() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public CleanupAuditLogResultDTO cleanupExpiredAuditLogs(Integer retentionDays) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public ArchiveAuditLogResultDTO archiveAuditLogs(LocalDateTime beforeDate) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditLogExportDTO exportAuditLogs(AuditLogExportRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditLogBackupDTO backupAuditLogs(AuditLogBackupRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditLogRestoreResultDTO restoreAuditLogs(AuditLogRestoreRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public RealTimeAuditMonitorDTO getRealTimeAuditMonitor() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void setAuditAlertRules(List<AuditAlertRuleRequest> rules) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<AuditAlertDTO> getAuditAlerts(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void acknowledgeAuditAlert(String alertId, String acknowledgedBy) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditIntegrityVerificationDTO verifyAuditIntegrity(LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditProofDTO generateAuditProof(String auditLogId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public AuditProofVerificationDTO verifyAuditProof(String auditProof) { throw new UnsupportedOperationException("Not implemented yet"); }
} 