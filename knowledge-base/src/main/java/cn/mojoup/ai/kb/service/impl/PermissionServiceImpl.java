package cn.mojoup.ai.kb.service.impl;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.KnowledgeBasePermission;
import cn.mojoup.ai.kb.entity.DocumentNodePermission;
import cn.mojoup.ai.kb.repository.KnowledgeBasePermissionRepository;
import cn.mojoup.ai.kb.repository.DocumentNodePermissionRepository;
import cn.mojoup.ai.kb.service.AuditService;
import cn.mojoup.ai.kb.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final KnowledgeBasePermissionRepository knowledgeBasePermissionRepository;
    private final DocumentNodePermissionRepository documentNodePermissionRepository;
    private final AuditService auditService;

    @Override
    @Cacheable(value = "knowledgeBaseAccess", key = "#kbId + '_' + #userId + '_' + #action")
    public boolean checkKnowledgeBaseAccess(String kbId, String userId, String action) {
        log.debug("Checking knowledge base access: kbId={}, userId={}, action={}", kbId, userId, action);
        
        // 获取用户对知识库的有效权限
        EffectivePermissionDTO permission = getEffectiveKnowledgeBasePermission(kbId, userId);
        
        // 检查是否有所需的权限
        return hasRequiredPermission(permission.getPermissions(), action);
    }

    @Override
    @Cacheable(value = "effectiveKnowledgeBasePermission", key = "#kbId + '_' + #userId")
    public EffectivePermissionDTO getEffectiveKnowledgeBasePermission(String kbId, String userId) {
        log.debug("Getting effective knowledge base permission: kbId={}, userId={}", kbId, userId);
        
        List<KnowledgeBasePermission> permissions = knowledgeBasePermissionRepository
                .findByKnowledgeBaseIdAndActivatedTrue(kbId);
        
        Set<String> effectivePermissions = new HashSet<>();
        String highestRole = null;
        LocalDateTime expiresAt = null;
        
        for (KnowledgeBasePermission permission : permissions) {
            if (isPermissionApplicableToUser(permission, userId)) {
                // 检查权限是否过期
                if (permission.getExpiresAt() != null && permission.getExpiresAt().isBefore(LocalDateTime.now())) {
                    continue;
                }
                
                effectivePermissions.addAll(permission.getPermissions());
                
                // 跟踪最高权限级别和最早过期时间
                if (highestRole == null || isHigherRole(permission.getRole(), highestRole)) {
                    highestRole = permission.getRole();
                }
                
                if (permission.getExpiresAt() != null) {
                    if (expiresAt == null || permission.getExpiresAt().isBefore(expiresAt)) {
                        expiresAt = permission.getExpiresAt();
                    }
                }
            }
        }
        
        return EffectivePermissionDTO.builder()
                .resourceId(kbId)
                .resourceType("KNOWLEDGE_BASE")
                .userId(userId)
                .permissions(effectivePermissions)
                .role(highestRole)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional
    public KnowledgeBasePermission grantKnowledgeBasePermission(String kbId, GrantKnowledgeBasePermissionRequest request) {
        log.info("Granting knowledge base permission: kbId={}, principal={}", kbId, request.getPrincipalId());
        
        String currentUserId = getCurrentUserId();
        
        // 检查是否已存在相同的权限
        Optional<KnowledgeBasePermission> existingPermission = knowledgeBasePermissionRepository
                .findByKnowledgeBaseIdAndPrincipalTypeAndPrincipalIdAndActivatedTrue(
                        kbId, request.getPrincipalType(), request.getPrincipalId());
        
        if (existingPermission.isPresent()) {
            throw new IllegalArgumentException("Permission already exists for this principal");
        }
        
        // 创建新权限
        KnowledgeBasePermission permission = KnowledgeBasePermission.builder()
                .knowledgeBaseId(kbId)
                .principalType(request.getPrincipalType())
                .principalId(request.getPrincipalId())
                .principalName(request.getPrincipalName())
                .permissions(request.getPermissions())
                .role(request.getRole())
                .expiresAt(request.getExpiresAt())
                .activated(true)
                .grantedBy(currentUserId)
                .build();
        
        permission = knowledgeBasePermissionRepository.save(permission);
        
        // 记录审计日志
        auditService.recordPermissionOperation(
                currentUserId,
                kbId,
                "KNOWLEDGE_BASE",
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.GRANT_PERMISSION,
                String.format("Granted permission to %s: %s", request.getPrincipalType(), request.getPrincipalId())
        );
        
        log.info("Knowledge base permission granted successfully: {}", permission.getId());
        return permission;
    }

    @Override
    @Transactional
    public void revokeKnowledgeBasePermission(String permissionId) {
        log.info("Revoking knowledge base permission: {}", permissionId);
        
        String currentUserId = getCurrentUserId();
        KnowledgeBasePermission permission = knowledgeBasePermissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
        
        permission.setActivated(false);
        permission.setRevokedBy(currentUserId);
        permission.setRevokedAt(LocalDateTime.now());
        
        knowledgeBasePermissionRepository.save(permission);
        
        // 记录审计日志
        auditService.recordPermissionOperation(
                currentUserId,
                permission.getKnowledgeBaseId(),
                "KNOWLEDGE_BASE",
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.REVOKE_PERMISSION,
                String.format("Revoked permission from %s: %s", permission.getPrincipalType(), permission.getPrincipalId())
        );
        
        log.info("Knowledge base permission revoked successfully: {}", permissionId);
    }

    @Override
    public Page<KnowledgeBasePermissionDTO> getKnowledgeBasePermissions(String kbId, Pageable pageable) {
        log.debug("Getting knowledge base permissions: kbId={}", kbId);
        
        Page<KnowledgeBasePermission> permissions = knowledgeBasePermissionRepository
                .findByKnowledgeBaseIdAndActivatedTrueOrderByCreatedAtDesc(kbId, pageable);
        
        return permissions.map(this::convertToKnowledgeBasePermissionDTO);
    }

    @Override
    @Cacheable(value = "documentNodeAccess", key = "#nodeId + '_' + #userId + '_' + #action")
    public boolean checkDocumentNodeAccess(String nodeId, String userId, String action) {
        log.debug("Checking document node access: nodeId={}, userId={}, action={}", nodeId, userId, action);
        
        // 获取用户对文档节点的有效权限
        EffectivePermissionDTO permission = getEffectiveDocumentNodePermission(nodeId, userId);
        
        // 检查是否有所需的权限
        return hasRequiredPermission(permission.getPermissions(), action);
    }

    @Override
    @Cacheable(value = "effectiveDocumentNodePermission", key = "#nodeId + '_' + #userId")
    public EffectivePermissionDTO getEffectiveDocumentNodePermission(String nodeId, String userId) {
        log.debug("Getting effective document node permission: nodeId={}, userId={}", nodeId, userId);
        
        List<DocumentNodePermission> permissions = documentNodePermissionRepository
                .findByNodeIdAndActivatedTrue(nodeId);
        
        Set<String> effectivePermissions = new HashSet<>();
        String highestRole = null;
        LocalDateTime expiresAt = null;
        
        for (DocumentNodePermission permission : permissions) {
            if (isPermissionApplicableToUser(permission, userId)) {
                // 检查权限是否过期
                if (permission.getExpiresAt() != null && permission.getExpiresAt().isBefore(LocalDateTime.now())) {
                    continue;
                }
                
                effectivePermissions.addAll(permission.getPermissions());
                
                // 跟踪最高权限级别和最早过期时间
                if (highestRole == null || isHigherRole(permission.getRole(), highestRole)) {
                    highestRole = permission.getRole();
                }
                
                if (permission.getExpiresAt() != null) {
                    if (expiresAt == null || permission.getExpiresAt().isBefore(expiresAt)) {
                        expiresAt = permission.getExpiresAt();
                    }
                }
            }
        }
        
        // 如果节点没有直接权限，检查继承权限
        if (effectivePermissions.isEmpty()) {
            // TODO: 实现权限继承逻辑
        }
        
        return EffectivePermissionDTO.builder()
                .resourceId(nodeId)
                .resourceType("DOCUMENT_NODE")
                .userId(userId)
                .permissions(effectivePermissions)
                .role(highestRole)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional
    public DocumentNodePermission grantDocumentNodePermission(String nodeId, GrantDocumentNodePermissionRequest request) {
        log.info("Granting document node permission: nodeId={}, principal={}", nodeId, request.getPrincipalId());
        
        String currentUserId = getCurrentUserId();
        
        // 检查是否已存在相同的权限
        Optional<DocumentNodePermission> existingPermission = documentNodePermissionRepository
                .findByNodeIdAndPrincipalTypeAndPrincipalIdAndActivatedTrue(
                        nodeId, request.getPrincipalType(), request.getPrincipalId());
        
        if (existingPermission.isPresent()) {
            throw new IllegalArgumentException("Permission already exists for this principal");
        }
        
        // 创建新权限
        DocumentNodePermission permission = DocumentNodePermission.builder()
                .nodeId(nodeId)
                .principalType(request.getPrincipalType())
                .principalId(request.getPrincipalId())
                .principalName(request.getPrincipalName())
                .permissions(request.getPermissions())
                .role(request.getRole())
                .inheritFromParent(request.getInheritFromParent())
                .applyToChildren(request.getApplyToChildren())
                .expiresAt(request.getExpiresAt())
                .activated(true)
                .grantedBy(currentUserId)
                .build();
        
        permission = documentNodePermissionRepository.save(permission);
        
        // 如果设置了应用到子节点，递归应用权限
        if (request.getApplyToChildren()) {
            applyPermissionToChildren(nodeId, permission);
        }
        
        // 记录审计日志
        auditService.recordPermissionOperation(
                currentUserId,
                nodeId,
                "DOCUMENT_NODE",
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.GRANT_PERMISSION,
                String.format("Granted permission to %s: %s", request.getPrincipalType(), request.getPrincipalId())
        );
        
        log.info("Document node permission granted successfully: {}", permission.getId());
        return permission;
    }

    // 辅助方法
    private boolean isPermissionApplicableToUser(KnowledgeBasePermission permission, String userId) {
        switch (permission.getPrincipalType()) {
            case USER:
                return userId.equals(permission.getPrincipalId());
            case ROLE:
                return userHasRole(userId, permission.getPrincipalId());
            case DEPARTMENT:
                return userInDepartment(userId, permission.getPrincipalId());
            case ORGANIZATION:
                return userInOrganization(userId, permission.getPrincipalId());
            default:
                return false;
        }
    }
    
    private boolean isPermissionApplicableToUser(DocumentNodePermission permission, String userId) {
        switch (permission.getPrincipalType()) {
            case USER:
                return userId.equals(permission.getPrincipalId());
            case ROLE:
                return userHasRole(userId, permission.getPrincipalId());
            case DEPARTMENT:
                return userInDepartment(userId, permission.getPrincipalId());
            case ORGANIZATION:
                return userInOrganization(userId, permission.getPrincipalId());
            default:
                return false;
        }
    }
    
    private boolean hasRequiredPermission(Set<String> userPermissions, String requiredAction) {
        // 权限映射表
        Map<String, Set<String>> permissionMapping = Map.of(
                "READ", Set.of("READ", "WRITE", "MANAGE", "ADMIN"),
                "WRITE", Set.of("WRITE", "MANAGE", "ADMIN"),
                "DELETE", Set.of("MANAGE", "ADMIN"),
                "MANAGE", Set.of("MANAGE", "ADMIN"),
                "ADMIN", Set.of("ADMIN")
        );
        
        Set<String> allowedPermissions = permissionMapping.getOrDefault(requiredAction, Set.of(requiredAction));
        
        return userPermissions.stream().anyMatch(allowedPermissions::contains);
    }
    
    private boolean isHigherRole(String role1, String role2) {
        // 角色优先级：ADMIN > MANAGER > EDITOR > VIEWER
        Map<String, Integer> rolePriority = Map.of(
                "VIEWER", 1,
                "EDITOR", 2,
                "MANAGER", 3,
                "ADMIN", 4
        );
        
        return rolePriority.getOrDefault(role1, 0) > rolePriority.getOrDefault(role2, 0);
    }
    
    private boolean userHasRole(String userId, String roleId) {
        // TODO: 实现角色检查逻辑，需要集成用户管理系统
        return false;
    }
    
    private boolean userInDepartment(String userId, String departmentId) {
        // TODO: 实现部门检查逻辑，需要集成组织架构系统
        return false;
    }
    
    private boolean userInOrganization(String userId, String organizationId) {
        // TODO: 实现组织检查逻辑，需要集成组织架构系统
        return false;
    }
    
    private void applyPermissionToChildren(String parentNodeId, DocumentNodePermission permission) {
        // TODO: 实现递归权限应用逻辑
        log.info("Applying permission to children of node: {}", parentNodeId);
    }
    
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private KnowledgeBasePermissionDTO convertToKnowledgeBasePermissionDTO(KnowledgeBasePermission permission) {
        return KnowledgeBasePermissionDTO.builder()
                .id(permission.getId())
                .knowledgeBaseId(permission.getKnowledgeBaseId())
                .principalType(permission.getPrincipalType())
                .principalId(permission.getPrincipalId())
                .principalName(permission.getPrincipalName())
                .permissions(permission.getPermissions())
                .role(permission.getRole())
                .expiresAt(permission.getExpiresAt())
                .activated(permission.getActivated())
                .grantedBy(permission.getGrantedBy())
                .grantedAt(permission.getCreatedAt())
                .build();
    }

    // 省略其他接口方法的实现...
    @Override public KnowledgeBasePermission updateKnowledgeBasePermission(String permissionId, UpdateKnowledgeBasePermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public BatchPermissionResultDTO batchGrantKnowledgeBasePermissions(String kbId, BatchGrantPermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public BatchPermissionResultDTO batchRevokeKnowledgeBasePermissions(List<String> permissionIds) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void revokeDocumentNodePermission(String permissionId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentNodePermission updateDocumentNodePermission(String permissionId, UpdateDocumentNodePermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<DocumentNodePermissionDTO> getDocumentNodePermissions(String nodeId, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void inheritParentPermissions(String parentNodeId, String childNodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public BatchPermissionResultDTO batchSetPermissionInheritance(List<String> nodeIds, boolean inherit) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<UserPermissionSummaryDTO> getUserPermissions(String userId, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<RolePermissionSummaryDTO> getRolePermissions(String roleId, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<DepartmentPermissionSummaryDTO> getDepartmentPermissions(String departmentId, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<PermissionConflictDTO> analyzePermissionConflicts(String resourceId, String resourceType) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<PermissionInheritanceDTO> getPermissionInheritanceChain(String nodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public PermissionImpactAnalysisDTO analyzePermissionImpact(String permissionId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public PermissionTemplateDTO createPermissionTemplate(CreatePermissionTemplateRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public ApplyPermissionTemplateResultDTO applyPermissionTemplate(String resourceId, String templateId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<PermissionTemplateDTO> getPermissionTemplates() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void deletePermissionTemplate(String templateId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<PermissionChangeLogDTO> getPermissionChangeLog(String resourceId, String resourceType, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public PermissionComplianceReportDTO checkPermissionCompliance(String organizationId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<AnomalousPermissionDTO> detectAnomalousPermissions(String organizationId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public PermissionReportDTO generatePermissionReport(PermissionReportRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public CleanupExpiredPermissionsResultDTO cleanupExpiredPermissions() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public SyncPermissionStatusResultDTO syncPermissionStatus(String resourceId, String resourceType) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public PermissionValidationResultDTO validatePermissionConfiguration(String resourceId, String resourceType) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public PermissionStatisticsDTO getPermissionStatistics(String organizationId) { throw new UnsupportedOperationException("Not implemented yet"); }
} 