package cn.mojoup.ai.knowledgebase.service.impl;

import cn.mojoup.ai.knowledgebase.domain.*;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeCategoryRepository;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeDocumentRepository;
import cn.mojoup.ai.knowledgebase.repository.DocumentEmbeddingRepository;
import cn.mojoup.ai.knowledgebase.service.KnowledgeDocumentService;
import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.domain.UploadRequest;
import cn.mojoup.ai.upload.domain.UploadResponse;
import cn.mojoup.ai.upload.service.FileUploadService;
import cn.mojoup.ai.rag.domain.DocumentEmbeddingRequest;
import cn.mojoup.ai.rag.service.DocumentEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 知识库文档管理服务实现
 * 整合upload模块和rag模块的能力
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final DocumentEmbeddingRepository embeddingRepository;
    private final FileUploadService fileUploadService;
    private final DocumentEmbeddingService documentEmbeddingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public KnowledgeDocument uploadDocument(MultipartFile file, DocumentUploadRequest request) {
        log.info("开始上传文档: title={}, categoryId={}", request.getTitle(), request.getCategoryId());

        try {
            // 1. 验证分类是否存在
            KnowledgeCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + request.getCategoryId()));

            // 2. 调用upload模块上传文件
            UploadRequest uploadRequest = UploadRequest.builder()
                    .uploadUserId(request.getUploadUserId())
                    .generateUniqueName(true)
                    .overwrite(false)
                    .build();

            UploadResponse uploadResponse = fileUploadService.uploadFile(file, uploadRequest);
            
            if (!uploadResponse.isSuccess()) {
                throw new RuntimeException("文件上传失败: " + uploadResponse.getMessage());
            }

            FileInfo fileInfo = uploadResponse.getFileInfo();

            // 3. 确定文档类型
            KnowledgeDocument.DocumentType docType = determineDocumentType(fileInfo.getFileExtension());

            // 4. 创建文档记录
            KnowledgeDocument document = KnowledgeDocument.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .category(category)
                    .kbId(category.getKnowledgeBaseId())
                    .fileId(fileInfo.getFileId())
                    .docType(docType)
                    .status(KnowledgeDocument.DocumentStatus.UPLOADING)
                    .fileSize(fileInfo.getFileSize())
                    .tags(request.getTags())
                    .accessLevel(request.getAccessLevel())
                    .sortOrder(request.getSortOrder())
                    .enabled(true)
                    .createdBy(request.getUploadUserId())
                    .updatedBy(request.getUploadUserId())
                    .metadata(request.getMetadata())
                    .build();

            document = documentRepository.save(document);
            log.info("文档记录已创建: documentId={}, fileId={}", document.getId(), fileInfo.getFileId());

            // 5. 如果需要立即处理嵌入
            if (request.getImmediateEmbedding()) {
                processDocumentEmbedding(document, request);
            }

            return document;

        } catch (Exception e) {
            log.error("文档上传失败: title={}", request.getTitle(), e);
            throw new RuntimeException("文档上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<KnowledgeDocument> uploadDocuments(List<MultipartFile> files, DocumentUploadRequest request) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // 为每个文件创建单独的请求，避免标题冲突
                DocumentUploadRequest fileRequest = new DocumentUploadRequest();
                fileRequest.setTitle(request.getTitle() + " - " + file.getOriginalFilename());
                fileRequest.setDescription(request.getDescription());
                fileRequest.setCategoryId(request.getCategoryId());
                fileRequest.setTags(request.getTags());
                fileRequest.setAccessLevel(request.getAccessLevel());
                fileRequest.setSortOrder(request.getSortOrder());
                fileRequest.setUploadUserId(request.getUploadUserId());
                fileRequest.setImmediateEmbedding(request.getImmediateEmbedding());
                fileRequest.setEmbeddingModel(request.getEmbeddingModel());
                fileRequest.setChunkStrategy(request.getChunkStrategy());
                fileRequest.setChunkSize(request.getChunkSize());
                fileRequest.setChunkOverlap(request.getChunkOverlap());
                fileRequest.setMetadata(request.getMetadata());

                KnowledgeDocument document = uploadDocument(file, fileRequest);
                documents.add(document);
                
            } catch (Exception e) {
                log.error("批量上传中的文件处理失败: filename={}", file.getOriginalFilename(), e);
                // 继续处理其他文件，不中断整个批量上传
            }
        }
        
        return documents;
    }

    @Override
    public Optional<KnowledgeDocument> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Override
    public Optional<KnowledgeDocument> getDocumentByFileId(String fileId) {
        return documentRepository.findByFileId(fileId);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, String operatorId) {
        log.info("删除文档: documentId={}, operatorId={}", documentId, operatorId);
        
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));

        // 逻辑删除文档
        document.setEnabled(false);
        document.setUpdatedBy(operatorId);
        documentRepository.save(document);

        // 删除文件（调用upload模块）
        try {
            fileUploadService.deleteFileById(document.getFileId());
        } catch (Exception e) {
            log.warn("删除文件失败: fileId={}", document.getFileId(), e);
        }

        // TODO: 删除向量数据（调用rag模块）
        // vectorStoreService.deleteDocument(document.getFileId());
    }

    @Override
    public Page<KnowledgeDocument> getDocumentsByCategory(Long categoryId, Pageable pageable) {
        return documentRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<KnowledgeDocument> getDocumentsByKnowledgeBase(Long kbId, Pageable pageable) {
        return documentRepository.findByKbId(kbId, pageable);
    }

    @Override
    public Page<KnowledgeDocument> searchDocuments(String keyword, Pageable pageable) {
        return documentRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<KnowledgeDocument> searchDocumentsByKnowledgeBase(Long kbId, String keyword, Pageable pageable) {
        return documentRepository.searchByKbIdAndKeyword(kbId, keyword, pageable);
    }

    @Override
    @Transactional
    public void reprocessDocument(Long documentId, DocumentUploadRequest embeddingConfig) {
        log.info("重新处理文档: documentId={}", documentId);
        
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));

        // 更新处理状态
        document.setStatus(KnowledgeDocument.DocumentStatus.PROCESSING);
        document.setProcessStartTime(LocalDateTime.now());
        document.setProcessEndTime(null);
        document.setErrorMessage(null);
        documentRepository.save(document);

        // 重新处理嵌入
        processDocumentEmbedding(document, embeddingConfig);
    }

    @Override
    public List<DocumentEmbedding> getDocumentEmbeddings(Long documentId) {
        return embeddingRepository.findByDocumentIdOrderByCreateTimeDesc(documentId);
    }

    @Override
    public DocumentStats getDocumentStats(Long kbId) {
        // TODO: 实现统计逻辑
        return new DocumentStats(0L, 0L, 0L, 0L, 0L, 0L);
    }

    /**
     * 处理文档嵌入
     */
    private void processDocumentEmbedding(KnowledgeDocument document, DocumentUploadRequest request) {
        try {
            log.info("开始处理文档嵌入: documentId={}, model={}", document.getId(), request.getEmbeddingModel());

            // 更新文档状态为处理中
            document.setStatus(KnowledgeDocument.DocumentStatus.EMBEDDING);
            document.setProcessStartTime(LocalDateTime.now());
            documentRepository.save(document);

            // 创建嵌入记录
            DocumentEmbedding embedding = DocumentEmbedding.builder()
                    .document(document)
                    .embeddingModel(request.getEmbeddingModel())
                    .vectorDimension(1536) // 默认OpenAI维度
                    .chunkStrategy(request.getChunkStrategy())
                    .chunkSize(request.getChunkSize())
                    .chunkOverlap(request.getChunkOverlap())
                    .status(DocumentEmbedding.EmbeddingStatus.PENDING)
                    .enabled(true)
                    .createdBy(document.getCreatedBy())
                    .build();

            embedding = embeddingRepository.save(embedding);

            // 调用rag模块进行文档处理
            DocumentEmbeddingRequest embeddingRequest = new DocumentEmbeddingRequest();
            embeddingRequest.setFileId(document.getFileId());
            embeddingRequest.setEmbeddingModel(request.getEmbeddingModel());
            embeddingRequest.setChunkSize(request.getChunkSize());
            embeddingRequest.setChunkOverlap(request.getChunkOverlap());
            
            // 异步处理嵌入
            documentEmbeddingService.processDocumentEmbedding(embeddingRequest);

            log.info("文档嵌入处理已提交: documentId={}, embeddingId={}", document.getId(), embedding.getId());

        } catch (Exception e) {
            log.error("文档嵌入处理失败: documentId={}", document.getId(), e);
            
            // 更新文档状态为失败
            document.setStatus(KnowledgeDocument.DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            document.setProcessEndTime(LocalDateTime.now());
            documentRepository.save(document);
        }
    }

    /**
     * 根据文件扩展名确定文档类型
     */
    private KnowledgeDocument.DocumentType determineDocumentType(String extension) {
        if (extension == null) return KnowledgeDocument.DocumentType.OTHER;
        
        return switch (extension.toLowerCase()) {
            case "pdf" -> KnowledgeDocument.DocumentType.PDF;
            case "doc", "docx" -> KnowledgeDocument.DocumentType.WORD;
            case "xls", "xlsx" -> KnowledgeDocument.DocumentType.EXCEL;
            case "ppt", "pptx" -> KnowledgeDocument.DocumentType.PPT;
            case "txt", "md" -> KnowledgeDocument.DocumentType.TEXT;
            case "jpg", "jpeg", "png", "gif", "bmp" -> KnowledgeDocument.DocumentType.IMAGE;
            case "mp4", "avi", "mov" -> KnowledgeDocument.DocumentType.VIDEO;
            case "mp3", "wav", "flac" -> KnowledgeDocument.DocumentType.AUDIO;
            default -> KnowledgeDocument.DocumentType.OTHER;
        };
    }

    // 其他方法的简化实现...
    @Override public KnowledgeDocument updateDocument(Long documentId, DocumentUploadRequest request) { return null; }
    @Override public void permanentDeleteDocument(Long documentId, String operatorId) {}
    @Override public Page<KnowledgeDocument> searchDocumentsByCategory(Long categoryId, String keyword, Pageable pageable) { return null; }
    @Override public void reprocessDocuments(List<Long> documentIds, DocumentUploadRequest embeddingConfig) {}
    @Override public KnowledgeDocument.DocumentStatus getDocumentStatus(Long documentId) { return null; }
    @Override public DocumentEmbedding createDocumentEmbedding(Long documentId, String embeddingModel, DocumentEmbedding.ChunkStrategy chunkStrategy, Integer chunkSize, Integer chunkOverlap) { return null; }
    @Override public void deleteDocumentEmbedding(Long embeddingId) {}
    @Override public List<KnowledgeDocument> getFailedDocuments() { return null; }
    @Override public List<KnowledgeDocument> getProcessingDocuments() { return null; }
    @Override public void retryFailedDocuments() {}
    @Override public DocumentStats getCategoryDocumentStats(Long categoryId) { return null; }
    @Override public void moveDocumentToCategory(Long documentId, Long targetCategoryId, String operatorId) {}
    @Override public void moveDocumentsToCategory(List<Long> documentIds, Long targetCategoryId, String operatorId) {}
    @Override public KnowledgeDocument copyDocumentToCategory(Long documentId, Long targetCategoryId, String operatorId) { return null; }
    @Override public String exportDocumentContent(Long documentId) { return null; }
    @Override public String getDocumentDownloadUrl(Long documentId) { return null; }
} 