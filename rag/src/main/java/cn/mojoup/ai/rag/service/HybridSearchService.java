package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 混合检索服务接口
 * 结合向量检索和关键词检索
 *
 * @author matt
 */
public interface HybridSearchService {

    /**
     * 执行混合检索
     */
    List<DocumentSegment> search(String query, String knowledgeBaseId,
                                 List<String> keywords, Double keywordWeight,
                                 Double semanticWeight, Boolean enableRerank,
                                 Integer limit, Double threshold);

    /**
     * 合并检索结果
     */
    List<DocumentSegment> mergeResults(List<DocumentSegment> keywordResults,
                                       List<DocumentSegment> semanticResults,
                                       Double keywordWeight, Double semanticWeight);

    /**
     * 计算混合分数
     */
    void calculateHybridScores(List<DocumentSegment> documents,
                               Double keywordWeight, Double semanticWeight);
} 