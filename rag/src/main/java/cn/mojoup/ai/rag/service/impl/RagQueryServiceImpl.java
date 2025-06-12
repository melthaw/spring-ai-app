package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.*;
import cn.mojoup.ai.rag.exception.RagException;
import cn.mojoup.ai.rag.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * RAG查询服务实现类
 * 负责参数组织和返回结果处理，核心查询逻辑委派给专门的服务组件
 *
 * @author matt
 */
@Slf4j
@Service
public class RagQueryServiceImpl implements RagQueryService {

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private KeywordSearchService keywordSearchService;

    @Autowired
    private HybridSearchService hybridSearchService;

    @Autowired
    private StructuredSearchService structuredSearchService;

    @Autowired
    private IntelligentSearchService intelligentSearchService;

    @Autowired
    private AnswerGenerationService answerGenerationService;

    @Autowired
    private SummaryGenerationService summaryGenerationService;

    @Autowired
    private RagAssistantService ragAssistantService;

    @Override
    public SimpleQueryResponse simpleQuery(SimpleQueryRequest request) {
        try {
            log.info("执行简单查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行向量检索
            List<DocumentSegment> documents = vectorSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成答案
            String answer = answerGenerationService.generateAnswer(
                    request.getQuestion(),
                    documents,
                    request.getTemperature(),
                    request.getMaxTokens()
            );

            // 组装响应
            SimpleQueryResponse response = new SimpleQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("simple");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setAnswer(answer);
            response.setDocuments(documents);
            response.setModel("qwen-max");
            response.setTokensUsed(ragAssistantService.estimateTokens(request.getQuestion(), answer));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("简单查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public MultiKnowledgeBaseQueryResponse multiKnowledgeBaseQuery(MultiKnowledgeBaseQueryRequest request) {
        try {
            log.info("执行多知识库查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 并行查询多个知识库
            List<DocumentSegment> allDocuments = new ArrayList<>();
            Map<String, Integer> knowledgeBaseScores = new HashMap<>();

            for (String kbId : request.getKnowledgeBaseIds()) {
                List<DocumentSegment> kbDocuments = vectorSearchService.search(
                        request.getQuestion(), kbId, request.getLimit(), request.getSimilarityThreshold()
                );
                allDocuments.addAll(kbDocuments);
                knowledgeBaseScores.put(kbId, kbDocuments.size());
            }

            // 生成答案
            String answer = answerGenerationService.generateAnswer(
                    request.getQuestion(),
                    allDocuments,
                    request.getTemperature(),
                    request.getMaxTokens()
            );

            // 组装响应
            MultiKnowledgeBaseQueryResponse response = new MultiKnowledgeBaseQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("multi_knowledge_base");
            response.setKnowledgeBaseIds(request.getKnowledgeBaseIds());
            response.setAnswer(answer);
            response.setDocuments(allDocuments);
            response.setKnowledgeBaseScores(knowledgeBaseScores);
            response.setModel("qwen-max");
            response.setTokensUsed(ragAssistantService.estimateTokens(request.getQuestion(), answer));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("多知识库查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public SemanticQueryResponse semanticQuery(SemanticQueryRequest request) {
        try {
            log.info("执行语义查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行语义检索
            List<DocumentSegment> documents = vectorSearchService.semanticSearch(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getEmbeddingModel(),
                    request.getLimit(),
                    request.getSimilarityThreshold(),
                    request.getRerank()
            );

            // 组装响应
            SemanticQueryResponse response = new SemanticQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("semantic");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setEmbeddingModel(request.getEmbeddingModel());
            response.setDocuments(documents);
            response.setAverageScore(ragAssistantService.calculateAverageScore(documents));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("语义查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public HybridQueryResponse hybridQuery(HybridQueryRequest request) {
        try {
            log.info("执行混合查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行混合检索
            List<DocumentSegment> documents = hybridSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getKeywords(),
                    request.getKeywordWeight(),
                    request.getSemanticWeight(),
                    request.getEnableRerank(),
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成答案
            String answer = answerGenerationService.generateAnswer(
                    request.getQuestion(), documents, 0.7, 2000
            );

            // 组装响应
            HybridQueryResponse response = new HybridQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("hybrid");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setAnswer(answer);
            response.setDocuments(documents);
            response.setMatchedKeywords(request.getKeywords());
            response.setHybridScore(ragAssistantService.calculateAverageScore(documents));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("混合查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public ConversationalQueryResponse conversationalQuery(ConversationalQueryRequest request) {
        try {
            log.info("执行对话式查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行向量检索
            List<DocumentSegment> documents = vectorSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成对话式答案
            String answer = answerGenerationService.generateConversationalAnswer(
                    request.getQuestion(),
                    documents,
                    request.getConversationHistory(),
                    request.getTemperature(),
                    request.getMaxTokens()
            );

            // 更新对话历史
            List<ConversationMessage> updatedHistory = ragAssistantService.updateConversationHistory(
                    request.getConversationHistory(), request.getQuestion(), answer
            );

            // 组装响应
            ConversationalQueryResponse response = new ConversationalQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("conversational");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setAnswer(answer);
            response.setDocuments(documents);
            response.setUpdatedHistory(updatedHistory);
            response.setModel("qwen-max");
            response.setTokensUsed(ragAssistantService.estimateTokens(request.getQuestion(), answer));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("对话式查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public StructuredQueryResponse structuredQuery(StructuredQueryRequest request) {
        try {
            log.info("执行结构化查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行结构化检索
            List<DocumentSegment> documents = structuredSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getFilters(),
                    request.getRequiredFields(),
                    request.getSortBy(),
                    request.getSortOrder(),
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成答案
            String answer = answerGenerationService.generateAnswer(
                    request.getQuestion(), documents, 0.7, 2000
            );

            // 组装响应
            StructuredQueryResponse response = new StructuredQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("structured");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setAnswer(answer);
            response.setDocuments(documents);
            response.setAppliedFilters(request.getFilters());
            response.setTotalMatches(documents.size());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("结构化查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public SummaryQueryResponse summaryQuery(SummaryQueryRequest request) {
        try {
            log.info("执行摘要查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行向量检索
            List<DocumentSegment> documents = vectorSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成摘要
            String summary = summaryGenerationService.generateSummary(
                    request.getQuestion(),
                    documents,
                    request.getSummaryType(),
                    request.getSummaryLength(),
                    request.getTemperature()
            );

            // 组装响应
            SummaryQueryResponse response = new SummaryQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("summary");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setSummaryType(request.getSummaryType());
            response.setSummaryLength(request.getSummaryLength());
            response.setSummary(summary);
            response.setSourceDocuments(documents);
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("摘要查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public CitationQueryResponse citationQuery(CitationQueryRequest request) {
        try {
            log.info("执行引用查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 执行向量检索
            List<DocumentSegment> documents = vectorSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseId(),
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成带引用的答案
            String answer = answerGenerationService.generateAnswerWithCitations(
                    request.getQuestion(),
                    documents,
                    request.getCitationStyle(),
                    request.getIncludePage(),
                    request.getTemperature(),
                    request.getMaxTokens()
            );

            // 生成引用
            List<Citation> citations = ragAssistantService.generateCitations(
                    documents, request.getCitationStyle()
            );

            // 组装响应
            CitationQueryResponse response = new CitationQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("citation");
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setCitationStyle(request.getCitationStyle());
            response.setAnswer(answer);
            response.setCitations(citations);
            response.setTokensUsed(ragAssistantService.estimateTokens(request.getQuestion(), answer));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("引用查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public IntelligentQueryResponse intelligentQuery(IntelligentQueryRequest request) {
        try {
            log.info("执行智能查询: {}", request.getQuestion());
            long startTime = System.currentTimeMillis();

            // 意图识别和策略选择
            String detectedIntent = ragAssistantService.detectQueryIntent(request.getQuestion());
            String selectedStrategy = ragAssistantService.selectOptimalStrategy(request.getQuestion(), detectedIntent);

            // 执行智能检索
            List<DocumentSegment> documents = intelligentSearchService.search(
                    request.getQuestion(),
                    request.getKnowledgeBaseIds(),
                    selectedStrategy,
                    request.getLimit(),
                    request.getSimilarityThreshold()
            );

            // 生成优化答案
            String answer = answerGenerationService.generateOptimizedAnswer(
                    request.getQuestion(),
                    documents,
                    detectedIntent,
                    selectedStrategy,
                    request.getTemperature(),
                    request.getMaxTokens()
            );

            // 构建优化信息
            Map<String, Object> optimizations = new HashMap<>();
            optimizations.put("intent_confidence", 0.95);
            optimizations.put("strategy_score", 0.88);
            optimizations.put("document_relevance", ragAssistantService.calculateAverageScore(documents));

            // 组装响应
            IntelligentQueryResponse response = new IntelligentQueryResponse();
            response.setQueryId(UUID.randomUUID().toString());
            response.setQuestion(request.getQuestion());
            response.setQueryTime(LocalDateTime.now());
            response.setQueryType("intelligent");
            response.setDetectedIntent(detectedIntent);
            response.setSelectedStrategy(selectedStrategy);
            response.setAnswer(answer);
            response.setDocuments(documents);
            response.setUsedKnowledgeBases(request.getKnowledgeBaseIds());
            response.setOptimizations(optimizations);
            response.setTokensUsed(ragAssistantService.estimateTokens(request.getQuestion(), answer));
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setSuccess(true);

            return response;

        } catch (Exception e) {
            log.error("智能查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public List<BatchQueryResponse> batchQuery(BatchQueryRequest request) {
        try {
            log.info("执行批量查询，问题数量: {}", request.getQuestions().size());
            long startTime = System.currentTimeMillis();

            List<BatchQueryItem> results = new ArrayList<>();

            for (String question : request.getQuestions()) {
                try {
                    // 执行单个查询
                    List<DocumentSegment> documents = vectorSearchService.search(
                            question, request.getKnowledgeBaseId(),
                            request.getLimit(), request.getSimilarityThreshold()
                    );

                    String answer = answerGenerationService.generateAnswer(
                            question, documents, 0.7, 2000
                    );

                    // 处理单个批量查询项
                    BatchQueryItem item = ragAssistantService.processSingleBatchQuery(
                            question, request, documents, answer
                    );
                    results.add(item);

                } catch (Exception e) {
                    log.error("批量查询中单个问题处理失败: {}", question, e);
                    BatchQueryItem errorItem = new BatchQueryItem();
                    errorItem.setQuestion(question);
                    errorItem.setSuccess(false);
                    errorItem.setErrorMessage(e.getMessage());
                    errorItem.setProcessingTime(0L);
                    results.add(errorItem);
                }
            }

            // 创建批量响应
            BatchQueryResponse batchResponse = ragAssistantService.createBatchResponse(results);
            batchResponse.setTotalProcessingTime(System.currentTimeMillis() - startTime);

            return List.of(batchResponse);

        } catch (Exception e) {
            log.error("批量查询失败", e);
            throw new RagException(500, "查询失败: " + e.getMessage());
        }
    }

    @Override
    public QuerySuggestionResponse getQuerySuggestions(QuerySuggestionRequest request) {
        try {
            log.info("获取查询建议: {}", request.getPartialQuery());

            List<String> suggestions = ragAssistantService.generateQuerySuggestions(
                    request.getPartialQuery(), request.getLimit()
            );

            QuerySuggestionResponse response = new QuerySuggestionResponse();
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setSuggestions(suggestions);

            return response;

        } catch (Exception e) {
            log.error("获取查询建议失败", e);
            throw new RagException(500, "获取建议失败: " + e.getMessage());
        }
    }

    @Override
    public RelatedQueryResponse getRelatedQueries(RelatedQueryRequest request) {
        try {
            log.info("获取相关查询: {}", request.getCurrentQuery());

            List<String> relatedQueries = ragAssistantService.generateRelatedQueries(
                    request.getCurrentQuery(), request.getLimit()
            );

            RelatedQueryResponse response = new RelatedQueryResponse();
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
            response.setRelatedQueries(relatedQueries);

            return response;

        } catch (Exception e) {
            log.error("获取相关查询失败", e);
            throw new RagException(500, "获取相关查询失败: " + e.getMessage());
        }
    }

    @Override
    public QueryHistoryResponse getQueryHistory(QueryHistoryRequest request) {
        try {
            log.info("获取查询历史: userId={}", request.getUserId());

            List<QueryHistoryItem> history = ragAssistantService.fetchQueryHistory(request);

            QueryHistoryResponse response = new QueryHistoryResponse();
            response.setUserId(request.getUserId());
            response.setHistory(history);
            response.setTotalCount(history.size());

            return response;

        } catch (Exception e) {
            log.error("获取查询历史失败", e);
            throw new RagException(500, "获取历史失败: " + e.getMessage(), e);
        }
    }

}