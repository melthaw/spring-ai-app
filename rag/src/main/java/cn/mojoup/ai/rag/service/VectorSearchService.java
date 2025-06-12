package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 向量检索服务接口
 *
 * @author matt
 */
public interface VectorSearchService {

    /**
     * 执行向量检索
     */
    List<DocumentSegment> search(String query, String knowledgeBaseId, Integer limit, Double threshold);

    /**
     * 执行语义检索（带重排序功能）
     */
    List<DocumentSegment> semanticSearch(String query,
                                         String knowledgeBaseId,
                                         String embeddingModel,
                                         Integer limit,
                                         Double threshold,
                                         Boolean rerank);

    /**
     * 重排序文档
     */
    List<DocumentSegment> rerankDocuments(List<DocumentSegment> documents, String query);
} 