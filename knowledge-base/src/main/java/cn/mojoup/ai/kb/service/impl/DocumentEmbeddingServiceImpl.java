package cn.mojoup.ai.kb.service.impl;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.DocumentEmbedding;
import cn.mojoup.ai.kb.entity.DocumentNode;
import cn.mojoup.ai.kb.repository.DocumentEmbeddingRepository;
import cn.mojoup.ai.kb.repository.DocumentNodeRepository;
import cn.mojoup.ai.kb.service.AuditService;
import cn.mojoup.ai.kb.service.DocumentEmbeddingService;
import cn.mojoup.ai.rag.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 文档嵌入服务实现
 *
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentEmbeddingServiceImpl implements DocumentEmbeddingService {

    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final DocumentNodeRepository documentNodeRepository;
    private final VectorSearchService vectorSearchService;
    private final AuditService auditService;

    @Value("${knowledge-base.embedding.default-model:text-embedding-ada-002}")
    private String defaultEmbeddingModel;

    @Value("${knowledge-base.embedding.default-chunk-size:1000}")
    private Integer defaultChunkSize;

    @Value("${knowledge-base.embedding.default-chunk-overlap:100}")
    private Integer defaultChunkOverlap;

    @Override
    @Transactional
    public DocumentEmbedding embedDocument(String nodeId, EmbedDocumentRequest request) {
        log.info("Starting document embedding for node: {}", nodeId);
        
        String currentUserId = getCurrentUserId();
        DocumentNode documentNode = getDocumentNode(nodeId);
        
        // 验证文档是否可以嵌入
        validateDocumentForEmbedding(documentNode);
        
        // 创建嵌入记录
        DocumentEmbedding embedding = DocumentEmbedding.builder()
                .nodeId(nodeId)
                .knowledgeBaseId(documentNode.getKnowledgeBaseId())
                .embeddingModel(request.getEmbeddingModel() != null ? request.getEmbeddingModel() : defaultEmbeddingModel)
                .chunkSize(request.getChunkSize() != null ? request.getChunkSize() : defaultChunkSize)
                .chunkOverlap(request.getChunkOverlap() != null ? request.getChunkOverlap() : defaultChunkOverlap)
                .parameters(request.getParameters())
                .status(DocumentEmbedding.Status.PENDING)
                .startedBy(currentUserId)
                .startedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        
        embedding = documentEmbeddingRepository.save(embedding);
        
        try {
            // 调用RAG服务进行文档嵌入
            EmbeddingResult result = performEmbedding(documentNode.getFilePath(), embedding);
            
            // 更新嵌入结果
            embedding.setStatus(DocumentEmbedding.Status.COMPLETED);
            embedding.setCompletedAt(LocalDateTime.now());
            embedding.setChunkCount(result.getChunkCount());
            embedding.setVectorCount(result.getVectorCount());
            embedding.setTokenCount(result.getTokenCount());
            embedding.setProcessingTime(result.getProcessingTime());
            embedding.setResultMetadata(result.getMetadata());
            
            // 更新文档节点状态
            documentNode.setEmbeddingStatus(DocumentNode.EmbeddingStatus.EMBEDDED);
            documentNode.setLastEmbeddedAt(LocalDateTime.now());
            documentNode.setVectorCount(result.getVectorCount());
            documentNodeRepository.save(documentNode);
            
        } catch (Exception e) {
            log.error("Document embedding failed for node: {}", nodeId, e);
            
            // 更新失败状态
            embedding.setStatus(DocumentEmbedding.Status.FAILED);
            embedding.setCompletedAt(LocalDateTime.now());
            embedding.setErrorMessage(e.getMessage());
            
            // 增加重试次数
            embedding.setRetryCount(embedding.getRetryCount() + 1);
        }
        
        embedding = documentEmbeddingRepository.save(embedding);
        
        // 记录审计日志
        auditService.recordEmbeddingOperation(
                currentUserId,
                nodeId,
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.EMBED_DOCUMENT,
                "Document embedding " + embedding.getStatus().name().toLowerCase(),
                Map.of(
                        "embeddingId", embedding.getId(),
                        "model", embedding.getEmbeddingModel(),
                        "chunkSize", embedding.getChunkSize(),
                        "status", embedding.getStatus()
                )
        );
        
        log.info("Document embedding completed for node: {}, status: {}", nodeId, embedding.getStatus());
        return embedding;
    }

    @Override
    @Transactional
    @Async
    public BatchEmbeddingResultDTO batchEmbedDocuments(String kbId, BatchEmbedDocumentRequest request) {
        log.info("Starting batch document embedding for knowledge base: {}", kbId);
        
        String currentUserId = getCurrentUserId();
        List<DocumentNode> nodes = documentNodeRepository.findByKnowledgeBaseIdAndIdIn(kbId, request.getNodeIds());
        
        BatchEmbeddingResultDTO result = BatchEmbeddingResultDTO.builder()
                .totalCount(nodes.size())
                .successCount(0)
                .failureCount(0)
                .results(new java.util.ArrayList<>())
                .build();
        
        for (DocumentNode node : nodes) {
            try {
                EmbedDocumentRequest embedRequest = EmbedDocumentRequest.builder()
                        .embeddingModel(request.getEmbeddingModel())
                        .chunkSize(request.getChunkSize())
                        .chunkOverlap(request.getChunkOverlap())
                        .parameters(request.getParameters())
                        .build();
                
                DocumentEmbedding embedding = embedDocument(node.getId(), embedRequest);
                
                if (embedding.getStatus() == DocumentEmbedding.Status.COMPLETED) {
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } else {
                    result.setFailureCount(result.getFailureCount() + 1);
                }
                
                result.getResults().add(DocumentEmbeddingResultDTO.builder()
                        .nodeId(node.getId())
                        .embeddingId(embedding.getId())
                        .status(embedding.getStatus())
                        .errorMessage(embedding.getErrorMessage())
                        .build());
                
            } catch (Exception e) {
                log.error("Failed to embed document node: {}", node.getId(), e);
                result.setFailureCount(result.getFailureCount() + 1);
                result.getResults().add(DocumentEmbeddingResultDTO.builder()
                        .nodeId(node.getId())
                        .status(DocumentEmbedding.Status.FAILED)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }
        
        log.info("Batch document embedding completed for knowledge base: {}, success: {}, failure: {}", 
                kbId, result.getSuccessCount(), result.getFailureCount());
        
        return result;
    }

    @Override
    @Transactional
    public DocumentEmbedding reembedDocument(String nodeId, ReembedDocumentRequest request) {
        log.info("Re-embedding document for node: {}", nodeId);
        
        String currentUserId = getCurrentUserId();
        DocumentNode documentNode = getDocumentNode(nodeId);
        
        // 删除现有向量数据
        try {
            vectorSearchService.deleteDocumentVectors(nodeId);
        } catch (Exception e) {
            log.warn("Failed to delete existing vectors for node: {}", nodeId, e);
        }
        
        // 创建新的嵌入请求
        EmbedDocumentRequest embedRequest = EmbedDocumentRequest.builder()
                .embeddingModel(request.getEmbeddingModel())
                .chunkSize(request.getChunkSize())
                .chunkOverlap(request.getChunkOverlap())
                .parameters(request.getParameters())
                .build();
        
        return embedDocument(nodeId, embedRequest);
    }

    @Override
    @Transactional
    public void cancelEmbedding(String embeddingId) {
        log.info("Cancelling embedding: {}", embeddingId);
        
        String currentUserId = getCurrentUserId();
        DocumentEmbedding embedding = documentEmbeddingRepository.findById(embeddingId)
                .orElseThrow(() -> new IllegalArgumentException("Embedding not found: " + embeddingId));
        
        if (embedding.getStatus() != DocumentEmbedding.Status.PENDING) {
            throw new IllegalStateException("Can only cancel pending embeddings");
        }
        
        embedding.setStatus(DocumentEmbedding.Status.CANCELLED);
        embedding.setCompletedAt(LocalDateTime.now());
        embedding.setCancelledBy(currentUserId);
        
        documentEmbeddingRepository.save(embedding);
        
        // 记录审计日志
        auditService.recordEmbeddingOperation(
                currentUserId,
                embedding.getNodeId(),
                cn.mojoup.ai.kb.entity.AuditLog.OperationType.CANCEL_EMBEDDING,
                "Cancelled document embedding",
                Map.of("embeddingId", embeddingId)
        );
        
        log.info("Embedding cancelled: {}", embeddingId);
    }

    @Override
    public EmbeddingTaskStatusDTO getEmbeddingStatus(String taskId) {
        log.debug("Getting embedding status: {}", taskId);
        
        DocumentEmbedding embedding = documentEmbeddingRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Embedding task not found: " + taskId));
        
        return EmbeddingTaskStatusDTO.builder()
                .taskId(embedding.getId())
                .nodeId(embedding.getNodeId())
                .status(embedding.getStatus())
                .progress(calculateProgress(embedding))
                .startedAt(embedding.getStartedAt())
                .completedAt(embedding.getCompletedAt())
                .estimatedRemainingTime(estimateRemainingTime(embedding))
                .errorMessage(embedding.getErrorMessage())
                .build();
    }

    @Override
    public Page<DocumentEmbeddingDTO> getDocumentEmbeddingHistory(String nodeId, Pageable pageable) {
        log.debug("Getting embedding history for node: {}", nodeId);
        
        Page<DocumentEmbedding> embeddings = documentEmbeddingRepository.findByNodeIdOrderByStartedAtDesc(nodeId, pageable);
        return embeddings.map(this::convertToDTO);
    }

    @Override
    public EmbeddingStatsDTO getEmbeddingStats(String kbId) {
        log.debug("Getting embedding stats for knowledge base: {}", kbId);
        
        List<DocumentEmbedding> embeddings = documentEmbeddingRepository.findByKnowledgeBaseId(kbId);
        
        long totalEmbeddings = embeddings.size();
        long completedEmbeddings = embeddings.stream()
                .mapToLong(e -> e.getStatus() == DocumentEmbedding.Status.COMPLETED ? 1 : 0)
                .sum();
        long failedEmbeddings = embeddings.stream()
                .mapToLong(e -> e.getStatus() == DocumentEmbedding.Status.FAILED ? 1 : 0)
                .sum();
        long totalVectors = embeddings.stream()
                .mapToLong(e -> e.getVectorCount() != null ? e.getVectorCount() : 0)
                .sum();
        long totalTokens = embeddings.stream()
                .mapToLong(e -> e.getTokenCount() != null ? e.getTokenCount() : 0)
                .sum();
        
        return EmbeddingStatsDTO.builder()
                .knowledgeBaseId(kbId)
                .totalEmbeddings(totalEmbeddings)
                .completedEmbeddings(completedEmbeddings)
                .failedEmbeddings(failedEmbeddings)
                .successRate(totalEmbeddings > 0 ? (double) completedEmbeddings / totalEmbeddings * 100 : 0)
                .totalVectors(totalVectors)
                .totalTokens(totalTokens)
                .build();
    }

    // 辅助方法
    private DocumentNode getDocumentNode(String nodeId) {
        return documentNodeRepository.findByIdAndDeletedFalse(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Document node not found: " + nodeId));
    }
    
    private void validateDocumentForEmbedding(DocumentNode documentNode) {
        if (documentNode.getNodeType() != DocumentNode.NodeType.DOCUMENT) {
            throw new IllegalArgumentException("Only document nodes can be embedded");
        }
        
        if (documentNode.getFilePath() == null || documentNode.getFilePath().isEmpty()) {
            throw new IllegalArgumentException("Document file path is required for embedding");
        }
    }
    
    private EmbeddingResult performEmbedding(String filePath, DocumentEmbedding embedding) {
        // 这里应该调用RAG服务的实际嵌入方法
        // 为了示例，这里返回模拟结果
        return EmbeddingResult.builder()
                .chunkCount(10)
                .vectorCount(10)
                .tokenCount(1000)
                .processingTime(5000L)
                .metadata(Map.of("model", embedding.getEmbeddingModel()))
                .build();
    }
    
    private double calculateProgress(DocumentEmbedding embedding) {
        switch (embedding.getStatus()) {
            case PENDING:
                return 0.0;
            case PROCESSING:
                return 50.0; // 可以根据实际进度计算
            case COMPLETED:
                return 100.0;
            case FAILED:
            case CANCELLED:
                return 0.0;
            default:
                return 0.0;
        }
    }
    
    private Long estimateRemainingTime(DocumentEmbedding embedding) {
        if (embedding.getStatus() == DocumentEmbedding.Status.PROCESSING) {
            // 根据历史数据估算剩余时间
            return 30000L; // 30秒（模拟值）
        }
        return null;
    }
    
    private DocumentEmbeddingDTO convertToDTO(DocumentEmbedding embedding) {
        return DocumentEmbeddingDTO.builder()
                .id(embedding.getId())
                .nodeId(embedding.getNodeId())
                .knowledgeBaseId(embedding.getKnowledgeBaseId())
                .embeddingModel(embedding.getEmbeddingModel())
                .chunkSize(embedding.getChunkSize())
                .chunkOverlap(embedding.getChunkOverlap())
                .status(embedding.getStatus())
                .chunkCount(embedding.getChunkCount())
                .vectorCount(embedding.getVectorCount())
                .tokenCount(embedding.getTokenCount())
                .processingTime(embedding.getProcessingTime())
                .startedBy(embedding.getStartedBy())
                .startedAt(embedding.getStartedAt())
                .completedAt(embedding.getCompletedAt())
                .errorMessage(embedding.getErrorMessage())
                .retryCount(embedding.getRetryCount())
                .build();
    }
    
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    // 内部结果类
    private static class EmbeddingResult {
        public static EmbeddingResultBuilder builder() {
            return new EmbeddingResultBuilder();
        }
        
        public static class EmbeddingResultBuilder {
            private Integer chunkCount;
            private Long vectorCount;
            private Long tokenCount;
            private Long processingTime;
            private Map<String, Object> metadata;
            
            public EmbeddingResultBuilder chunkCount(Integer chunkCount) {
                this.chunkCount = chunkCount;
                return this;
            }
            
            public EmbeddingResultBuilder vectorCount(Long vectorCount) {
                this.vectorCount = vectorCount;
                return this;
            }
            
            public EmbeddingResultBuilder tokenCount(Long tokenCount) {
                this.tokenCount = tokenCount;
                return this;
            }
            
            public EmbeddingResultBuilder processingTime(Long processingTime) {
                this.processingTime = processingTime;
                return this;
            }
            
            public EmbeddingResultBuilder metadata(Map<String, Object> metadata) {
                this.metadata = metadata;
                return this;
            }
            
            public EmbeddingResult build() {
                EmbeddingResult result = new EmbeddingResult();
                result.chunkCount = this.chunkCount;
                result.vectorCount = this.vectorCount;
                result.tokenCount = this.tokenCount;
                result.processingTime = this.processingTime;
                result.metadata = this.metadata;
                return result;
            }
        }
        
        private Integer chunkCount;
        private Long vectorCount;
        private Long tokenCount;
        private Long processingTime;
        private Map<String, Object> metadata;
        
        public Integer getChunkCount() { return chunkCount; }
        public Long getVectorCount() { return vectorCount; }
        public Long getTokenCount() { return tokenCount; }
        public Long getProcessingTime() { return processingTime; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    // 省略其他接口方法的实现...
    @Override public Page<EmbeddingTaskDTO> getEmbeddingTasks(EmbeddingTaskQueryRequest request, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentEmbedding retryFailedEmbedding(String embeddingId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void deleteDocumentVectors(String nodeId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public BatchDeleteVectorResultDTO batchDeleteVectors(List<String> nodeIds) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public List<EmbeddingModelDTO> getSupportedEmbeddingModels() { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public EmbeddingCostEstimateDTO estimateEmbeddingCost(EmbeddingCostEstimateRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public DocumentChunkPreviewDTO previewDocumentChunks(String nodeId, ChunkPreviewRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public EmbeddingProgressDTO getEmbeddingProgress(String kbId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void pauseKnowledgeBaseEmbedding(String kbId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public void resumeKnowledgeBaseEmbedding(String kbId) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public VectorSyncResultDTO syncVectorStatus(String kbId) { throw new UnsupportedOperationException("Not implemented yet"); }
} 