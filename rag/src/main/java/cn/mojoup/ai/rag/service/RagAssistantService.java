package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.*;

import java.util.List;

/**
 * RAG辅助服务接口
 * 提供意图识别、策略选择、引用生成等辅助功能
 *
 * @author matt
 */
public interface RagAssistantService {

    /**
     * 意图识别
     */
    String detectQueryIntent(String question);

    /**
     * 策略选择
     */
    String selectOptimalStrategy(String question, String intent);

    /**
     * 生成引用
     */
    List<Citation> generateCitations(List<DocumentSegment> documents, String citationStyle);

    /**
     * 更新对话历史
     */
    List<ConversationMessage> updateConversationHistory(List<ConversationMessage> history,
                                                        String question, String answer);

    /**
     * 估算Token使用量
     */
    Integer estimateTokens(String question, String answer);

    /**
     * 计算平均分数
     */
    Double calculateAverageScore(List<DocumentSegment> documents);

    /**
     * 生成查询建议
     */
    List<String> generateQuerySuggestions(String partialQuery, Integer limit);

    /**
     * 生成相关查询
     */
    List<String> generateRelatedQueries(String currentQuery, Integer limit);

    /**
     * 获取查询历史
     */
    List<QueryHistoryItem> fetchQueryHistory(QueryHistoryRequest request);

    /**
     * 创建批量查询响应
     */
    BatchQueryResponse createBatchResponse(List<BatchQueryItem> results);

    /**
     * 处理单个批量查询项
     */
    BatchQueryItem processSingleBatchQuery(String question, BatchQueryRequest request,
                                           List<DocumentSegment> documents, String answer);
} 