package cn.mojoup.ai.rag.service.impl;


import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.HybridSearchService;
import cn.mojoup.ai.rag.service.IntelligentSearchService;
import cn.mojoup.ai.rag.service.KeywordSearchService;
import cn.mojoup.ai.rag.service.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI驱动的智能检索服务实现类
 * 使用大模型进行智能意图检测和策略选择
 *
 * @author matt
 */
@Slf4j
@Service
public class IntelligentSearchServiceImpl implements IntelligentSearchService {

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private KeywordSearchService keywordSearchService;

    @Autowired
    private HybridSearchService hybridSearchService;

    @Value("${rag.ai.model:gpt-3.5-turbo}")
    private String aiModel;

    @Value("${rag.ai.temperature:0.2}")
    private Double aiTemperature;

    @Value("${rag.search.intelligent.enable-ai-intent:true}")
    private Boolean enableAIIntent;

    @Value("${rag.search.intelligent.intent-confidence-threshold:0.8}")
    private Double intentConfidenceThreshold;

    @Autowired
    private ChatClient chatClient;

    @Override
    public List<DocumentSegment> search(String query, List<String> knowledgeBaseIds,
                                        String strategy, Integer limit, Double threshold) {
        log.info("AI驱动智能检索: query={}, strategy={}, knowledgeBaseIds={}, limit={}",
                 query, strategy, knowledgeBaseIds, limit);

        try {
            // 如果没有指定策略，使用AI进行智能策略选择
            if (strategy == null || strategy.isEmpty()) {
                String intent = detectIntent(query);
                strategy = selectStrategy(query, intent);
                log.info("AI智能策略选择: intent={}, selectedStrategy={}", intent, strategy);
            }

            List<List<DocumentSegment>> kbResults = new ArrayList<>();

            for (String kbId : knowledgeBaseIds) {
                List<DocumentSegment> kbResult = executeSearchByStrategy(query, kbId, strategy, limit, threshold);
                if (!kbResult.isEmpty()) {
                    kbResults.add(kbResult);
                }
            }

            return mergeMultiKbResults(kbResults, limit);

        } catch (Exception e) {
            log.error("智能检索失败: query={}, error={}", query, e.getMessage(), e);
            // 降级到基础向量搜索
            return fallbackToBasicSearch(query, knowledgeBaseIds.get(0), limit, threshold);
        }
    }

    @Override
    public String detectIntent(String query) {
        log.debug("AI意图检测: query={}", query);

        if (query == null || query.trim().isEmpty()) {
            return "general_qa";
        }

        try {
            if (enableAIIntent) {
                return detectIntentWithAI(query);
            } else {
                return detectIntentWithRules(query);
            }
        } catch (Exception e) {
            log.error("意图检测失败，降级到规则方法: query={}, error={}", query, e.getMessage());
            return detectIntentWithRules(query);
        }
    }

    @Override
    public String selectStrategy(String query, String intent) {
        log.debug("AI策略选择: query={}, intent={}", query, intent);

        try {
            if (enableAIIntent) {
                return selectStrategyWithAI(query, intent);
            } else {
                return selectStrategyWithRules(intent);
            }
        } catch (Exception e) {
            log.error("策略选择失败，降级到规则方法: intent={}, error={}", intent, e.getMessage());
            return selectStrategyWithRules(intent);
        }
    }

