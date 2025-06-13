package cn.mojoup.ai.kb.service;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.KnowledgeBasePermission;
import cn.mojoup.ai.kb.entity.DocumentNodePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 权限管理服务接口
 * 负责知识库和文档节点的权限控制
 *
 * @author matt
 */
public interface PermissionService {

    // ==================== 知识库权限管理 ====================

    /**
     * 检查用户对知识库的访问权限
     */
    boolean checkKnowledgeBaseAccess(String kbId, String userId, String action);

    /**
     * 获取用户对知识库的有效权限
     */
    EffectivePermissionDTO getEffectiveKnowledgeBasePermission(String kbId, String userId);

    /**
     * 授予知识库权限
     */
    KnowledgeBasePermission grantKnowledgeBasePermission(String kbId, GrantKnowledgeBasePermissionRequest request);

    /**
     * 撤销知识库权限
     */
    void revokeKnowledgeBasePermission(String permissionId);

    /**
     * 更新知识库权限
     */
    KnowledgeBasePermission updateKnowledgeBasePermission(String permissionId, UpdateKnowledgeBasePermissionRequest request);

    /**
     * 获取知识库的所有权限
     */
    Page<KnowledgeBasePermissionDTO> getKnowledgeBasePermissions(String kbId, Pageable pageable);

    /**
     * 批量授予知识库权限
     */
    BatchPermissionResultDTO batchGrantKnowledgeBasePermissions(String kbId, BatchGrantPermissionRequest request);

    /**
     * 批量撤销知识库权限
     */
    BatchPermissionResultDTO batchRevokeKnowledgeBasePermissions(List<String> permissionIds);

    // ==================== 文档节点权限管理 ====================

    /**
     * 检查用户对文档节点的访问权限
     */
    boolean checkDocumentNodeAccess(String nodeId, String userId, String action);

    /**
     * 获取用户对文档节点的有效权限
     */
    EffectivePermissionDTO getEffectiveDocumentNodePermission(String nodeId, String userId);

    /**
     * 授予文档节点权限
     */
    DocumentNodePermission grantDocumentNodePermission(String nodeId, GrantDocumentNodePermissionRequest request);

    /**
     * 撤销文档节点权限
     */
    void revokeDocumentNodePermission(String permissionId);

    /**
     * 更新文档节点权限
     */
    DocumentNodePermission updateDocumentNodePermission(String permissionId, UpdateDocumentNodePermissionRequest request);

    /**
     * 获取文档节点的所有权限
     */
    Page<DocumentNodePermissionDTO> getDocumentNodePermissions(String nodeId, Pageable pageable);

    /**
     * 继承父节点权限到子节点
     */
    void inheritParentPermissions(String parentNodeId, String childNodeId);

    /**
     * 批量设置节点权限继承
     */
    BatchPermissionResultDTO batchSetPermissionInheritance(List<String> nodeIds, boolean inherit);

    // ==================== 权限查询和分析 ====================

    /**
     * 查询用户的所有权限
     */
    Page<UserPermissionSummaryDTO> getUserPermissions(String userId, Pageable pageable);

    /**
     * 查询角色的所有权限
     */
    Page<RolePermissionSummaryDTO> getRolePermissions(String roleId, Pageable pageable);

    /**
     * 查询部门的所有权限
     */
    Page<DepartmentPermissionSummaryDTO> getDepartmentPermissions(String departmentId, Pageable pageable);

    /**
     * 分析权限冲突
     */
    List<PermissionConflictDTO> analyzePermissionConflicts(String resourceId, String resourceType);

    /**
     * 获取权限继承链
     */
    List<PermissionInheritanceDTO> getPermissionInheritanceChain(String nodeId);

    /**
     * 权限影响分析
     */
    PermissionImpactAnalysisDTO analyzePermissionImpact(String permissionId);

    // ==================== 权限模板管理 ====================

    /**
     * 创建权限模板
     */
    PermissionTemplateDTO createPermissionTemplate(CreatePermissionTemplateRequest request);

    /**
     * 应用权限模板
     */
    ApplyPermissionTemplateResultDTO applyPermissionTemplate(String resourceId, String templateId);

    /**
     * 获取权限模板列表
     */
    List<PermissionTemplateDTO> getPermissionTemplates();

    /**
     * 删除权限模板
     */
    void deletePermissionTemplate(String templateId);

    // ==================== 权限审计和监控 ====================

    /**
     * 获取权限变更历史
     */
    Page<PermissionChangeLogDTO> getPermissionChangeLog(String resourceId, String resourceType, Pageable pageable);

    /**
     * 权限合规检查
     */
    PermissionComplianceReportDTO checkPermissionCompliance(String organizationId);

    /**
     * 检测异常权限
     */
    List<AnomalousPermissionDTO> detectAnomalousPermissions(String organizationId);

    /**
     * 生成权限报告
     */
    PermissionReportDTO generatePermissionReport(PermissionReportRequest request);

    // ==================== 权限工具方法 ====================

    /**
     * 清理过期权限
     */
    CleanupExpiredPermissionsResultDTO cleanupExpiredPermissions();

    /**
     * 同步权限状态
     */
    SyncPermissionStatusResultDTO syncPermissionStatus(String resourceId, String resourceType);

    /**
     * 验证权限配置
     */
    PermissionValidationResultDTO validatePermissionConfiguration(String resourceId, String resourceType);

    /**
     * 获取权限统计信息
     */
    PermissionStatisticsDTO getPermissionStatistics(String organizationId);
} 