package cn.mojoup.ai.knowledgebase.service;

import cn.mojoup.ai.knowledgebase.domain.CreateKnowledgeBaseRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

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
     * 更新知识库信息
     */
    KnowledgeBase updateKnowledgeBase(Long id, CreateKnowledgeBaseRequest request);

    /**
     * 根据ID获取知识库详情
     */
    Optional<KnowledgeBase> getKnowledgeBaseById(Long id);

    /**
     * 根据编码获取知识库详情
     */
    Optional<KnowledgeBase> getKnowledgeBaseByCode(String kbCode);

    /**
     * 删除知识库（逻辑删除）
     */
    void deleteKnowledgeBase(Long id, String operatorId);

    /**
     * 归档知识库
     */
    void archiveKnowledgeBase(Long id, String operatorId);

    /**
     * 恢复知识库
     */
    void restoreKnowledgeBase(Long id, String operatorId);

    /**
     * 获取用户的知识库列表
     */
    List<KnowledgeBase> getUserKnowledgeBases(String userId);

    /**
     * 分页获取知识库列表
     */
    Page<KnowledgeBase> getKnowledgeBasesPage(Pageable pageable);

    /**
     * 根据类型分页获取知识库
     */
    Page<KnowledgeBase> getKnowledgeBasesByType(KnowledgeBase.KnowledgeBaseType kbType, Pageable pageable);

    /**
     * 搜索知识库
     */
    Page<KnowledgeBase> searchKnowledgeBases(String keyword, Pageable pageable);

    /**
     * 用户搜索知识库
     */
    Page<KnowledgeBase> searchUserKnowledgeBases(String userId, String keyword, Pageable pageable);

    /**
     * 获取公开的知识库
     */
    Page<KnowledgeBase> getPublicKnowledgeBases(Pageable pageable);

    /**
     * 检查知识库编码是否可用
     */
    boolean isKbCodeAvailable(String kbCode);

    /**
     * 更新知识库统计信息
     */
    void updateKnowledgeBaseStats(Long kbId);

    /**
     * 获取知识库统计信息
     */
    KnowledgeBaseStats getKnowledgeBaseStats(Long kbId);

    /**
     * 知识库统计信息
     */
    class KnowledgeBaseStats {
        private Long totalDocuments;
        private Long totalSize;
        private Long totalCategories;
        private Long completedDocuments;
        private Long processingDocuments;
        private Long failedDocuments;

        public KnowledgeBaseStats(Long totalDocuments, Long totalSize, Long totalCategories,
                                 Long completedDocuments, Long processingDocuments, Long failedDocuments) {
            this.totalDocuments = totalDocuments;
            this.totalSize = totalSize;
            this.totalCategories = totalCategories;
            this.completedDocuments = completedDocuments;
            this.processingDocuments = processingDocuments;
            this.failedDocuments = failedDocuments;
        }

        public Long getTotalDocuments() { return totalDocuments; }
        public Long getTotalSize() { return totalSize; }
        public Long getTotalCategories() { return totalCategories; }
        public Long getCompletedDocuments() { return completedDocuments; }
        public Long getProcessingDocuments() { return processingDocuments; }
        public Long getFailedDocuments() { return failedDocuments; }
    }
}