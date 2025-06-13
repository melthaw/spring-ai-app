package cn.mojoup.ai.kb.service;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.DocumentNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文档节点服务接口
 * 负责文档树形结构的管理
 *
 * @author matt
 */
public interface DocumentNodeService {

    /**
     * 创建文档节点（文件夹或文档）
     */
    DocumentNode createDocumentNode(String kbId, CreateDocumentNodeRequest request);

    /**
     * 更新文档节点
     */
    DocumentNode updateDocumentNode(String nodeId, UpdateDocumentNodeRequest request);

    /**
     * 删除文档节点（软删除）
     */
    void deleteDocumentNode(String nodeId);

    /**
     * 根据ID获取文档节点
     */
    DocumentNodeDetailDTO getDocumentNodeById(String nodeId);

    /**
     * 获取知识库的文档树
     */
    List<DocumentNodeTreeDTO> getDocumentTree(String kbId);

    /**
     * 获取指定节点的子节点
     */
    List<DocumentNodeListDTO> getChildNodes(String parentId);

    /**
     * 分页查询用户可访问的文档节点
     */
    Page<DocumentNodeListDTO> getAccessibleNodes(String kbId, Pageable pageable);

    /**
     * 移动文档节点
     */
    DocumentNode moveDocumentNode(String nodeId, MoveDocumentNodeRequest request);

    /**
     * 复制文档节点
     */
    DocumentNode copyDocumentNode(String nodeId, CopyDocumentNodeRequest request);

    /**
     * 重命名文档节点
     */
    DocumentNode renameDocumentNode(String nodeId, String newName);

    /**
     * 搜索文档节点
     */
    Page<DocumentNodeListDTO> searchDocumentNodes(String kbId, DocumentNodeSearchRequest request, Pageable pageable);

    /**
     * 检查用户是否有权限访问节点
     */
    boolean hasAccessPermission(String nodeId, String userId);

    /**
     * 检查用户是否有权限管理节点
     */
    boolean hasManagePermission(String nodeId, String userId);

    /**
     * 获取节点的权限列表
     */
    List<DocumentNodePermissionDTO> getNodePermissions(String nodeId);

    /**
     * 授予节点权限
     */
    void grantNodePermission(String nodeId, GrantNodePermissionRequest request);

    /**
     * 撤销节点权限
     */
    void revokeNodePermission(String nodeId, String permissionId);

    /**
     * 批量更新节点权限
     */
    void batchUpdateNodePermissions(String nodeId, BatchNodePermissionRequest request);

    /**
     * 获取节点统计信息
     */
    DocumentNodeStatsDTO getNodeStats(String nodeId);

    /**
     * 获取节点路径
     */
    List<DocumentNodeBreadcrumbDTO> getNodePath(String nodeId);

    /**
     * 验证节点名称是否可用
     */
    boolean isNodeNameAvailable(String kbId, String parentId, String nodeName, String excludeId);

    /**
     * 批量删除节点
     */
    BatchDeleteNodeResultDTO batchDeleteNodes(List<String> nodeIds);

    /**
     * 从上传的文件创建文档节点
     */
    DocumentNode createDocumentFromUpload(String kbId, String parentId, CreateDocumentFromUploadRequest request);

    /**
     * 更新文档节点的文件信息
     */
    DocumentNode updateDocumentFile(String nodeId, UpdateDocumentFileRequest request);

    /**
     * 获取文档预览
     */
    DocumentPreviewDTO getDocumentPreview(String nodeId);

    /**
     * 获取文档版本历史
     */
    Page<DocumentVersionDTO> getDocumentVersions(String nodeId, Pageable pageable);

    /**
     * 恢复文档版本
     */
    DocumentNode restoreDocumentVersion(String nodeId, String versionId);
} 