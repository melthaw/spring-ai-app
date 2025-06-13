package cn.mojoup.ai.kb.service.impl;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.KnowledgeBase;
import cn.mojoup.ai.kb.repository.KnowledgeBaseRepository;
import cn.mojoup.ai.kb.service.AuditService;
import cn.mojoup.ai.kb.service.KnowledgeBaseService;
import cn.mojoup.ai.kb.service.PermissionService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识库服务实现
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public KnowledgeBase createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        log.info("Creating knowledge base: {}", request.getName());
        
        String currentUserId = getCurrentUserId();
        
        // 验证名称是否可用
        if (knowledgeBaseRepository.existsByNameAndOrganizationIdAndDeletedFalse(
                request.getName(), request.getOrganizationId())) {
            throw new IllegalArgumentException("Knowledge base name already exists");
        }
        
        // 创建知识库
        KnowledgeBase knowledgeBase = KnowledgeBase.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organizationId(request.getOrganizationId())
                .departmentId(request.getDepartmentId())
                .accessLevel(request.getAccessLevel())
                .tags(request.getTags())
                .iconUrl(request.getIconUrl())
                .color(request.getColor())
                .defaultEmbeddingModel(request.getDefaultEmbeddingModel())
                .defaultChunkSize(request.getDefaultChunkSize())
                .defaultChunkOverlap(request.getDefaultChunkOverlap())
                .autoEmbedding(request.getAutoEmbedding())
                .embeddingSchedule(request.getEmbeddingSchedule())
                .maxDocuments(request.getMaxDocuments())
                .maxStorage(request.getMaxStorage())
                .allowedFileTypes(request.getAllowedFileTypes())
                .enableVersionControl(request.getEnableVersionControl())
                .enableFullTextSearch(request.getEnableFullTextSearch())
                .enableSemanticSearch(request.getEnableSemanticSearch())
                .retentionDays(request.getRetentionDays())
                .ownerUserId(currentUserId)
                .createdBy(currentUserId)
                .status(KnowledgeBase.Status.ACTIVE)
                .build();
        
        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        
        // 记录审计日志
        auditService.recordKnowledgeBaseOperation(
                currentUserId,
                knowledgeBase.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.CREATE,
                "Created knowledge base: " + knowledgeBase.getName(),
                null,
                knowledgeBase
        );
        
        log.info("Knowledge base created successfully: {}", knowledgeBase.getId());
        return knowledgeBase;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#id, authentication.name, 'UPDATE')")
    @CacheEvict(value = "knowledgeBase", key = "#id")
    public KnowledgeBase updateKnowledgeBase(String id, UpdateKnowledgeBaseRequest request) {
        log.info("Updating knowledge base: {}", id);
        
        String currentUserId = getCurrentUserId();
        KnowledgeBase knowledgeBase = getKnowledgeBaseEntity(id);
        KnowledgeBase originalData = knowledgeBase.clone();
        
        // 更新字段
        if (request.getName() != null) {
            knowledgeBase.setName(request.getName());
        }
        if (request.getDescription() != null) {
            knowledgeBase.setDescription(request.getDescription());
        }
        if (request.getAccessLevel() != null) {
            knowledgeBase.setAccessLevel(request.getAccessLevel());
        }
        if (request.getTags() != null) {
            knowledgeBase.setTags(request.getTags());
        }
        if (request.getIconUrl() != null) {
            knowledgeBase.setIconUrl(request.getIconUrl());
        }
        if (request.getColor() != null) {
            knowledgeBase.setColor(request.getColor());
        }
        if (request.getDefaultEmbeddingModel() != null) {
            knowledgeBase.setDefaultEmbeddingModel(request.getDefaultEmbeddingModel());
        }
        if (request.getDefaultChunkSize() != null) {
            knowledgeBase.setDefaultChunkSize(request.getDefaultChunkSize());
        }
        if (request.getDefaultChunkOverlap() != null) {
            knowledgeBase.setDefaultChunkOverlap(request.getDefaultChunkOverlap());
        }
        if (request.getAutoEmbedding() != null) {
            knowledgeBase.setAutoEmbedding(request.getAutoEmbedding());
        }
        if (request.getEmbeddingSchedule() != null) {
            knowledgeBase.setEmbeddingSchedule(request.getEmbeddingSchedule());
        }
        if (request.getMaxDocuments() != null) {
            knowledgeBase.setMaxDocuments(request.getMaxDocuments());
        }
        if (request.getMaxStorage() != null) {
            knowledgeBase.setMaxStorage(request.getMaxStorage());
        }
        if (request.getAllowedFileTypes() != null) {
            knowledgeBase.setAllowedFileTypes(request.getAllowedFileTypes());
        }
        if (request.getEnableVersionControl() != null) {
            knowledgeBase.setEnableVersionControl(request.getEnableVersionControl());
        }
        if (request.getEnableFullTextSearch() != null) {
            knowledgeBase.setEnableFullTextSearch(request.getEnableFullTextSearch());
        }
        if (request.getEnableSemanticSearch() != null) {
            knowledgeBase.setEnableSemanticSearch(request.getEnableSemanticSearch());
        }
        if (request.getRetentionDays() != null) {
            knowledgeBase.setRetentionDays(request.getRetentionDays());
        }
        
        knowledgeBase.setUpdatedBy(currentUserId);
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        
        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        
        // 记录审计日志
        auditService.recordKnowledgeBaseOperation(
                currentUserId,
                knowledgeBase.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.UPDATE,
                "Updated knowledge base: " + knowledgeBase.getName(),
                originalData,
                knowledgeBase
        );
        
        log.info("Knowledge base updated successfully: {}", id);
        return knowledgeBase;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#id, authentication.name, 'DELETE')")
    @CacheEvict(value = "knowledgeBase", key = "#id")
    public void deleteKnowledgeBase(String id) {
        log.info("Deleting knowledge base: {}", id);
        
        String currentUserId = getCurrentUserId();
        KnowledgeBase knowledgeBase = getKnowledgeBaseEntity(id);
        
        // 软删除
        knowledgeBase.setDeleted(true);
        knowledgeBase.setDeletedBy(currentUserId);
        knowledgeBase.setDeletedAt(LocalDateTime.now());
        knowledgeBase.setStatus(KnowledgeBase.Status.ARCHIVED);
        
        knowledgeBaseRepository.save(knowledgeBase);
        
        // 记录审计日志
        auditService.recordKnowledgeBaseOperation(
                currentUserId,
                knowledgeBase.getId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.DELETE,
                "Deleted knowledge base: " + knowledgeBase.getName(),
                knowledgeBase,
                null
        );
        
        log.info("Knowledge base deleted successfully: {}", id);
    }

    @Override
    @Cacheable(value = "knowledgeBase", key = "#id")
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#id, authentication.name, 'READ')")
    public KnowledgeBaseDetailDTO getKnowledgeBaseById(String id) {
        log.debug("Getting knowledge base by id: {}", id);
        
        KnowledgeBase knowledgeBase = getKnowledgeBaseEntity(id);
        return convertToDetailDTO(knowledgeBase);
    }

    @Override
    public Page<KnowledgeBaseListDTO> getUserKnowledgeBases(Pageable pageable) {
        log.debug("Getting user knowledge bases, page: {}", pageable.getPageNumber());
        
        String currentUserId = getCurrentUserId();
        Page<KnowledgeBase> knowledgeBases = knowledgeBaseRepository.findAccessibleKnowledgeBases(
                currentUserId, pageable);
        
        return knowledgeBases.map(this::convertToListDTO);
    }

    @Override
    public Page<KnowledgeBaseListDTO> getOrganizationKnowledgeBases(String organizationId, Pageable pageable) {
        log.debug("Getting organization knowledge bases: {}, page: {}", organizationId, pageable.getPageNumber());
        
        String currentUserId = getCurrentUserId();
        Page<KnowledgeBase> knowledgeBases = knowledgeBaseRepository.findByOrganizationIdAndAccessibleByUser(
                organizationId, currentUserId, pageable);
        
        return knowledgeBases.map(this::convertToListDTO);
    }

    @Override
    public Page<KnowledgeBaseListDTO> searchKnowledgeBases(KnowledgeBaseSearchRequest request, Pageable pageable) {
        log.debug("Searching knowledge bases with query: {}", request.getQuery());
        
        String currentUserId = getCurrentUserId();
        Page<KnowledgeBase> knowledgeBases = knowledgeBaseRepository.searchKnowledgeBases(
                request.getQuery(),
                request.getOrganizationId(),
                request.getDepartmentId(),
                request.getAccessLevel(),
                request.getTags(),
                request.getOwnerUserId(),
                request.getStatus(),
                currentUserId,
                pageable
        );
        
        return knowledgeBases.map(this::convertToListDTO);
    }

    @Override
    @Cacheable(value = "knowledgeBaseStats", key = "#id")
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#id, authentication.name, 'READ')")
    public KnowledgeBaseStatsDTO getKnowledgeBaseStats(String id) {
        log.debug("Getting knowledge base stats: {}", id);
        
        KnowledgeBase knowledgeBase = getKnowledgeBaseEntity(id);
        
        // 获取统计信息
        Long documentCount = knowledgeBaseRepository.countDocumentsByKnowledgeBaseId(id);
        Long embeddedDocumentCount = knowledgeBaseRepository.countEmbeddedDocumentsByKnowledgeBaseId(id);
        Long totalSize = knowledgeBaseRepository.sumStorageSizeByKnowledgeBaseId(id);
        Long vectorCount = knowledgeBaseRepository.countVectorsByKnowledgeBaseId(id);
        
        return KnowledgeBaseStatsDTO.builder()
                .knowledgeBaseId(id)
                .documentCount(documentCount)
                .embeddedDocumentCount(embeddedDocumentCount)
                .totalStorageSize(totalSize)
                .vectorCount(vectorCount)
                .embeddingProgress(embeddedDocumentCount.doubleValue() / documentCount.doubleValue() * 100)
                .createdAt(knowledgeBase.getCreatedAt())
                .lastUpdatedAt(knowledgeBase.getUpdatedAt())
                .build();
    }

    @Override
    public boolean isKnowledgeBaseNameAvailable(String organizationId, String name, String excludeId) {
        log.debug("Checking knowledge base name availability: {}", name);
        
        if (excludeId != null) {
            return !knowledgeBaseRepository.existsByNameAndOrganizationIdAndDeletedFalseAndIdNot(
                    name, organizationId, excludeId);
        } else {
            return !knowledgeBaseRepository.existsByNameAndOrganizationIdAndDeletedFalse(
                    name, organizationId);
        }
    }

    // 其他方法的实现...
    // 由于篇幅限制，这里省略了其他方法的实现，实际项目中需要完整实现所有接口方法
    
    private KnowledgeBase getKnowledgeBaseEntity(String id) {
        return knowledgeBaseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Knowledge base not found: " + id));
    }
    
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private KnowledgeBaseDetailDTO convertToDetailDTO(KnowledgeBase knowledgeBase) {
        return KnowledgeBaseDetailDTO.builder()
                .id(knowledgeBase.getId())
                .name(knowledgeBase.getName())
                .description(knowledgeBase.getDescription())
                .organizationId(knowledgeBase.getOrganizationId())
                .departmentId(knowledgeBase.getDepartmentId())
                .accessLevel(knowledgeBase.getAccessLevel())
                .tags(knowledgeBase.getTags())
                .iconUrl(knowledgeBase.getIconUrl())
                .color(knowledgeBase.getColor())
                .defaultEmbeddingModel(knowledgeBase.getDefaultEmbeddingModel())
                .defaultChunkSize(knowledgeBase.getDefaultChunkSize())
                .defaultChunkOverlap(knowledgeBase.getDefaultChunkOverlap())
                .autoEmbedding(knowledgeBase.getAutoEmbedding())
                .embeddingSchedule(knowledgeBase.getEmbeddingSchedule())
                .maxDocuments(knowledgeBase.getMaxDocuments())
                .maxStorage(knowledgeBase.getMaxStorage())
                .allowedFileTypes(knowledgeBase.getAllowedFileTypes())
                .enableVersionControl(knowledgeBase.getEnableVersionControl())
                .enableFullTextSearch(knowledgeBase.getEnableFullTextSearch())
                .enableSemanticSearch(knowledgeBase.getEnableSemanticSearch())
                .retentionDays(knowledgeBase.getRetentionDays())
                .ownerUserId(knowledgeBase.getOwnerUserId())
                .status(knowledgeBase.getStatus())
                .createdAt(knowledgeBase.getCreatedAt())
                .createdBy(knowledgeBase.getCreatedBy())
                .updatedAt(knowledgeBase.getUpdatedAt())
                .updatedBy(knowledgeBase.getUpdatedBy())
                .build();
    }
    
    private KnowledgeBaseListDTO convertToListDTO(KnowledgeBase knowledgeBase) {
        return KnowledgeBaseListDTO.builder()
                .id(knowledgeBase.getId())
                .name(knowledgeBase.getName())
                .description(knowledgeBase.getDescription())
                .organizationId(knowledgeBase.getOrganizationId())
                .departmentId(knowledgeBase.getDepartmentId())
                .accessLevel(knowledgeBase.getAccessLevel())
                .tags(knowledgeBase.getTags())
                .iconUrl(knowledgeBase.getIconUrl())
                .color(knowledgeBase.getColor())
                .ownerUserId(knowledgeBase.getOwnerUserId())
                .status(knowledgeBase.getStatus())
                .documentCount(0L) // 需要另外查询
                .totalSize(0L) // 需要另外查询
                .createdAt(knowledgeBase.getCreatedAt())
                .updatedAt(knowledgeBase.getUpdatedAt())
                .build();
    }

    // 省略其他接口方法的实现...
    // 这里只是为了展示实现的结构，实际项目中需要实现所有接口方法
    @Override public KnowledgeBase cloneKnowledgeBase(String id, CloneKnowledgeBaseRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void archiveKnowledgeBase(String id) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBase restoreKnowledgeBase(String id) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void transferKnowledgeBaseOwnership(String id, TransferOwnershipRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<KnowledgeBasePermissionDTO> getKnowledgeBasePermissions(String id) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void grantKnowledgeBasePermission(String id, GrantKnowledgeBasePermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void revokeKnowledgeBasePermission(String id, String permissionId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void batchUpdateKnowledgeBasePermissions(String id, BatchKnowledgeBasePermissionRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public BatchDeleteKnowledgeBaseResultDTO batchDeleteKnowledgeBases(List<String> ids) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseMaintenanceReportDTO performKnowledgeBaseMaintenance(String id, MaintenanceOptionsRequest options) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<KnowledgeBaseBackupDTO> getKnowledgeBaseBackups(String id) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseBackupDTO createKnowledgeBaseBackup(String id, CreateBackupRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void restoreKnowledgeBaseFromBackup(String id, String backupId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseImportResultDTO importKnowledgeBase(ImportKnowledgeBaseRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseExportDTO exportKnowledgeBase(String id, ExportKnowledgeBaseRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void syncKnowledgeBaseWithExternalSource(String id, SyncExternalSourceRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<KnowledgeBaseTemplateDTO> getKnowledgeBaseTemplates() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBase createKnowledgeBaseFromTemplate(String templateId, CreateFromTemplateRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseTemplateDTO saveAsTemplate(String id, SaveAsTemplateRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseHealthReportDTO checkKnowledgeBaseHealth(String id) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public KnowledgeBaseOptimizationReportDTO optimizeKnowledgeBase(String id, OptimizationOptionsRequest options) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<KnowledgeBaseActivityDTO> getKnowledgeBaseActivities(String id, ActivityQueryRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public Map<String, Object> getKnowledgeBaseMetrics(String id, MetricsQueryRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
} 