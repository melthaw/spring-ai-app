package cn.mojoup.ai.knowledgebase.service;

import cn.mojoup.ai.knowledgebase.domain.DocumentSearchRequest;
import cn.mojoup.ai.knowledgebase.domain.DocumentUploadRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument;
import cn.mojoup.ai.knowledgebase.domain.DocumentEmbedding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 知识库文档管理服务接口
 * 整合upload模块和rag模块的能力
 * 
 * @author matt
 */
public interface KnowledgeDocumentService {

    /**
     * 上传文档到知识库
     * 1. 调用upload模块上传文件
     * 2. 创建文档记录
     * 3. 异步调用rag模块进行文档处理和嵌入
     */
    KnowledgeDocument uploadDocument(MultipartFile file, DocumentUploadRequest request);

    /**
     * 批量上传文档
     */
    List<KnowledgeDocument> uploadDocuments(List<MultipartFile> files, DocumentUploadRequest request);

    /**
     * 更新文档信息
     */
    KnowledgeDocument updateDocument(Long documentId, DocumentUploadRequest request);

    /**
     * 根据ID获取文档详情
     */
    Optional<KnowledgeDocument> getDocumentById(Long id);

    /**
     * 根据文件ID获取文档
     */
    Optional<KnowledgeDocument> getDocumentByFileId(String fileId);

    /**
     * 删除文档（逻辑删除，同时删除文件和向量数据）
     */
    void deleteDocument(Long documentId, String operatorId);

    /**
     * 物理删除文档（彻底删除文件和所有相关数据）
     */
    void permanentDeleteDocument(Long documentId, String operatorId);

    /**
     * 获取分类下的文档列表
     */
    Page<KnowledgeDocument> getDocumentsByCategory(Long categoryId, Pageable pageable);

    /**
     * 获取知识库下的文档列表
     */
    Page<KnowledgeDocument> getDocumentsByKnowledgeBase(Long kbId, Pageable pageable);

    /**
     * 搜索文档
     */
    Page<KnowledgeDocument> searchDocuments(String keyword, Pageable pageable);

    /**
     * 在指定知识库中搜索文档
     */
    Page<KnowledgeDocument> searchDocumentsByKnowledgeBase(Long kbId, String keyword, Pageable pageable);

    /**
     * 在指定分类中搜索文档
     */
    Page<KnowledgeDocument> searchDocumentsByCategory(Long categoryId, String keyword, Pageable pageable);

    /**
     * 重新处理文档（重新解析和嵌入）
     */
    void reprocessDocument(Long documentId, DocumentUploadRequest embeddingConfig);

    /**
     * 批量重新处理文档
     */
    void reprocessDocuments(List<Long> documentIds, DocumentUploadRequest embeddingConfig);

    /**
     * 获取文档的处理状态
     */
    KnowledgeDocument.DocumentStatus getDocumentStatus(Long documentId);

    /**
     * 获取文档的嵌入结果列表
     */
    List<DocumentEmbedding> getDocumentEmbeddings(Long documentId);

    /**
     * 为文档创建新的嵌入
     */
    DocumentEmbedding createDocumentEmbedding(Long documentId, String embeddingModel, 
                                             DocumentEmbedding.ChunkStrategy chunkStrategy,
                                             Integer chunkSize, Integer chunkOverlap);

    /**
     * 删除文档的指定嵌入
     */
    void deleteDocumentEmbedding(Long embeddingId);

    /**
     * 获取处理失败的文档列表
     */
    List<KnowledgeDocument> getFailedDocuments();

    /**
     * 获取处理中的文档列表
     */
    List<KnowledgeDocument> getProcessingDocuments();

    /**
     * 重试处理失败的文档
     */
    void retryFailedDocuments();

    /**
     * 获取文档统计信息
     */
    DocumentStats getDocumentStats(Long kbId);

    /**
     * 获取分类的文档统计信息
     */
    DocumentStats getCategoryDocumentStats(Long categoryId);

    /**
     * 移动文档到另一个分类
     */
    void moveDocumentToCategory(Long documentId, Long targetCategoryId, String operatorId);

    /**
     * 批量移动文档
     */
    void moveDocumentsToCategory(List<Long> documentIds, Long targetCategoryId, String operatorId);

    /**
     * 复制文档到另一个分类
     */
    KnowledgeDocument copyDocumentToCategory(Long documentId, Long targetCategoryId, String operatorId);

    /**
     * 导出文档内容
     */
    String exportDocumentContent(Long documentId);

    /**
     * 获取文档的下载链接
     */
    String getDocumentDownloadUrl(Long documentId);

    /**
     * 使用搜索请求查询文档（高级搜索）
     */
    Page<KnowledgeDocument> searchDocumentsAdvanced(DocumentSearchRequest searchRequest, Pageable pageable);

    /**
     * 文档统计信息
     */
    class DocumentStats {
        private Long totalDocuments;
        private Long completedDocuments;
        private Long processingDocuments;
        private Long failedDocuments;
        private Long totalSize;
        private Long totalCharacters;

        public DocumentStats(Long totalDocuments, Long completedDocuments, Long processingDocuments,
                           Long failedDocuments, Long totalSize, Long totalCharacters) {
            this.totalDocuments = totalDocuments;
            this.completedDocuments = completedDocuments;
            this.processingDocuments = processingDocuments;
            this.failedDocuments = failedDocuments;
            this.totalSize = totalSize;
            this.totalCharacters = totalCharacters;
        }

        public Long getTotalDocuments() { return totalDocuments; }
        public Long getCompletedDocuments() { return completedDocuments; }
        public Long getProcessingDocuments() { return processingDocuments; }
        public Long getFailedDocuments() { return failedDocuments; }
        public Long getTotalSize() { return totalSize; }
        public Long getTotalCharacters() { return totalCharacters; }
    }
} 