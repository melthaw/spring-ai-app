package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentEmbeddingRequest;
import cn.mojoup.ai.rag.domain.DocumentEmbeddingResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文档嵌入整合服务接口
 * 整合文档读取、嵌入和向量存储功能
 *
 * @author matt
 */
public interface DocumentEmbeddingService {

    /**
     * 同步处理文档嵌入
     *
     * @param request 嵌入请求
     * @return 处理结果
     */
    DocumentEmbeddingResponse processDocument(DocumentEmbeddingRequest request);

    /**
     * 异步处理文档嵌入
     *
     * @param request 嵌入请求
     * @return 异步处理结果
     */
    CompletableFuture<DocumentEmbeddingResponse> processDocumentAsync(DocumentEmbeddingRequest request);

    /**
     * 批量处理文档嵌入
     *
     * @param requests 嵌入请求列表
     * @return 处理结果列表
     */
    List<DocumentEmbeddingResponse> processDocuments(List<DocumentEmbeddingRequest> requests);

    /**
     * 异步批量处理文档嵌入
     *
     * @param requests 嵌入请求列表
     * @return 异步处理结果
     */
    CompletableFuture<List<DocumentEmbeddingResponse>> processDocumentsAsync(List<DocumentEmbeddingRequest> requests);

    /**
     * 获取处理状态
     *
     * @param processingId 处理ID
     * @return 处理状态
     */
    DocumentEmbeddingResponse getProcessingStatus(String processingId);

    /**
     * 取消处理
     *
     * @param processingId 处理ID
     * @return 是否成功取消
     */
    boolean cancelProcessing(String processingId);

    /**
     * 重新处理失败的文档
     *
     * @param processingId 原处理ID
     * @return 新的处理结果
     */
    DocumentEmbeddingResponse retryProcessing(String processingId);

    /**
     * 删除文档的所有嵌入数据
     *
     * @param fileId 文件ID
     * @param knowledgeBaseId 知识库ID
     * @return 是否成功删除
     */
    boolean deleteDocumentEmbeddings(String fileId, String knowledgeBaseId);

    /**
     * 检查文档是否已经嵌入
     *
     * @param fileId 文件ID
     * @param knowledgeBaseId 知识库ID
     * @return 是否已嵌入
     */
    boolean isDocumentEmbedded(String fileId, String knowledgeBaseId);

    /**
     * 获取支持的文件类型
     *
     * @return 支持的文件扩展名列表
     */
    List<String> getSupportedFileTypes();

    /**
     * 获取支持的嵌入模型
     *
     * @return 支持的模型列表
     */
    List<String> getSupportedEmbeddingModels();
} 