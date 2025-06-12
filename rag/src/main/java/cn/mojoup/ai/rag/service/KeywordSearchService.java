package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 关键词检索服务接口
 *
 * @author matt
 */
public interface KeywordSearchService {

    /**
     * 执行关键词检索
     */
    List<DocumentSegment> search(String query, List<String> keywords,
                                 String knowledgeBaseId, Integer limit);

    /**
     * 提取查询中的关键词
     */
    List<String> extractKeywords(String query);

    /**
     * 增强关键词匹配分数
     */
    void enhanceKeywordMatching(List<DocumentSegment> documents, List<String> keywords);
} 