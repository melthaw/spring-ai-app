package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.*;

import java.util.List;

/**
 * RAG查询服务接口
 * 提供多种查询场景的支持，包括简单查询、复杂查询和一体化查询
 *
 * @author matt
 */
public interface RagQueryService {

    // ==================== Case by Case 查询接口 ====================

    /**
     * 简单文本查询 - 基于单个知识库的基础问答
     *
     * @param request 简单查询请求
     * @return 查询结果
     */
    SimpleQueryResponse simpleQuery(SimpleQueryRequest request);

    /**
     * 多知识库查询 - 跨多个知识库进行查询
     *
     * @param request 多知识库查询请求
     * @return 查询结果
     */
    MultiKnowledgeBaseQueryResponse multiKnowledgeBaseQuery(MultiKnowledgeBaseQueryRequest request);

    /**
     * 语义相似度查询 - 基于向量相似度的文档检索
     *
     * @param request 语义查询请求
     * @return 查询结果
     */
    SemanticQueryResponse semanticQuery(SemanticQueryRequest request);

    /**
     * 混合查询 - 结合关键词和语义的混合检索
     *
     * @param request 混合查询请求
     * @return 查询结果
     */
    HybridQueryResponse hybridQuery(HybridQueryRequest request);

    /**
     * 对话式查询 - 支持上下文的多轮对话
     *
     * @param request 对话查询请求
     * @return 查询结果
     */
    ConversationalQueryResponse conversationalQuery(ConversationalQueryRequest request);

    /**
     * 结构化查询 - 支持过滤条件的结构化检索
     *
     * @param request 结构化查询请求
     * @return 查询结果
     */
    StructuredQueryResponse structuredQuery(StructuredQueryRequest request);

    /**
     * 摘要查询 - 对检索结果进行摘要生成
     *
     * @param request 摘要查询请求
     * @return 查询结果
     */
    SummaryQueryResponse summaryQuery(SummaryQueryRequest request);

    /**
     * 引用查询 - 提供详细的引用信息和来源
     *
     * @param request 引用查询请求
     * @return 查询结果
     */
    CitationQueryResponse citationQuery(CitationQueryRequest request);

    // ==================== All in One 查询接口 ====================

    /**
     * 智能查询 - 一体化查询接口，自动选择最佳查询策略
     * 根据查询内容和参数自动选择合适的查询方式
     *
     * @param request 智能查询请求
     * @return 查询结果
     */
    IntelligentQueryResponse intelligentQuery(IntelligentQueryRequest request);

    /**
     * 批量查询 - 支持批量问题的并行处理
     *
     * @param request 批量查询请求
     * @return 查询结果列表
     */
    List<BatchQueryResponse> batchQuery(BatchQueryRequest request);

    // ==================== 辅助查询接口 ====================

    /**
     * 查询建议 - 基于输入提供查询建议
     *
     * @param request 查询建议请求
     * @return 建议列表
     */
    QuerySuggestionResponse getQuerySuggestions(QuerySuggestionRequest request);

    /**
     * 相关查询 - 基于当前查询推荐相关问题
     *
     * @param request 相关查询请求
     * @return 相关查询列表
     */
    RelatedQueryResponse getRelatedQueries(RelatedQueryRequest request);

    /**
     * 查询历史 - 获取用户查询历史
     *
     * @param request 查询历史请求
     * @return 历史记录
     */
    QueryHistoryResponse getQueryHistory(QueryHistoryRequest request);
} 