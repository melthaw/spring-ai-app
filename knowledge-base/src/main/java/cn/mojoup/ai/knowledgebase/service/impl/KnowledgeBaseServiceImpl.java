package cn.mojoup.ai.knowledgebase.service.impl;

import cn.mojoup.ai.knowledgebase.domain.CreateKnowledgeBaseRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeBase;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeBaseSearchRequest;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeBaseRepository;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeDocumentRepository;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeCategoryRepository;
import cn.mojoup.ai.knowledgebase.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 知识库管理服务实现
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeCategoryRepository categoryRepository;

    @Override
    @Transactional
    public KnowledgeBase createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        log.info("创建知识库: kbCode={}, kbName={}", request.getKbCode(), request.getKbName());

        // 检查编码是否已存在
        if (knowledgeBaseRepository.existsByKbCode(request.getKbCode())) {
            throw new IllegalArgumentException("知识库编码已存在: " + request.getKbCode());
        }

        KnowledgeBase knowledgeBase = KnowledgeBase.builder()
                .kbCode(request.getKbCode())
                .kbName(request.getKbName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .coverUrl(request.getCoverUrl())
                .ownerId(request.getOwnerId())
                .ownerName(request.getOwnerName())
                .kbType(request.getKbType())
                .status(KnowledgeBase.KnowledgeBaseStatus.ACTIVE)
                .isPublic(request.getIsPublic())
                .accessLevel(request.getAccessLevel())
                .documentCount(0)
                .totalSize(0L)
                .lastUpdated(LocalDateTime.now())
                .createdBy(request.getOwnerId())
                .updatedBy(request.getOwnerId())
                .metadata(request.getMetadata())
                .build();

        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        log.info("知识库创建成功: id={}, kbCode={}", knowledgeBase.getId(), knowledgeBase.getKbCode());

        return knowledgeBase;
    }

    @Override
    @Transactional
    public KnowledgeBase updateKnowledgeBase(Long id, CreateKnowledgeBaseRequest request) {
        log.info("更新知识库: id={}, kbName={}", id, request.getKbName());

        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识库不存在: " + id));

        // 如果编码发生变化，检查新编码是否可用
        if (!knowledgeBase.getKbCode().equals(request.getKbCode())) {
            if (knowledgeBaseRepository.existsByKbCode(request.getKbCode())) {
                throw new IllegalArgumentException("知识库编码已存在: " + request.getKbCode());
            }
            knowledgeBase.setKbCode(request.getKbCode());
        }

        // 更新字段
        knowledgeBase.setKbName(request.getKbName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setIconUrl(request.getIconUrl());
        knowledgeBase.setCoverUrl(request.getCoverUrl());
        knowledgeBase.setOwnerName(request.getOwnerName());
        knowledgeBase.setKbType(request.getKbType());
        knowledgeBase.setIsPublic(request.getIsPublic());
        knowledgeBase.setAccessLevel(request.getAccessLevel());
        knowledgeBase.setLastUpdated(LocalDateTime.now());
        knowledgeBase.setUpdatedBy(request.getOwnerId());
        knowledgeBase.setMetadata(request.getMetadata());

        knowledgeBase = knowledgeBaseRepository.save(knowledgeBase);
        log.info("知识库更新成功: id={}", id);

        return knowledgeBase;
    }

    @Override
    public Optional<KnowledgeBase> getKnowledgeBaseById(Long id) {
        return knowledgeBaseRepository.findById(id);
    }

    @Override
    public Optional<KnowledgeBase> getKnowledgeBaseByCode(String kbCode) {
        return knowledgeBaseRepository.findByKbCode(kbCode);
    }

    @Override
    @Transactional
    public void deleteKnowledgeBase(Long id, String operatorId) {
        log.info("删除知识库: id={}, operatorId={}", id, operatorId);

        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识库不存在: " + id));

        knowledgeBase.setStatus(KnowledgeBase.KnowledgeBaseStatus.DELETED);
        knowledgeBase.setUpdatedBy(operatorId);
        knowledgeBase.setLastUpdated(LocalDateTime.now());

        knowledgeBaseRepository.save(knowledgeBase);
        log.info("知识库删除成功: id={}", id);
    }

    @Override
    @Transactional
    public void archiveKnowledgeBase(Long id, String operatorId) {
        log.info("归档知识库: id={}, operatorId={}", id, operatorId);

        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识库不存在: " + id));

        knowledgeBase.setStatus(KnowledgeBase.KnowledgeBaseStatus.ARCHIVED);
        knowledgeBase.setUpdatedBy(operatorId);
        knowledgeBase.setLastUpdated(LocalDateTime.now());

        knowledgeBaseRepository.save(knowledgeBase);
        log.info("知识库归档成功: id={}", id);
    }

    @Override
    @Transactional
    public void restoreKnowledgeBase(Long id, String operatorId) {
        log.info("恢复知识库: id={}, operatorId={}", id, operatorId);

        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("知识库不存在: " + id));

        knowledgeBase.setStatus(KnowledgeBase.KnowledgeBaseStatus.ACTIVE);
        knowledgeBase.setUpdatedBy(operatorId);
        knowledgeBase.setLastUpdated(LocalDateTime.now());

        knowledgeBaseRepository.save(knowledgeBase);
        log.info("知识库恢复成功: id={}", id);
    }

    @Override
    public List<KnowledgeBase> getUserKnowledgeBases(String userId) {
        return knowledgeBaseRepository.findByOwnerIdAndStatusOrderByCreateTimeDesc(
                userId, KnowledgeBase.KnowledgeBaseStatus.ACTIVE);
    }

    @Override
    public Page<KnowledgeBase> getKnowledgeBasesPage(Pageable pageable) {
        return knowledgeBaseRepository.findAll(pageable);
    }

    @Override
    public Page<KnowledgeBase> getKnowledgeBasesByType(KnowledgeBase.KnowledgeBaseType kbType, Pageable pageable) {
        return knowledgeBaseRepository.findByKbType(kbType, pageable);
    }

    @Override
    public Page<KnowledgeBase> searchKnowledgeBases(String keyword, Pageable pageable) {
        KnowledgeBaseSearchRequest searchRequest = KnowledgeBaseSearchRequest.builder()
                .keyword(keyword)
                .statuses(List.of(KnowledgeBase.KnowledgeBaseStatus.ACTIVE))
                .build();
        
        return knowledgeBaseRepository.searchKnowledgeBases(searchRequest, pageable);
    }

    @Override
    public Page<KnowledgeBase> searchUserKnowledgeBases(String userId, String keyword, Pageable pageable) {
        KnowledgeBaseSearchRequest searchRequest = KnowledgeBaseSearchRequest.builder()
                .keyword(keyword)
                .ownerId(userId)
                .statuses(List.of(KnowledgeBase.KnowledgeBaseStatus.ACTIVE))
                .build();

        return knowledgeBaseRepository.searchKnowledgeBases(searchRequest, pageable);
    }

    @Override
    public Page<KnowledgeBase> getPublicKnowledgeBases(Pageable pageable) {
        return knowledgeBaseRepository.findByIsPublicTrueAndStatusOrderByCreateTimeDesc(
                KnowledgeBase.KnowledgeBaseStatus.ACTIVE, pageable);
    }

    @Override
    public boolean isKbCodeAvailable(String kbCode) {
        return !knowledgeBaseRepository.existsByKbCode(kbCode);
    }

    @Override
    @Transactional
    public void updateKnowledgeBaseStats(Long kbId) {
        log.info("更新知识库统计信息: kbId={}", kbId);

        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new IllegalArgumentException("知识库不存在: " + kbId));

        // 统计文档数量
        Long documentCount = documentRepository.countByKbIdAndStatus(kbId, 
                List.of(cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument.DocumentStatus.COMPLETED));

        // 统计总大小
        Long totalSize = documentRepository.sumFileSizeByKbId(kbId);

        // 统计分类数量
        Long categoryCount = categoryRepository.countByKnowledgeBaseId(kbId);

        knowledgeBase.setDocumentCount(documentCount.intValue());
        knowledgeBase.setTotalSize(totalSize);
        knowledgeBase.setLastUpdated(LocalDateTime.now());

        knowledgeBaseRepository.save(knowledgeBase);
        log.info("知识库统计信息更新完成: kbId={}, documents={}, size={}", kbId, documentCount, totalSize);
    }

    @Override
    public KnowledgeBaseStats getKnowledgeBaseStats(Long kbId) {
        // 获取基本统计信息
        Long totalDocuments = documentRepository.countByKbIdAndStatus(kbId, 
                List.of(cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument.DocumentStatus.values()));
        Long totalSize = documentRepository.sumFileSizeByKbId(kbId);
        Long totalCategories = categoryRepository.countByKnowledgeBaseId(kbId);

        // 获取不同状态的文档数量
        List<Object[]> statusCounts = documentRepository.countByKbIdAndStatus(kbId);
        
        Long completedDocuments = 0L;
        Long processingDocuments = 0L;
        Long failedDocuments = 0L;

        for (Object[] statusCount : statusCounts) {
            cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument.DocumentStatus status = 
                    (cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument.DocumentStatus) statusCount[0];
            Long count = (Long) statusCount[1];

            switch (status) {
                case COMPLETED -> completedDocuments = count;
                case PROCESSING, EMBEDDING -> processingDocuments += count;
                case FAILED -> failedDocuments = count;
            }
        }

        return new KnowledgeBaseStats(totalDocuments, totalSize, totalCategories,
                completedDocuments, processingDocuments, failedDocuments);
    }

    @Override
    public Page<KnowledgeBase> searchKnowledgeBasesAdvanced(KnowledgeBaseSearchRequest searchRequest, Pageable pageable) {
        log.info("高级搜索知识库: keyword={}, ownerId={}", searchRequest.getKeyword(), searchRequest.getOwnerId());
        return knowledgeBaseRepository.searchKnowledgeBases(searchRequest, pageable);
    }
} 