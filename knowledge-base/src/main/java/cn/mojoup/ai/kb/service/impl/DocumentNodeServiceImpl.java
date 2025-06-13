package cn.mojoup.ai.kb.service.impl;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.DocumentNode;
import cn.mojoup.ai.kb.mapper.DocumentNodeMapper;
import cn.mojoup.ai.kb.repository.DocumentNodeRepository;
import cn.mojoup.ai.kb.service.AuditService;
import cn.mojoup.ai.kb.service.DocumentNodeService;
import cn.mojoup.ai.kb.service.PermissionService;
import cn.mojoup.ai.kb.repository.KnowledgeBaseRepository;
import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.service.FileInfoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档节点服务实现
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentNodeServiceImpl implements DocumentNodeService {

    private final DocumentNodeRepository documentNodeRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final FileInfoStorageService fileInfoStorageService;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final DocumentNodeMapper documentNodeMapper;

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#kbId, authentication.name, 'CREATE_DOCUMENT')")
    public DocumentNode createDocumentNode(String kbId, CreateDocumentNodeRequest request) {
        log.info("Creating document node: {} in knowledge base: {}", request.getName(), kbId);
        
        String currentUserId = getCurrentUserId();
        
        // 验证父节点（如果指定）
        DocumentNode parentNode = null;
        if (request.getParentId() != null) {
            parentNode = getDocumentNodeEntity(request.getParentId());
            if (!parentNode.getKnowledgeBaseId().equals(kbId)) {
                throw new IllegalArgumentException("Parent node must be in the same knowledge base");
            }
        }
        
        // 验证名称唯一性
        if (!isNodeNameAvailable(kbId, request.getParentId(), request.getName(), null)) {
            throw new IllegalArgumentException("Node name already exists in the same parent");
        }
        
        // 创建文档节点
        DocumentNode documentNode = documentNodeMapper.fromCreateRequest(request);
        documentNode.setKnowledgeBaseId(kbId);
        documentNode.setParentId(request.getParentId());
        documentNode.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        documentNode.setEmbeddingStatus(DocumentNode.EmbeddingStatus.NOT_EMBEDDED);
        documentNode.setCreatedBy(currentUserId);
        documentNode.setStatus(DocumentNode.Status.ACTIVE);
        
        // 设置层级路径
        if (parentNode != null) {
            documentNode.setPath(parentNode.getPath() + "/" + request.getName());
            documentNode.setLevel(parentNode.getLevel() + 1);
        } else {
            documentNode.setPath("/" + request.getName());
            documentNode.setLevel(1);
        }
        
        documentNode = documentNodeRepository.save(documentNode);
        
        // 记录审计日志
        auditService.recordDocumentNodeOperation(
                currentUserId,
                documentNode.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.CREATE,
                "Created document node: " + documentNode.getName(),
                null,
                documentNode
        );
        
        log.info("Document node created successfully: {}", documentNode.getId());
        return documentNode;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#nodeId, authentication.name, 'UPDATE')")
    @CacheEvict(value = "documentNode", key = "#nodeId")
    public DocumentNode updateDocumentNode(String nodeId, UpdateDocumentNodeRequest request) {
        log.info("Updating document node: {}", nodeId);
        
        String currentUserId = getCurrentUserId();
        DocumentNode documentNode = getDocumentNodeEntity(nodeId);
        DocumentNode originalData = documentNode.clone();
        
        // 更新字段
        if (request.getName() != null && !request.getName().equals(documentNode.getName())) {
            // 验证新名称是否可用
            if (!isNodeNameAvailable(documentNode.getKnowledgeBaseId(), documentNode.getParentId(), 
                    request.getName(), nodeId)) {
                throw new IllegalArgumentException("Node name already exists");
            }
            updateNodePath(documentNode);
        }
        
        // 使用MapStruct进行部分更新
        documentNodeMapper.updateFromRequest(request, documentNode);
        
        documentNode.setUpdatedBy(currentUserId);
        documentNode.setUpdatedAt(LocalDateTime.now());
        
        documentNode = documentNodeRepository.save(documentNode);
        
        // 记录审计日志
        auditService.recordDocumentNodeOperation(
                currentUserId,
                documentNode.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.UPDATE,
                "Updated document node: " + documentNode.getName(),
                originalData,
                documentNode
        );
        
        log.info("Document node updated successfully: {}", nodeId);
        return documentNode;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#nodeId, authentication.name, 'DELETE')")
    @CacheEvict(value = "documentNode", key = "#nodeId")
    public void deleteDocumentNode(String nodeId) {
        log.info("Deleting document node: {}", nodeId);
        
        String currentUserId = getCurrentUserId();
        DocumentNode documentNode = getDocumentNodeEntity(nodeId);
        
        // 检查是否有子节点
        long childCount = documentNodeRepository.countByParentIdAndDeletedFalse(nodeId);
        if (childCount > 0) {
            throw new IllegalStateException("Cannot delete node with children. Delete children first.");
        }
        
        // 软删除
        documentNode.setDeleted(true);
        documentNode.setDeletedBy(currentUserId);
        documentNode.setDeletedAt(LocalDateTime.now());
        documentNode.setStatus(DocumentNode.Status.ARCHIVED);
        
        documentNodeRepository.save(documentNode);
        
        // 记录审计日志
        auditService.recordDocumentNodeOperation(
                currentUserId,
                documentNode.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.DELETE,
                "Deleted document node: " + documentNode.getName(),
                documentNode,
                null
        );
        
        log.info("Document node deleted successfully: {}", nodeId);
    }

    @Override
    @Cacheable(value = "documentNode", key = "#nodeId")
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#nodeId, authentication.name, 'READ')")
    public DocumentNodeDetailDTO getDocumentNodeById(String nodeId) {
        log.debug("Getting document node by id: {}", nodeId);
        
        DocumentNode documentNode = getDocumentNodeEntity(nodeId);
        return convertToDetailDTO(documentNode);
    }

    @Override
    @Cacheable(value = "documentTree", key = "#kbId")
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#kbId, authentication.name, 'READ')")
    public List<DocumentNodeTreeDTO> getDocumentTree(String kbId) {
        log.debug("Getting document tree for knowledge base: {}", kbId);
        
        String currentUserId = getCurrentUserId();
        List<DocumentNode> allNodes = documentNodeRepository.findByKnowledgeBaseIdAndDeletedFalseOrderByPathAsc(kbId);
        
        // 过滤用户有权限访问的节点
        List<DocumentNode> accessibleNodes = allNodes.stream()
                .filter(node -> permissionService.checkDocumentNodeAccess(node.getId(), currentUserId, "READ"))
                .collect(Collectors.toList());
        
        return buildTree(accessibleNodes);
    }

    @Override
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#parentId, authentication.name, 'READ')")
    public List<DocumentNodeListDTO> getChildNodes(String parentId) {
        log.debug("Getting child nodes for parent: {}", parentId);
        
        String currentUserId = getCurrentUserId();
        List<DocumentNode> childNodes = documentNodeRepository.findByParentIdAndDeletedFalseOrderBySortOrderAscNameAsc(parentId);
        
        return childNodes.stream()
                .filter(node -> permissionService.checkDocumentNodeAccess(node.getId(), currentUserId, "READ"))
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DocumentNodeListDTO> getAccessibleNodes(String kbId, Pageable pageable) {
        log.debug("Getting accessible nodes for knowledge base: {}, page: {}", kbId, pageable.getPageNumber());
        
        String currentUserId = getCurrentUserId();
        Page<DocumentNode> nodes = documentNodeRepository.findAccessibleNodesByUser(kbId, currentUserId, pageable);
        
        return nodes.map(this::convertToListDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#nodeId, authentication.name, 'UPDATE')")
    public DocumentNode moveDocumentNode(String nodeId, MoveDocumentNodeRequest request) {
        log.info("Moving document node: {} to parent: {}", nodeId, request.getNewParentId());
        
        String currentUserId = getCurrentUserId();
        DocumentNode documentNode = getDocumentNodeEntity(nodeId);
        
        // 验证目标父节点
        DocumentNode newParent = null;
        if (request.getNewParentId() != null) {
            newParent = getDocumentNodeEntity(request.getNewParentId());
            if (!newParent.getKnowledgeBaseId().equals(documentNode.getKnowledgeBaseId())) {
                throw new IllegalArgumentException("Cannot move node to different knowledge base");
            }
            
            // 防止循环引用
            if (isDescendantOf(request.getNewParentId(), nodeId)) {
                throw new IllegalArgumentException("Cannot move node to its own descendant");
            }
        }
        
        // 验证名称在新位置是否唯一
        if (!isNodeNameAvailable(documentNode.getKnowledgeBaseId(), request.getNewParentId(), 
                documentNode.getName(), nodeId)) {
            throw new IllegalArgumentException("Node name already exists in target location");
        }
        
        String oldParentId = documentNode.getParentId();
        documentNode.setParentId(request.getNewParentId());
        documentNode.setSortOrder(request.getNewSortOrder() != null ? request.getNewSortOrder() : 0);
        documentNode.setUpdatedBy(currentUserId);
        documentNode.setUpdatedAt(LocalDateTime.now());
        
        // 更新路径和层级
        updateNodePath(documentNode);
        
        documentNode = documentNodeRepository.save(documentNode);
        
        // 递归更新所有子节点的路径
        updateChildrenPaths(documentNode);
        
        // 记录审计日志
        auditService.recordDocumentNodeOperation(
                currentUserId,
                documentNode.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.MOVE,
                String.format("Moved document node from parent %s to %s", oldParentId, request.getNewParentId()),
                Map.of("oldParentId", oldParentId, "newParentId", request.getNewParentId()),
                documentNode
        );
        
        log.info("Document node moved successfully: {}", nodeId);
        return documentNode;
    }

    @Override
    public boolean hasAccessPermission(String nodeId, String userId) {
        return permissionService.checkDocumentNodeAccess(nodeId, userId, "READ");
    }

    @Override
    public boolean hasManagePermission(String nodeId, String userId) {
        return permissionService.checkDocumentNodeAccess(nodeId, userId, "MANAGE");
    }

    @Override
    public boolean isNodeNameAvailable(String kbId, String parentId, String nodeName, String excludeId) {
        log.debug("Checking node name availability: {} in parent: {}", nodeName, parentId);
        
        if (excludeId != null) {
            return !documentNodeRepository.existsByKnowledgeBaseIdAndParentIdAndNameAndDeletedFalseAndIdNot(
                    kbId, parentId, nodeName, excludeId);
        } else {
            return !documentNodeRepository.existsByKnowledgeBaseIdAndParentIdAndNameAndDeletedFalse(
                    kbId, parentId, nodeName);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#kbId, authentication.name, 'CREATE_DOCUMENT')")
    public DocumentNode createDocumentFromUpload(String kbId, String parentId, CreateDocumentFromUploadRequest request) {
        log.info("Creating document from upload: fileId={} in knowledge base: {}", request.getFileId(), kbId);
        
        String currentUserId = getCurrentUserId();
        
        // 验证父节点（如果指定）
        DocumentNode parentNode = null;
        if (parentId != null) {
            parentNode = getDocumentNodeEntity(parentId);
            if (!parentNode.getKnowledgeBase().getKbId().equals(kbId)) {
                throw new IllegalArgumentException("Parent node must be in the same knowledge base");
            }
        }
        
        // 获取上传文件信息
        FileInfo fileInfo = fileInfoStorageService.getFileInfo(request.getFileId())
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + request.getFileId()));
        
        // 验证文件是否属于当前用户或有权限访问
        if (!fileInfo.getUploadedBy().equals(currentUserId)) {
            // 这里可以添加更复杂的权限检查逻辑
            log.warn("User {} attempting to access file {} uploaded by {}", 
                    currentUserId, request.getFileId(), fileInfo.getUploadedBy());
        }
        
        // 使用文件名作为节点名称（如果未指定）
        String nodeName = request.getNodeName() != null ? request.getNodeName() : fileInfo.getOriginalFilename();
        
        // 验证名称唯一性
        if (!isNodeNameAvailable(kbId, parentId, nodeName, null)) {
            throw new IllegalArgumentException("Node name already exists in the same parent: " + nodeName);
        }
        
        // 创建文档节点
        DocumentNode documentNode = DocumentNode.builder()
                .knowledgeBase(getKnowledgeBaseEntity(kbId))
                .parent(parentNode)
                .nodeName(nodeName)
                .description(request.getDescription())
                .nodeType(DocumentNode.NodeType.DOCUMENT)
                .fileId(request.getFileId())
                .fileName(fileInfo.getOriginalFilename())
                .fileExtension(getFileExtension(fileInfo.getOriginalFilename()))
                .fileSize(fileInfo.getFileSize())
                .contentType(fileInfo.getContentType())
                .tags(request.getTags())
                .attributes(request.getAttributes())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .status(DocumentNode.NodeStatus.ACTIVE)
                .accessLevel(request.getAccessLevel() != null ? request.getAccessLevel() : DocumentNode.AccessLevel.INTERNAL)
                .embedded(false)
                .embeddingStatus(DocumentNode.EmbeddingStatus.PENDING)
                .version(1)
                .creatorId(currentUserId)
                .build();
        
        // 设置层级路径
        if (parentNode != null) {
            documentNode.setNodePath(parentNode.getNodePath() + "/" + nodeName);
            documentNode.setLevel(parentNode.getLevel() + 1);
        } else {
            documentNode.setNodePath("/" + nodeName);
            documentNode.setLevel(1);
        }
        
        documentNode = documentNodeRepository.save(documentNode);
        
        // 记录审计日志
        auditService.recordDocumentNodeOperation(
                currentUserId,
                documentNode.getNodeId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.CREATE,
                "Created document node from upload: " + documentNode.getNodeName(),
                Map.of(
                        "fileId", request.getFileId(),
                        "fileName", fileInfo.getOriginalFilename(),
                        "fileSize", fileInfo.getFileSize(),
                        "contentType", fileInfo.getContentType()
                ),
                documentNode
        );
        
        log.info("Document node created successfully from upload: {}", documentNode.getNodeId());
        return documentNode;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#nodeId, authentication.name, 'UPDATE')")
    @CacheEvict(value = "documentNode", key = "#nodeId")
    public DocumentNode updateDocumentFile(String nodeId, UpdateDocumentFileRequest request) {
        log.info("Updating document file: nodeId={}, newFileId={}", nodeId, request.getNewFileId());
        
        String currentUserId = getCurrentUserId();
        DocumentNode documentNode = getDocumentNodeEntity(nodeId);
        
        // 验证节点类型
        if (documentNode.getNodeType() != DocumentNode.NodeType.DOCUMENT) {
            throw new IllegalArgumentException("Only document nodes can have their files updated");
        }
        
        // 获取新文件信息
        FileInfo newFileInfo = fileInfoStorageService.getFileInfo(request.getNewFileId())
                .orElseThrow(() -> new IllegalArgumentException("New file not found: " + request.getNewFileId()));
        
        // 验证文件是否属于当前用户或有权限访问
        if (!newFileInfo.getUploadedBy().equals(currentUserId)) {
            log.warn("User {} attempting to access file {} uploaded by {}", 
                    currentUserId, request.getNewFileId(), newFileInfo.getUploadedBy());
        }
        
        // 保存原始文件信息用于审计
        String oldFileId = documentNode.getFileId();
        String oldFileName = documentNode.getFileName();
        Long oldFileSize = documentNode.getFileSize();
        
        // 更新文档节点的文件信息
        documentNode.setFileId(request.getNewFileId());
        documentNode.setFileName(newFileInfo.getOriginalFilename());
        documentNode.setFileExtension(getFileExtension(newFileInfo.getOriginalFilename()));
        documentNode.setFileSize(newFileInfo.getFileSize());
        documentNode.setContentType(newFileInfo.getContentType());
        documentNode.setLastModifierId(currentUserId);
        
        // 如果文件发生变化，重置嵌入状态
        if (!request.getNewFileId().equals(oldFileId)) {
            documentNode.setEmbedded(false);
            documentNode.setEmbeddingStatus(DocumentNode.EmbeddingStatus.PENDING);
            documentNode.setEmbeddingTime(null);
            documentNode.setVersion(documentNode.getVersion() + 1);
        }
        
        // 更新节点名称（如果请求中指定了）
        if (request.getNewNodeName() != null && !request.getNewNodeName().equals(documentNode.getNodeName())) {
            // 验证新名称的唯一性
            if (!isNodeNameAvailable(
                    documentNode.getKnowledgeBase().getKbId(), 
                    documentNode.getParent() != null ? documentNode.getParent().getNodeId() : null, 
                    request.getNewNodeName(), 
                    nodeId)) {
                throw new IllegalArgumentException("New node name already exists in the same parent: " + request.getNewNodeName());
            }
            
            String oldNodeName = documentNode.getNodeName();
            documentNode.setNodeName(request.getNewNodeName());
            
            // 更新路径
            updateNodePath(documentNode);
            
            // 递归更新所有子节点的路径
            updateChildrenPaths(documentNode);
        }
        
        documentNode = documentNodeRepository.save(documentNode);
        
        // 记录审计日志
        auditService.recordDocumentNodeOperation(
                currentUserId,
                documentNode.getNodeId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.UPDATE,
                "Updated document file: " + documentNode.getNodeName(),
                Map.of(
                        "oldFileId", oldFileId,
                        "newFileId", request.getNewFileId(),
                        "oldFileName", oldFileName,
                        "newFileName", newFileInfo.getOriginalFilename(),
                        "oldFileSize", oldFileSize,
                        "newFileSize", newFileInfo.getFileSize()
                ),
                documentNode
        );
        
        log.info("Document file updated successfully: {}", nodeId);
        return documentNode;
    }

    // 辅助方法
    private DocumentNode getDocumentNodeEntity(String nodeId) {
        return documentNodeRepository.findByNodeIdAndDeletedFalse(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Document node not found: " + nodeId));
    }
    
    private cn.mojoup.ai.kb.entity.KnowledgeBase getKnowledgeBaseEntity(String kbId) {
        return knowledgeBaseRepository.findByKbIdAndDeletedFalse(kbId)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge base not found: " + kbId));
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private void updateNodePath(DocumentNode node) {
        if (node.getParent() == null) {
            node.setNodePath("/" + node.getNodeName());
            node.setLevel(1);
        } else {
            DocumentNode parent = node.getParent();
            node.setNodePath(parent.getNodePath() + "/" + node.getNodeName());
            node.setLevel(parent.getLevel() + 1);
        }
    }
    
    private void updateChildrenPaths(DocumentNode parentNode) {
        List<DocumentNode> children = documentNodeRepository.findByParentAndDeletedFalse(parentNode);
        for (DocumentNode child : children) {
            child.setNodePath(parentNode.getNodePath() + "/" + child.getNodeName());
            child.setLevel(parentNode.getLevel() + 1);
            documentNodeRepository.save(child);
            
            // 递归更新子节点的子节点
            updateChildrenPaths(child);
        }
    }
    
    private boolean isDescendantOf(String ancestorId, String nodeId) {
        DocumentNode node = getDocumentNodeEntity(nodeId);
        String currentParentId = node.getParentId();
        
        while (currentParentId != null) {
            if (currentParentId.equals(ancestorId)) {
                return true;
            }
            DocumentNode parent = getDocumentNodeEntity(currentParentId);
            currentParentId = parent.getParentId();
        }
        
        return false;
    }
    
    private List<DocumentNodeTreeDTO> buildTree(List<DocumentNode> nodes) {
        // 构建树形结构的逻辑
        return nodes.stream()
                .filter(node -> node.getParentId() == null)
                .map(this::buildTreeNode)
                .collect(Collectors.toList());
    }
    
    private DocumentNodeTreeDTO buildTreeNode(DocumentNode node) {
        List<DocumentNode> children = documentNodeRepository.findByParentIdAndDeletedFalseOrderBySortOrderAscNameAsc(node.getId());
        
        DocumentNodeTreeDTO treeNode = documentNodeMapper.toTreeDTO(node);
        treeNode.setHasChildren(!children.isEmpty());
        treeNode.setChildren(children.stream().map(this::buildTreeNode).collect(Collectors.toList()));
        
        return treeNode;
    }
    
    private DocumentNodeDetailDTO convertToDetailDTO(DocumentNode node) {
        return documentNodeMapper.toDetailDTO(node);
    }
    
    private DocumentNodeListDTO convertToListDTO(DocumentNode node) {
        return DocumentNodeListDTO.builder()
                .id(node.getId())
                .name(node.getName())
                .nodeType(node.getNodeType())
                .fileName(node.getFileName())
                .fileSize(node.getFileSize())
                .mimeType(node.getMimeType())
                .embeddingStatus(node.getEmbeddingStatus())
                .status(node.getStatus())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .build();
    }

    // 省略其他接口方法的实现...
    @Override public DocumentNode copyDocumentNode(String nodeId, CopyDocumentNodeRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentNode renameDocumentNode(String nodeId, String newName) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<DocumentNodeListDTO> searchDocumentNodes(String kbId, DocumentNodeSearchRequest request, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<DocumentNodePermissionDTO> getNodePermissions(String nodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void grantNodePermission(String nodeId, GrantNodePermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void revokeNodePermission(String nodeId, String permissionId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void batchUpdateNodePermissions(String nodeId, BatchNodePermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentNodeStatsDTO getNodeStats(String nodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<DocumentNodeBreadcrumbDTO> getNodePath(String nodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public BatchDeleteNodeResultDTO batchDeleteNodes(List<String> nodeIds) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentPreviewDTO getDocumentPreview(String nodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Page<DocumentVersionDTO> getDocumentVersions(String nodeId, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentNode restoreDocumentVersion(String nodeId, String versionId) { throw new UnsupportedOperationException("Not implemented yet"); }
} 