package cn.mojoup.ai.kb.service.impl;

import cn.mojoup.ai.kb.entity.AuditLog;
import cn.mojoup.ai.kb.repository.AuditLogRepository;
import cn.mojoup.ai.kb.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计服务实现类
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog recordAuditLog(String userId, String operationType, String resourceId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(operationType)
                    .resourceId(resourceId)
                    .description(description)
                    .operationTime(LocalDateTime.now())
                    .ipAddress(getClientIpAddress())
                    .userAgent(getUserAgent())
                    .success(true)
                    .riskLevel(AuditLog.RiskLevel.LOW)
                    .build();

            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
            throw new RuntimeException("Failed to record audit log", e);
        }
    }

    @Override
    @Async
    public void recordAuditLogAsync(String userId, String operationType, String resourceId, String description) {
        try {
            recordAuditLog(userId, operationType, resourceId, description);
        } catch (Exception e) {
            log.error("Failed to record audit log asynchronously", e);
        }
    }

    @Override
    public AuditLog recordKnowledgeBaseOperation(String userId, String kbId, AuditLog.OperationType operationType, 
                                               String description, Object beforeData, Object afterData) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .operationType(operationType.name())
                .resourceType("KNOWLEDGE_BASE")
                .resourceId(kbId)
                .description(description)
                .operationTime(LocalDateTime.now())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .success(true)
                .riskLevel(determineRiskLevel(operationType))
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog recordDocumentNodeOperation(String userId, String nodeId, AuditLog.OperationType operationType, 
                                              String description, Object beforeData, Object afterData) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .operationType(operationType.name())
                .resourceType("DOCUMENT_NODE")
                .resourceId(nodeId)
                .description(description)
                .operationTime(LocalDateTime.now())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .success(true)
                .riskLevel(determineRiskLevel(operationType))
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog recordPermissionOperation(String userId, String resourceId, String resourceType, 
                                            AuditLog.OperationType operationType, String description) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .operationType(operationType.name())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .operationTime(LocalDateTime.now())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .success(true)
                .riskLevel(AuditLog.RiskLevel.MEDIUM)
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Override
    public AuditLog recordEmbeddingOperation(String userId, String nodeId, AuditLog.OperationType operationType, 
                                           String description, Map<String, Object> operationData) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .operationType(operationType.name())
                .resourceType("EMBEDDING")
                .resourceId(nodeId)
                .description(description)
                .operationTime(LocalDateTime.now())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .success(true)
                .riskLevel(AuditLog.RiskLevel.LOW)
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(String userId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        // 简化实现，实际应该使用自定义查询
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByResource(String resourceType, String resourceId, Pageable pageable) {
        // 简化实现，实际应该使用自定义查询
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByOperation(AuditLog.OperationType operationType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        // 简化实现，实际应该使用自定义查询
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getSensitiveOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        // 简化实现，实际应该使用自定义查询
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getFailedOperationLogs(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        // 简化实现，实际应该使用自定义查询
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByRiskLevel(AuditLog.RiskLevel riskLevel, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        // 简化实现，实际应该使用自定义查询
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUserOperations(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.countByUserIdAndTimeBetween(userId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOperationsByType(String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.countByOperationTypeAndTimeBetween(operationType, startTime, endTime);
    }

    @Override
    public void cleanupExpiredAuditLogs(Integer retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        auditLogRepository.deleteByOperationTimeBefore(cutoffTime);
        log.info("Cleaned up audit logs older than {} days", retentionDays);
    }

    // ==================== 私有辅助方法 ====================

    private String getClientIpAddress() {
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
            log.debug("Failed to get client IP address", e);
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Failed to get user agent", e);
        }
        return "unknown";
    }

    private AuditLog.RiskLevel determineRiskLevel(AuditLog.OperationType operationType) {
        switch (operationType) {
            case DELETE:
                return AuditLog.RiskLevel.HIGH;
            case UPDATE:
                return AuditLog.RiskLevel.MEDIUM;
            case CREATE:
            case READ:
            default:
                return AuditLog.RiskLevel.LOW;
        }
    }
} 