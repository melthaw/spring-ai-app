package cn.mojoup.ai.kb.service;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.DocumentEmbedding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 文档嵌入服务接口
 * 负责调用RAG服务进行文档嵌入操作，并记录嵌入参数和结果
 *
 * @author matt
 */
public interface DocumentEmbeddingService {

    /**
     * 嵌入单个文档
     */
    DocumentEmbedding embedDocument(String nodeId, EmbedDocumentRequest request);

    /**
     * 批量嵌入文档
     */
    BatchEmbeddingResultDTO batchEmbedDocuments(String kbId, BatchEmbedDocumentRequest request);

    /**
     * 重新嵌入文档
     */
    DocumentEmbedding reembedDocument(String nodeId, ReembedDocumentRequest request);

    /**
     * 取消嵌入操作
     */
    void cancelEmbedding(String embeddingId);

    /**
     * 获取嵌入任务状态
     */
    EmbeddingTaskStatusDTO getEmbeddingStatus(String taskId);

    /**
     * 获取文档的嵌入历史
     */
    Page<DocumentEmbeddingDTO> getDocumentEmbeddingHistory(String nodeId, Pageable pageable);

    /**
     * 获取知识库的嵌入统计
     */
    EmbeddingStatsDTO getEmbeddingStats(String kbId);

    /**
     * 查询嵌入任务
     */
    Page<EmbeddingTaskDTO> getEmbeddingTasks(EmbeddingTaskQueryRequest request, Pageable pageable);

    /**
     * 重试失败的嵌入任务
     */
    DocumentEmbedding retryFailedEmbedding(String embeddingId);

    /**
     * 删除文档的向量数据
     */
    void deleteDocumentVectors(String nodeId);

    /**
     * 批量删除向量数据
     */
    BatchDeleteVectorResultDTO batchDeleteVectors(List<String> nodeIds);

    /**
     * 获取支持的嵌入模型列表
     */
    List<EmbeddingModelDTO> getSupportedEmbeddingModels();

    /**
     * 估算嵌入成本
     */
    EmbeddingCostEstimateDTO estimateEmbeddingCost(EmbeddingCostEstimateRequest request);

    /**
     * 预览文档分块结果
     */
    DocumentChunkPreviewDTO previewDocumentChunks(String nodeId, ChunkPreviewRequest request);

    /**
     * 获取嵌入进度
     */
    EmbeddingProgressDTO getEmbeddingProgress(String kbId);

    /**
     * 暂停知识库的嵌入任务
     */
    void pauseKnowledgeBaseEmbedding(String kbId);

    /**
     * 恢复知识库的嵌入任务
     */
    void resumeKnowledgeBaseEmbedding(String kbId);

    /**
     * 同步向量数据状态
     */
    VectorSyncResultDTO syncVectorStatus(String kbId);
} 