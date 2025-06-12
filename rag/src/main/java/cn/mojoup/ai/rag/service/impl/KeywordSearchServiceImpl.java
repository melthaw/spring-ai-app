package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.KeywordSearchService;
import cn.mojoup.ai.rag.service.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI驱动的关键词检索服务实现类
 * 使用大模型进行智能关键词提取和文档匹配
 *
 * @author matt
 */
@Slf4j
@Service
public class KeywordSearchServiceImpl implements KeywordSearchService {

    @Autowired
    private VectorSearchService vectorSearchService;

    @Value("${rag.search.keyword.max-keywords:10}")
    private Integer maxKeywords;

    @Value("${rag.search.keyword.boost-factor:0.2}")
    private Double boostFactor;

    @Value("${rag.ai.model:gpt-3.5-turbo}")
    private String aiModel;

    @Value("${rag.ai.temperature:0.3}")
    private Double temperature;

    // TODO: 注入实际的AI客户端
    // @Autowired
    // private ChatClient chatClient;

    @Override
    public List<DocumentSegment> search(String query, List<String> keywords,
                                        String knowledgeBaseId, Integer limit) {
        log.info("AI驱动关键词检索: query={}, keywords={}, knowledgeBaseId={}, limit={}",
                 query, keywords, knowledgeBaseId, limit);

        try {
            // 1. 参数验证
            if (query == null || query.trim().isEmpty()) {
                log.warn("查询字符串为空");
                return new ArrayList<>();
            }

            limit = limit != null ? limit : 10;

            // 2. 使用AI提取关键词（如果未提供）
            if (keywords == null || keywords.isEmpty()) {
                keywords = extractKeywordsWithAI(query);
            }

            if (keywords.isEmpty()) {
                log.warn("AI无法提取到有效关键词，降级到向量搜索");
                return vectorSearchService.search(query, knowledgeBaseId, limit, 0.6);
            }

            // 3. 获取候选文档（向量搜索作为基础）
            List<DocumentSegment> candidateDocuments = vectorSearchService.search(
                    query, knowledgeBaseId, limit * 2, 0.5);

            if (candidateDocuments.isEmpty()) {
                log.warn("未找到候选文档");
                return new ArrayList<>();
            }

            // 4. 使用AI进行智能文档匹配和评分
            List<DocumentSegment> aiRankedDocuments = rankDocumentsWithAI(
                    query, keywords, candidateDocuments);

            // 5. 增强关键词匹配分数
            enhanceKeywordMatching(aiRankedDocuments, keywords);

            // 6. 返回限制数量的结果
            return aiRankedDocuments.stream()
                                    .limit(limit)
                                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("AI关键词检索失败: query={}, error={}", query, e.getMessage(), e);
            // 降级到向量搜索
            return vectorSearchService.search(query, knowledgeBaseId, limit, 0.6);
        }
    }

