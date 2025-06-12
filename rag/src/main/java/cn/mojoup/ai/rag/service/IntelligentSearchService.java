package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 智能检索服务接口
 * 支持多知识库智能检索
 *
 * @author matt
 */
public interface IntelligentSearchService {

    /**
     * 执行智能检索
     */
    List<DocumentSegment> search(String query, List<String> knowledgeBaseIds,
                                 String strategy, Integer limit, Double threshold);

    /**
     * 检测查询意图
     */
    String detectIntent(String query);

    /**
     * 选择最佳检索策略
     */
    String selectStrategy(String query, String intent);

    /**
     * 合并多知识库结果
     */
    List<DocumentSegment> mergeMultiKbResults(List<List<DocumentSegment>> kbResults, Integer limit);
} 