    @Override
    public List<DocumentSegment> mergeMultiKbResults(List<List<DocumentSegment>> kbResults, Integer limit) {
        log.debug("合并多知识库结果: kbCount={}, limit={}", kbResults.size(), limit);

        if (kbResults.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用智能合并策略
        return mergeWithIntelligentStrategy(kbResults, limit);
    }

    // ==================== AI驱动的核心方法 ====================

    /**
     * 使用AI进行意图检测
     */
    private String detectIntentWithAI(String query) {
        String prompt = buildIntentDetectionPrompt(query);

        log.debug("AI意图检测提示词: {}", prompt);

        List<Message> messages = List.of(UserMessage.builder().text(prompt).build());
        ChatResponse response = chatClient.prompt(new Prompt(messages))
                                          .messages(messages)
                                          .advisors(new SimpleLoggerAdvisor())
                                          .call()
                                          .chatResponse();
        String aiResponse = response.getResult().getOutput().getText();

        return parseIntentFromAIResponse(aiResponse);
    }

    /**
     * 使用AI进行策略选择
     */
    private String selectStrategyWithAI(String query, String intent) {
        String prompt = buildStrategySelectionPrompt(query, intent);

        log.debug("AI策略选择提示词: {}", prompt);

        // TODO: 替换为实际的AI调用
        String aiResponse = simulateAIStrategySelection(query, intent);

        return parseStrategyFromAIResponse(aiResponse);
    }

    // ==================== 提示词构建方法 ====================

    /**
     * 构建意图检测提示词
     */
    private String buildIntentDetectionPrompt(String query) {
        return String.format("""
                                     你是一个专业的查询意图分析专家。请分析用户查询的意图类型。
                                                     
                                     意图类型定义：
                                     1. definition - 定义类：询问概念、含义、定义等
                                     2. explanation - 解释类：询问原因、机制、过程等
                                     3. how_to - 操作类：询问方法、步骤、操作流程等
                                     4. example - 示例类：询问例子、案例、实例等
                                     5. comparison - 比较类：询问区别、对比、差异等
                                     6. analysis - 分析类：询问分析、评估、优缺点等
                                     7. summary - 总结类：询问总结、概括、摘要等
                                     8. factual - 事实类：询问具体数据、时间、地点等
                                     9. opinion - 观点类：询问看法、建议、推荐等
                                     10. general_qa - 一般问答：其他类型的问题
                                                     
                                     用户查询：%s
                                                     
                                     请按以下JSON格式返回结果：
                                     {
                                         "intent": "意图类型",
                                         "confidence": 0.95,
                                         "reasoning": "判断理由的简短说明",
                                         "keywords": ["关键指示词1", "关键指示词2"],
                                         "query_type": "simple|complex|multi_intent"
                                     }
                                     """, query);
    }

    /**
     * 构建策略选择提示词
     */
    private String buildStrategySelectionPrompt(String query, String intent) {
        return String.format("""
                                     你是一个专业的搜索策略选择专家。请根据查询内容和意图类型选择最佳的搜索策略。
                                                     
                                     可用搜索策略：
                                     1. semantic - 语义搜索：适合概念性、抽象性问题，利用向量相似度
                                     2. keyword - 关键词搜索：适合精确匹配、术语查找、具体操作步骤
                                     3. hybrid - 混合搜索：结合语义和关键词，适合复杂问题
                                     4. structured - 结构化搜索：适合有明确过滤条件的查询
                                                     
                                     查询内容：%s
                                     检测意图：%s
                                                     
                                     选择标准：
                                     - definition/explanation类问题：倾向于semantic，理解概念内涵
                                     - how_to/example类问题：倾向于keyword，精确匹配操作步骤
                                     - comparison/analysis类问题：倾向于hybrid，需要全面信息
                                     - factual类问题：倾向于keyword，精确匹配具体信息
                                     - 复杂多意图问题：倾向于hybrid，综合多种搜索能力
                                                     
                                     请按以下JSON格式返回结果：
                                     {
                                         "strategy": "搜索策略",
                                         "confidence": 0.90,
                                         "reasoning": "选择理由",
                                         "alternative": "备选策略",
                                         "parameters": {
                                             "keyword_weight": 0.3,
                                             "semantic_weight": 0.7,
                                             "enable_rerank": true
                                         }
                                     }
                                     """, query, intent);
    }

    // ==================== AI响应解析方法 ====================

    /**
     * 解析AI意图检测响应
     */
    private String parseIntentFromAIResponse(String aiResponse) {
        try {
            // TODO: 使用实际的JSON解析
            return parseSimpleIntentResponse(aiResponse);
        } catch (Exception e) {
            log.error("解析AI意图响应失败: {}", e.getMessage());
            return "general_qa";
        }
    }

    /**
     * 解析AI策略选择响应
     */
    private String parseStrategyFromAIResponse(String aiResponse) {
        try {
            // TODO: 使用实际的JSON解析
            return parseSimpleStrategyResponse(aiResponse);
        } catch (Exception e) {
            log.error("解析AI策略响应失败: {}", e.getMessage());
            return "hybrid"; // 默认策略
        }
    }

    // ==================== 模拟AI响应方法（开发阶段使用）====================

    private String simulateAIIntentDetection(String query) {
        String lowerQuery = query.toLowerCase();
        String intent = "general_qa";
        double confidence = 0.7;
        String reasoning = "基于关键词模式匹配";
        List<String> keywords = new ArrayList<>();
        String queryType = "simple";

        // 智能意图分析
        if (lowerQuery.contains("什么是") || lowerQuery.contains("定义") ||
            lowerQuery.contains("含义") || lowerQuery.contains("概念") ||
            lowerQuery.contains("什么叫") || lowerQuery.contains("指的是")) {
            intent = "definition";
            confidence = 0.95;
            reasoning = "包含明确的定义询问词汇";
            keywords.addAll(Arrays.asList("什么是", "定义", "概念"));
        } else if (lowerQuery.contains("如何") || lowerQuery.contains("怎么") ||
                   lowerQuery.contains("步骤") || lowerQuery.contains("方法") ||
                   lowerQuery.contains("怎样") || lowerQuery.contains("如何实现")) {
            intent = "how_to";
            confidence = 0.92;
            reasoning = "包含操作指导相关词汇";
            keywords.addAll(Arrays.asList("如何", "怎么", "步骤", "方法"));
        } else if (lowerQuery.contains("为什么") || lowerQuery.contains("原因") ||
                   lowerQuery.contains("解释") || lowerQuery.contains("说明") ||
                   lowerQuery.contains("机制") || lowerQuery.contains("原理")) {
            intent = "explanation";
            confidence = 0.90;
            reasoning = "包含解释说明相关词汇";
            keywords.addAll(Arrays.asList("为什么", "原因", "解释"));
        } else if (lowerQuery.contains("区别") || lowerQuery.contains("对比") ||
                   lowerQuery.contains("比较") || lowerQuery.contains("差异") ||
                   lowerQuery.contains("不同") || lowerQuery.contains("差别")) {
            intent = "comparison";
            confidence = 0.88;
            reasoning = "包含比较对比相关词汇";
            keywords.addAll(Arrays.asList("区别", "对比", "比较"));
            queryType = "complex";
        } else if (lowerQuery.contains("例子") || lowerQuery.contains("示例") ||
                   lowerQuery.contains("案例") || lowerQuery.contains("举例") ||
                   lowerQuery.contains("实例") || lowerQuery.contains("样例")) {
            intent = "example";
            confidence = 0.87;
            reasoning = "包含示例请求相关词汇";
            keywords.addAll(Arrays.asList("例子", "示例", "案例"));
        } else if (lowerQuery.contains("分析") || lowerQuery.contains("评估") ||
                   lowerQuery.contains("优缺点") || lowerQuery.contains("影响") ||
                   lowerQuery.contains("利弊") || lowerQuery.contains("评价")) {
            intent = "analysis";
            confidence = 0.85;
            reasoning = "包含分析评估相关词汇";
            keywords.addAll(Arrays.asList("分析", "评估", "优缺点"));
            queryType = "complex";
        }

        return String.format("""
                                     {
                                         "intent": "%s",
                                         "confidence": %.2f,
                                         "reasoning": "%s",
                                         "keywords": %s,
                                         "query_type": "%s"
                                     }
                                     """, intent, confidence, reasoning,
                             keywords.stream().map(k -> "\"" + k + "\"").collect(Collectors.joining(", ", "[", "]")),
                             queryType);
    }

    private String simulateAIStrategySelection(String query, String intent) {
        String strategy = "hybrid";
        double confidence = 0.8;
        String reasoning = "默认混合策略";
        String alternative = "semantic";
        double keywordWeight = 0.3;
        double semanticWeight = 0.7;
        boolean enableRerank = true;

        // 基于意图的智能策略选择
        switch (intent) {
            case "definition":
            case "explanation":
                strategy = "semantic";
                confidence = 0.92;
                reasoning = "定义和解释类问题适合语义搜索，能更好理解概念内涵";
                alternative = "hybrid";
                keywordWeight = 0.2;
                semanticWeight = 0.8;
                break;

            case "how_to":
            case "example":
                strategy = "keyword";
                confidence = 0.88;
                reasoning = "操作和示例类问题适合关键词搜索，精确匹配具体内容";
                alternative = "hybrid";
                keywordWeight = 0.7;
                semanticWeight = 0.3;
                enableRerank = false;
                break;

            case "comparison":
            case "analysis":
                strategy = "hybrid";
                confidence = 0.95;
                reasoning = "比较和分析类问题需要综合多种信息，混合搜索效果最佳";
                alternative = "semantic";
                keywordWeight = 0.4;
                semanticWeight = 0.6;
                break;

            case "factual":
                strategy = "keyword";
                confidence = 0.90;
                reasoning = "事实类问题需要精确匹配，关键词搜索更适合";
                alternative = "structured";
                keywordWeight = 0.8;
                semanticWeight = 0.2;
                break;

            default:
                // 复杂查询的特殊处理
                if (query.length() > 50 || query.contains("并且") || query.contains("同时")) {
                    confidence = 0.85;
                    reasoning = "复杂查询使用混合搜索获得最佳平衡";
                }
        }

        return String.format("""
                                     {
                                         "strategy": "%s",
                                         "confidence": %.2f,
                                         "reasoning": "%s",
                                         "alternative": "%s",
                                         "parameters": {
                                             "keyword_weight": %.1f,
                                             "semantic_weight": %.1f,
                                             "enable_rerank": %b
                                         }
                                     }
                                     """, strategy, confidence, reasoning, alternative,
                             keywordWeight, semanticWeight, enableRerank);
    }

    // ==================== 辅助解析方法 ====================

    private String parseSimpleIntentResponse(String response) {
        if (response.contains("\"intent\"")) {
            try {
                String intentSection = response.substring(response.indexOf("\"intent\""));
                String intent = intentSection.substring(intentSection.indexOf(":") + 1);
                intent = intent.substring(intent.indexOf("\"") + 1);
                intent = intent.substring(0, intent.indexOf("\""));
                return intent.trim();
            } catch (Exception e) {
                log.warn("解析意图响应失败: {}", response);
            }
        }
        return "general_qa";
    }

    private String parseSimpleStrategyResponse(String response) {
        if (response.contains("\"strategy\"")) {
            try {
                String strategySection = response.substring(response.indexOf("\"strategy\""));
                String strategy = strategySection.substring(strategySection.indexOf(":") + 1);
                strategy = strategy.substring(strategy.indexOf("\"") + 1);
                strategy = strategy.substring(0, strategy.indexOf("\""));
                return strategy.trim();
            } catch (Exception e) {
                log.warn("解析策略响应失败: {}", response);
            }
        }
        return "hybrid";
    }

    // ==================== 降级方法 ====================

    /**
     * 基于规则的意图检测（降级方法）
     */
    private String detectIntentWithRules(String query) {
        String lowerQuery = query.toLowerCase();

        // 定义类问题
        if (lowerQuery.contains("什么是") || lowerQuery.contains("定义") ||
            lowerQuery.contains("含义") || lowerQuery.contains("概念")) {
            return "definition";
        }

        // 操作类问题
        if (lowerQuery.contains("如何") || lowerQuery.contains("怎么") ||
            lowerQuery.contains("步骤") || lowerQuery.contains("方法")) {
            return "how_to";
        }

        // 解释类问题
        if (lowerQuery.contains("为什么") || lowerQuery.contains("原因") ||
            lowerQuery.contains("解释") || lowerQuery.contains("说明")) {
            return "explanation";
        }

        // 比较类问题
        if (lowerQuery.contains("区别") || lowerQuery.contains("对比") ||
            lowerQuery.contains("比较") || lowerQuery.contains("差异")) {
            return "comparison";
        }

        // 分析类问题
        if (lowerQuery.contains("分析") || lowerQuery.contains("评估") ||
            lowerQuery.contains("优缺点") || lowerQuery.contains("影响")) {
            return "analysis";
        }

        // 总结类问题
        if (lowerQuery.contains("总结") || lowerQuery.contains("概括") ||
            lowerQuery.contains("摘要") || lowerQuery.contains("归纳")) {
            return "summary";
        }

        // 示例类问题
        if (lowerQuery.contains("例子") || lowerQuery.contains("示例") ||
            lowerQuery.contains("案例") || lowerQuery.contains("举例")) {
            return "example";
        }

        return "general_qa";
    }

    /**
     * 基于规则的策略选择（降级方法）
     */
    private String selectStrategyWithRules(String intent) {
        switch (intent) {
            case "definition":
            case "explanation":
                return "semantic";
            case "how_to":
            case "example":
                return "keyword";
            case "comparison":
            case "analysis":
                return "hybrid";
            case "summary":
                return "semantic";
            default:
                return "hybrid";
        }
    }

    /**
     * 智能合并策略
     */
    private List<DocumentSegment> mergeWithIntelligentStrategy(List<List<DocumentSegment>> kbResults, Integer limit) {
        // 使用加权轮询方式，优先选择高分文档
        List<DocumentSegment> mergedResults = new ArrayList<>();
        Map<String, DocumentSegment> deduplicationMap = new HashMap<>();

        // 计算每个知识库的平均分数，用于权重计算
        Map<Integer, Double> kbWeights = new HashMap<>();
        for (int i = 0; i < kbResults.size(); i++) {
            List<DocumentSegment> kbResult = kbResults.get(i);
            double avgScore = kbResult.stream().mapToDouble(DocumentSegment::getScore).average().orElse(0.0);
            kbWeights.put(i, avgScore);
        }

        int maxSize = kbResults.stream().mapToInt(List::size).max().orElse(0);

        for (int i = 0; i < maxSize && mergedResults.size() < limit; i++) {
            // 按权重排序知识库
            List<Integer> sortedKbIndexes = kbWeights.entrySet().stream()
                                                     .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                                                     .map(Map.Entry::getKey)
                                                     .collect(Collectors.toList());

            for (Integer kbIndex : sortedKbIndexes) {
                List<DocumentSegment> kbResult = kbResults.get(kbIndex);
                if (i < kbResult.size() && mergedResults.size() < limit) {
                    DocumentSegment doc = kbResult.get(i);

                    // 智能去重
                    String key = generateDocumentKey(doc);
                    if (!deduplicationMap.containsKey(key) ||
                        deduplicationMap.get(key).getScore() < doc.getScore()) {
                        deduplicationMap.put(key, doc);
                    }
                }
            }
        }

        return deduplicationMap.values().stream()
                               .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                               .limit(limit)
                               .collect(Collectors.toList());
    }

    private String generateDocumentKey(DocumentSegment doc) {
        // 更智能的文档去重key生成
        return doc.getDocumentId() + "_" + doc.getPosition();
    }

    private List<DocumentSegment> fallbackToBasicSearch(String query, String knowledgeBaseId,
                                                        Integer limit, Double threshold) {
        log.warn("智能检索失败，降级到基础向量搜索: query={}", query);
        try {
            return vectorSearchService.search(query, knowledgeBaseId, limit, threshold);
        } catch (Exception e) {
            log.error("基础搜索也失败了: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== 私有辅助方法 ====================

    private List<DocumentSegment> executeSearchByStrategy(String query, String knowledgeBaseId,
                                                          String strategy, Integer limit, Double threshold) {
        try {
            switch (strategy.toLowerCase()) {
                case "hybrid":
                    List<String> keywords = keywordSearchService.extractKeywords(query);
                    return hybridSearchService.search(query, knowledgeBaseId, keywords,
                                                      0.3, 0.7, true, limit, threshold);
                case "semantic":
                    return vectorSearchService.semanticSearch(query, knowledgeBaseId, "text-embedding-ada-002",
                                                              limit, threshold, true);
                case "keyword":
                    List<String> extractedKeywords = keywordSearchService.extractKeywords(query);
                    return keywordSearchService.search(query, extractedKeywords, knowledgeBaseId, limit);
                case "structured":
                    // TODO: 实现结构化搜索
                    return vectorSearchService.search(query, knowledgeBaseId, limit, threshold);
                default:
                    return vectorSearchService.search(query, knowledgeBaseId, limit, threshold);
            }
        } catch (Exception e) {
            log.error("执行{}策略搜索失败: query={}, error={}", strategy, query, e.getMessage());
            return vectorSearchService.search(query, knowledgeBaseId, limit, threshold);
        }
    }
}