    @Override
    public List<String> extractKeywords(String query) {
        log.debug("AI关键词提取: query={}", query);

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return extractKeywordsWithAI(query);
        } catch (Exception e) {
            log.error("AI关键词提取失败: query={}, error={}", query, e.getMessage(), e);
            // 降级到简单分词
            return fallbackKeywordExtraction(query);
        }
    }

    @Override
    public void enhanceKeywordMatching(List<DocumentSegment> documents, List<String> keywords) {
        log.debug("AI增强关键词匹配: docCount={}, keywords={}", documents.size(), keywords);

        if (documents == null || documents.isEmpty() || keywords == null || keywords.isEmpty()) {
            return;
        }

        try {
            // 使用AI对每个文档进行关键词匹配评分
            enhanceWithAIMatching(documents, keywords);
        } catch (Exception e) {
            log.error("AI关键词匹配增强失败: error={}", e.getMessage(), e);
            // 降级到传统匹配方法
            enhanceWithTraditionalMatching(documents, keywords);
        }
    }

    // ==================== AI驱动的核心方法 ====================

    /**
     * 使用AI提取关键词
     */
    private List<String> extractKeywordsWithAI(String query) {
        String prompt = buildKeywordExtractionPrompt(query);

        log.debug("AI关键词提取提示词: {}", prompt);

        // TODO: 替换为实际的AI调用
        // ChatResponse response = chatClient.call(
        //     ChatRequest.builder()
        //         .model(aiModel)
        //         .temperature(temperature)
        //         .messages(List.of(new Message(MessageType.USER, prompt)))
        //         .build()
        // );
        // String aiResponse = response.getResult().getOutput().getContent();

        // 模拟AI响应
        String aiResponse = simulateAIKeywordExtraction(query);

        return parseKeywordsFromAIResponse(aiResponse);
    }

    /**
     * 使用AI对文档进行智能排序
     */
    private List<DocumentSegment> rankDocumentsWithAI(String query, List<String> keywords,
                                                      List<DocumentSegment> documents) {
        String prompt = buildDocumentRankingPrompt(query, keywords, documents);

        log.debug("AI文档排序提示词长度: {}", prompt.length());

        // TODO: 替换为实际的AI调用
        String aiResponse = simulateAIDocumentRanking(query, keywords, documents);

        return parseRankedDocumentsFromAIResponse(aiResponse, documents);
    }

    /**
     * 使用AI增强关键词匹配
     */
    private void enhanceWithAIMatching(List<DocumentSegment> documents, List<String> keywords) {
        for (DocumentSegment doc : documents) {
            try {
                String prompt = buildMatchingEnhancementPrompt(doc.getContent(), keywords);

                // TODO: 替换为实际的AI调用
                String aiResponse = simulateAIMatchingEnhancement(doc.getContent(), keywords);

                // 解析AI评分并更新文档
                updateDocumentWithAIScoring(doc, keywords, aiResponse);

            } catch (Exception e) {
                log.error("单个文档AI增强失败: docId={}, error={}", doc.getDocumentId(), e.getMessage());
            }
        }
    }

    // ==================== 提示词构建方法 ====================

    /**
     * 构建关键词提取提示词
     */
    private String buildKeywordExtractionPrompt(String query) {
        return String.format("""
                                     你是一个专业的关键词提取专家。请从用户查询中提取最重要的关键词。
                                                     
                                     要求：
                                     1. 提取3-8个最重要的关键词
                                     2. 关键词应该包含核心概念、实体名称、技术术语等
                                     3. 去除停用词和无意义的词汇
                                     4. 支持中英文混合提取
                                     5. 按重要性降序排列
                                                     
                                     用户查询：%s
                                                     
                                     请按以下JSON格式返回结果：
                                     {
                                         "keywords": ["关键词1", "关键词2", "关键词3"],
                                         "explanation": "提取理由的简短说明"
                                     }
                                     """, query);
    }

    /**
     * 构建文档排序提示词
     */
    private String buildDocumentRankingPrompt(String query, List<String> keywords,
                                              List<DocumentSegment> documents) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("""
                                            你是一个专业的文档相关性评估专家。请根据用户查询和关键词对以下文档进行相关性排序。
                                                            
                                            用户查询：%s
                                            关键词：%s
                                                            
                                            评估标准：
                                            1. 内容相关性：文档内容与查询的匹配程度
                                            2. 关键词覆盖：文档包含的关键词数量和重要性
                                            3. 语义相似性：文档与查询的语义相关程度
                                            4. 信息完整性：文档信息的完整性和准确性
                                                            
                                            文档列表：
                                            """, query, String.join(", ", keywords)));

        for (int i = 0; i < documents.size(); i++) {
            DocumentSegment doc = documents.get(i);
            prompt.append(String.format("""
                                                                    
                                                文档%d:
                                                ID: %s
                                                标题: %s
                                                内容: %s
                                                """, i + 1, doc.getSegmentId(),
                                        doc.getTitle() != null ? doc.getTitle() : "无标题",
                                        truncateContent(doc.getContent(), 200)));
        }

        prompt.append("""
                                              
                              请按以下JSON格式返回排序结果：
                              {
                                  "rankings": [
                                      {
                                          "documentId": "文档ID",
                                          "rank": 1,
                                          "score": 0.95,
                                          "reasoning": "排序理由"
                                      }
                                  ],
                                  "summary": "整体评估总结"
                              }
                              """);

        return prompt.toString();
    }

    /**
     * 构建匹配增强提示词
     */
    private String buildMatchingEnhancementPrompt(String content, List<String> keywords) {
        return String.format("""
                                     你是一个专业的文本匹配分析师。请分析文档内容与关键词的匹配情况。
                                                     
                                     关键词：%s
                                     文档内容：%s
                                                     
                                     请分析以下方面：
                                     1. 关键词匹配度：每个关键词在文档中的匹配情况
                                     2. 语义相关性：文档内容与关键词的语义关联程度
                                     3. 重要性权重：不同关键词在此文档中的重要性
                                     4. 高亮位置：关键匹配内容的位置信息
                                                     
                                     请按以下JSON格式返回结果：
                                     {
                                         "overallScore": 0.85,
                                         "keywordMatches": [
                                             {
                                                 "keyword": "关键词",
                                                 "matchType": "exact|semantic|partial",
                                                 "score": 0.9,
                                                 "positions": [{"start": 10, "end": 15, "text": "匹配文本"}]
                                             }
                                         ],
                                         "semanticRelevance": 0.8,
                                         "summary": "匹配分析总结"
                                     }
                                     """, String.join(", ", keywords), truncateContent(content, 500));
    }

    // ==================== AI响应解析方法 ====================

    /**
     * 解析AI关键词提取响应
     */
    private List<String> parseKeywordsFromAIResponse(String aiResponse) {
        try {
            // TODO: 使用实际的JSON解析
            return parseSimpleKeywordResponse(aiResponse);
        } catch (Exception e) {
            log.error("解析AI关键词响应失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 解析AI文档排序响应
     */
    private List<DocumentSegment> parseRankedDocumentsFromAIResponse(String aiResponse,
                                                                     List<DocumentSegment> originalDocs) {
        try {
            // TODO: 使用实际的JSON解析
            return parseSimpleRankingResponse(aiResponse, originalDocs);
        } catch (Exception e) {
            log.error("解析AI排序响应失败: {}", e.getMessage());
            return originalDocs; // 返回原始顺序
        }
    }

    /**
     * 更新文档的AI评分
     */
    private void updateDocumentWithAIScoring(DocumentSegment doc, List<String> keywords, String aiResponse) {
        try {
            // TODO: 使用实际的JSON解析
            updateSimpleAIScoring(doc, keywords, aiResponse);
        } catch (Exception e) {
            log.error("更新AI评分失败: docId={}, error={}", doc.getDocumentId(), e.getMessage());
        }
    }

    // ==================== 模拟AI响应方法（开发阶段使用）====================

    private String simulateAIKeywordExtraction(String query) {
        // 模拟AI智能关键词提取
        List<String> extractedKeywords = new ArrayList<>();

        // 基于查询内容的智能分析
        if (query.contains("机器学习") || query.contains("machine learning")) {
            extractedKeywords.addAll(Arrays.asList("机器学习", "算法", "模型", "训练"));
        }
        if (query.contains("人工智能") || query.contains("AI") || query.contains("artificial intelligence")) {
            extractedKeywords.addAll(Arrays.asList("人工智能", "深度学习", "神经网络"));
        }
        if (query.contains("数据") || query.contains("data")) {
            extractedKeywords.addAll(Arrays.asList("数据分析", "数据科学", "大数据"));
        }

        // 如果没有匹配到特定领域，进行通用提取
        if (extractedKeywords.isEmpty()) {
            String[] words = query.split("\\s+");
            for (String word : words) {
                if (word.length() > 2 && !isStopWord(word)) {
                    extractedKeywords.add(word);
                }
            }
        }

        return String.format("""
                                     {
                                         "keywords": %s,
                                         "explanation": "基于查询内容的语义分析，提取了核心概念和关键术语"
                                     }
                                     """, extractedKeywords.stream()
                                                           .limit(maxKeywords)
                                                           .map(k -> "\"" + k + "\"")
                                                           .collect(Collectors.joining(", ", "[", "]")));
    }

    private String simulateAIDocumentRanking(String query, List<String> keywords,
                                             List<DocumentSegment> documents) {
        // 模拟AI智能文档排序
        List<String> rankings = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            DocumentSegment doc = documents.get(i);
            double score = calculateMockAIScore(doc, query, keywords);

            rankings.add(String.format("""
                                               {
                                                   "documentId": "%s",
                                                   "rank": %d,
                                                   "score": %.2f,
                                                   "reasoning": "基于内容相关性和关键词匹配度的综合评估"
                                               }""", doc.getSegmentId(), i + 1, score));
        }

        return String.format("""
                                     {
                                         "rankings": [%s],
                                         "summary": "根据查询相关性对文档进行了智能排序"
                                     }
                                     """, String.join(",", rankings));
    }

    private String simulateAIMatchingEnhancement(String content, List<String> keywords) {
        // 模拟AI匹配增强
        List<String> matches = new ArrayList<>();

        for (String keyword : keywords) {
            double score = content.toLowerCase().contains(keyword.toLowerCase()) ? 0.9 : 0.3;
            String matchType = content.toLowerCase().contains(keyword.toLowerCase()) ? "exact" : "semantic";

            matches.add(String.format("""
                                              {
                                                  "keyword": "%s",
                                                  "matchType": "%s",
                                                  "score": %.2f,
                                                  "positions": [{"start": 0, "end": %d, "text": "%s"}]
                                              }""", keyword, matchType, score, keyword.length(), keyword));
        }

        return String.format("""
                                     {
                                         "overallScore": 0.85,
                                         "keywordMatches": [%s],
                                         "semanticRelevance": 0.8,
                                         "summary": "基于AI语义分析的关键词匹配评估"
                                     }
                                     """, String.join(",", matches));
    }

    // ==================== 辅助方法 ====================

    private List<String> parseSimpleKeywordResponse(String response) {
        // 简化的关键词解析
        List<String> keywords = new ArrayList<>();

        // 尝试从JSON中提取关键词
        if (response.contains("keywords")) {
            String keywordSection = response.substring(response.indexOf("["), response.lastIndexOf("]") + 1);
            keywordSection = keywordSection.replaceAll("[\\[\\]\"]", "");
            keywords.addAll(Arrays.asList(keywordSection.split(",\\s*")));
        }

        return keywords.stream()
                       .filter(k -> k.trim().length() > 0)
                       .limit(maxKeywords)
                       .collect(Collectors.toList());
    }

    private List<DocumentSegment> parseSimpleRankingResponse(String response, List<DocumentSegment> originalDocs) {
        // 简化的排序解析 - 保持原始顺序但更新分数
        for (DocumentSegment doc : originalDocs) {
            if (response.contains(doc.getSegmentId())) {
                // 根据AI响应调整分数
                double currentScore = doc.getScore();
                doc.setScore(Math.min(1.0, currentScore * 1.1));
            }
        }

        return originalDocs.stream()
                           .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                           .collect(Collectors.toList());
    }

    private void updateSimpleAIScoring(DocumentSegment doc, List<String> keywords, String response) {
        if (response.contains("overallScore")) {
            try {
                // 简化的分数提取
                String scoreStr = response.substring(response.indexOf("overallScore") + 15);
                scoreStr = scoreStr.substring(0, scoreStr.indexOf(",")).trim();
                double aiScore = Double.parseDouble(scoreStr);

                // 结合AI分数和原始分数
                double finalScore = doc.getScore() * (1 - boostFactor) + aiScore * boostFactor;
                doc.setScore(Math.min(1.0, finalScore));

                // 添加AI分析元数据
                if (doc.getMetadata() == null) {
                    doc.setMetadata(new HashMap<>());
                }
                doc.getMetadata().put("aiScore", aiScore);
                doc.getMetadata().put("aiAnalyzed", true);
                doc.getMetadata().put("searchMethod", "ai_keyword");

            } catch (NumberFormatException e) {
                log.warn("无法解析AI分数: {}", response);
            }
        }
    }

    private double calculateMockAIScore(DocumentSegment doc, String query, List<String> keywords) {
        double score = 0.7; // 基础分数

        String content = doc.getContent().toLowerCase();
        String lowerQuery = query.toLowerCase();

        // 查询匹配加分
        if (content.contains(lowerQuery)) {
            score += 0.2;
        }

        // 关键词匹配加分
        long matchedKeywords = keywords.stream()
                                       .mapToLong(keyword -> content.contains(keyword.toLowerCase()) ? 1 : 0)
                                       .sum();

        score += (matchedKeywords / (double) keywords.size()) * 0.1;

        return Math.min(1.0, score);
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private List<String> fallbackKeywordExtraction(String query) {
        // 降级的关键词提取方法
        return Arrays.stream(query.split("\\s+"))
                     .filter(word -> word.length() > 2)
                     .filter(word -> !isStopWord(word))
                     .limit(maxKeywords)
                     .collect(Collectors.toList());
    }

    private void enhanceWithTraditionalMatching(List<DocumentSegment> documents, List<String> keywords) {
        // 降级的传统匹配方法
        documents.forEach(doc -> {
            long matchCount = keywords.stream()
                                      .mapToLong(keyword -> doc.getContent().toLowerCase()
                                                               .contains(keyword.toLowerCase()) ? 1 : 0)
                                      .sum();

            double boost = matchCount * 0.1;
            doc.setScore(Math.min(1.0, doc.getScore() + boost));
        });
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("的", "了", "在", "是", "我", "有", "和", "the", "a", "an", "and", "or", "but");
        return stopWords.contains(word.toLowerCase());
    }
}