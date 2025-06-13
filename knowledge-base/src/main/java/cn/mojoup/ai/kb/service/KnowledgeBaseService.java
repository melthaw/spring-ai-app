package cn.mojoup.ai.kb.service;

import cn.mojoup.ai.kb.entity.KnowledgeBase;
import cn.mojoup.ai.kb.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理服务接口
 *
 * @author matt
 */
public interface KnowledgeBaseService {

    /**
     * 创建知识库
     */
    KnowledgeBase createKnowledgeBase(CreateKnowledgeBaseRequest request);

    /**
     * 更新知识库
     */
    KnowledgeBase updateKnowledgeBase(String kbId, UpdateKnowledgeBaseRequest request);

    /**
     * 删除知识库（软删除）
     */
    void deleteKnowledgeBase(String kbId);

    /**
     * 根据ID获取知识库
     */
    KnowledgeBaseDetailDTO getKnowledgeBaseById(String kbId);

    /**
     * 分页查询用户可访问的知识库
     */
    Page<KnowledgeBaseListDTO> getAccessibleKnowledgeBases(Pageable pageable);

    /**
     * 根据组织查询知识库
     */
    List<KnowledgeBaseListDTO> getKnowledgeBasesByOrganization(String organizationId);

    /**
     * 根据部门查询知识库
     */
    List<KnowledgeBaseListDTO> getKnowledgeBasesByDepartment(String departmentId);

    /**
     * 搜索知识库
     */
    Page<KnowledgeBaseListDTO> searchKnowledgeBases(KnowledgeBaseSearchRequest request, Pageable pageable);

    /**
     * 检查用户是否有权限访问知识库
     */
    boolean hasAccessPermission(String kbId, String userId);

    /**
     * 检查用户是否有管理权限
     */
    boolean hasManagePermission(String kbId, String userId);

    /**
     * 获取知识库统计信息
     */
    KnowledgeBaseStatsDTO getKnowledgeBaseStats(String kbId);

    /**
     * 获取用户的知识库概览
     */
    UserKnowledgeBaseOverviewDTO getUserKnowledgeBaseOverview();

    /**
     * 更新知识库统计信息
     */
    void updateKnowledgeBaseStats(String kbId);

    /**
     * 复制知识库
     */
    KnowledgeBase cloneKnowledgeBase(String sourceKbId, CloneKnowledgeBaseRequest request);

    /**
     * 归档知识库
     */
    void archiveKnowledgeBase(String kbId);

    /**
     * 恢复知识库
     */
    void restoreKnowledgeBase(String kbId);

    /**
     * 导出知识库配置
     */
    KnowledgeBaseExportDTO exportKnowledgeBase(String kbId);

    /**
     * 导入知识库配置
     */
    KnowledgeBase importKnowledgeBase(KnowledgeBaseImportRequest request);

    /**
     * 获取知识库的权限列表
     */
    List<KnowledgeBasePermissionDTO> getKnowledgeBasePermissions(String kbId);

    /**
     * 授予知识库权限
     */
    void grantPermission(String kbId, GrantPermissionRequest request);

    /**
     * 撤销知识库权限
     */
    void revokePermission(String kbId, String permissionId);

    /**
     * 批量更新权限
     */
    void batchUpdatePermissions(String kbId, BatchPermissionRequest request);

    /**
     * 获取知识库维护建议
     */
    List<MaintenanceSuggestionDTO> getMaintenanceSuggestions(String kbId);

    /**
     * 执行知识库维护
     */
    MaintenanceResultDTO performMaintenance(String kbId, MaintenanceRequest request);

    /**
     * 获取知识库活动日志
     */
    Page<ActivityLogDTO> getKnowledgeBaseActivities(String kbId, Pageable pageable);

    /**
     * 验证知识库名称是否可用
     */
    boolean isKnowledgeBaseNameAvailable(String kbName, String organizationId, String excludeId);

    /**
     * 获取推荐的知识库设置
     */
    Map<String, Object> getRecommendedSettings(String kbType, String organizationId);
} 