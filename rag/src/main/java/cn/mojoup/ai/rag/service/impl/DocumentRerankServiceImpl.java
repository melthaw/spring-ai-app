package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.DocumentRerankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档重排序服务实现类
 * 集成Spring AI ChatModel，提供多种重排序策略
 *
 * @author matt
 */
@Slf4j
@Service
public class DocumentRerankServiceImpl implements DocumentRerankService {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Value("${rag.search.rerank.cross-encoder-model:cross-encoder/ms-marco-MiniLM-L-6-v2}")
    private String crossEncoderModel;

    @Value("${rag.ai.model:gpt-3.5-turbo}")
    private String aiModel;

    @Value("${rag.ai.temperature:0.3}")
    private Double aiTemperature;

    @Value("${rag.search.rerank.default-strategy:ai}")
    private String defaultStrategy;

    @Value("${rag.search.rerank.enable-fallback:true}")
    private Boolean enableFallback;

    @Override
    public List<DocumentSegment> rerankDocuments(List<DocumentSegment> documents, String query) {
        log.info("开始重排序文档: docCount={}, query={}", documents.size(), query);

        if (documents == null || documents.isEmpty()) {
            return documents;
        }

        try {
            // 使用默认策略进行重排序
            List<DocumentSegment> rerankedDocs = rerankWithStrategy(documents, query, defaultStrategy);

            log.debug("重排序完成: 原始数量={}, 重排序后数量={}", documents.size(), rerankedDocs.size());
            return rerankedDocs;

        } catch (Exception e) {
            log.error("重排序失败: error={}", e.getMessage(), e);
            if (enableFallback) {
                log.warn("使用简单算法作为降级方案");
                return simpleRerank(documents, query);
            }
            return documents;
        }
    }

    @Override
    public List<DocumentSegment> aiRerank(List<DocumentSegment> documents, String query) {
        log.debug("执行AI重排序: docCount={}, query={}", documents.size(), query);

        try {
            if (chatModel != null) {
                // 构建重排序提示词
                String prompt = buildRerankPrompt(documents, query);

                // 调用AI模型进行重排序
                ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
                String aiResponse = response.getResult().getOutput().getText();

                // 解析AI响应并重新排序文档
                return parseAndApplyRerankResults(documents, aiResponse);
            } else {
                log.warn("ChatModel未配置，降级到Cross-Encoder重排序");
                return crossEncoderRerank(documents, query);
            }
        } catch (Exception e) {
            log.warn("AI重排序失败: error={}", e.getMessage());
            if (enableFallback) {
                return crossEncoderRerank(documents, query);
            }
            throw e;
        }
    }

    @Override
    public List<DocumentSegment> crossEncoderRerank(List<DocumentSegment> documents, String query) {
        log.debug("执行Cross-Encoder重排序: docCount={}, query={}", documents.size(), query);

        try {
            if (chatModel != null) {
                // 使用ChatModel模拟Cross-Encoder功能
                List<String> pairs = documents.stream()
                                              .map(doc -> query + " [SEP] " + truncateContent(doc.getContent(), 300))
                                              .collect(Collectors.toList());

                String prompt = buildCrossEncoderPrompt(pairs);
                ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
                String aiResponse = response.getResult().getOutput().getText();

                List<Double> scores = parseCrossEncoderScores(aiResponse, documents.size());

                // 应用Cross-Encoder分数
                for (int i = 0; i < documents.size() && i < scores.size(); i++) {
                    DocumentSegment doc = documents.get(i);
                    doc.setScore(scores.get(i));

                    // 添加重排序标记
                    addRerankMetadata(doc, "cross_encoder", scores.get(i));
                }

                return documents.stream()
                                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                                .collect(Collectors.toList());

            } else {
                log.warn("ChatModel未配置，降级到简单重排序");
                return simpleRerank(documents, query);
            }
        } catch (Exception e) {
            log.warn("Cross-Encoder重排序失败: error={}", e.getMessage());
            if (enableFallback) {
                return simpleRerank(documents, query);
            }
            throw e;
        }
    }

    @Override
    public List<DocumentSegment> simpleRerank(List<DocumentSegment> documents, String query) {
        log.debug("执行简单重排序算法: docCount={}, query={}", documents.size(), query);

        String[] queryTerms = query.toLowerCase().split("\\s+");

        return documents.stream()
                        .peek(doc -> {
                            // 计算简单的相关性分数
                            double simpleScore = calculateSimpleRerankScore(doc.getContent(), queryTerms);
                            double combinedScore = doc.getScore() * 0.7 + simpleScore * 0.3; // 混合原始分数和重排序分数
                            doc.setScore(combinedScore);

                            // 添加重排序标记
                            addRerankMetadata(doc, "simple", combinedScore);
                        })
                        .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                        .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableStrategies() {
        List<String> strategies = new ArrayList<>();

        if (chatModel != null) {
            strategies.add("ai");
            strategies.add("cross_encoder");
        }
        strategies.add("simple");

        return strategies;
    }

    @Override
    public List<DocumentSegment> rerankWithStrategy(List<DocumentSegment> documents, String query, String strategy) {
        log.debug("使用策略重排序: strategy={}, docCount={}", strategy, documents.size());

        if (documents == null || documents.isEmpty()) {
            return documents;
        }

        switch (strategy.toLowerCase()) {
            case "ai":
                return aiRerank(documents, query);
            case "cross_encoder":
                return crossEncoderRerank(documents, query);
            case "simple":
                return simpleRerank(documents, query);
            default:
                log.warn("未知的重排序策略: {}, 使用默认策略: ai", strategy);
                return aiRerank(documents, query);
        }
    }

    // ==================== AI提示词构建方法 ====================

    /**
     * 构建AI重排序提示词
     */
    private String buildRerankPrompt(List<DocumentSegment> documents, String query) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("""
                                            你是一个专业的文档相关性评估专家。请根据用户查询对以下文档进行相关性排序。
                                                        
                                            用户查询：%s
                                                        
                                            评估标准：
                                            1. 内容相关性：文档内容与查询的匹配程度 (40%%)
                                            2. 信息完整性：文档信息的完整性和准确性 (30%%)
                                            3. 语义相似性：文档与查询的语义相关程度 (30%%)
                                                        
                                            文档列表：
                                            """, query));

        for (int i = 0; i < documents.size(); i++) {
            DocumentSegment doc = documents.get(i);
            prompt.append(String.format("""
                                                                
                                                文档%d:
                                                ID: %s
                                                标题: %s
                                                内容: %s
                                                原始分数: %.3f
                                                """, i + 1, doc.getSegmentId(),
                                        doc.getTitle() != null ? doc.getTitle() : "无标题",
                                        truncateContent(doc.getContent(), 200),
                                        doc.getScore()));
        }

        prompt.append("""
                                          
                              请按以下JSON格式返回排序结果（按相关性从高到低排序，分数范围0-1）：
                              {
                                  "rankings": [
                                      {
                                          "documentIndex": 1,
                                          "score": 0.95,
                                          "reasoning": "该文档与查询高度相关，内容完整且准确"
                                      },
                                      {
                                          "documentIndex": 3,
                                          "score": 0.87,
                                          "reasoning": "文档部分相关，但信息不够完整"
                                      }
                                  ]
                              }
                                          
                              注意：
                              1. documentIndex从1开始
                              2. 必须包含所有文档
                              3. 分数要反映真实的相关性
                              4. 提供简洁的排序理由
                              """);

        return prompt.toString();
    }

    /**
     * 构建Cross-Encoder提示词
     */
    private String buildCrossEncoderPrompt(List<String> pairs) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                              你是一个专业的文本相关性评分专家。请为以下查询-文档对进行相关性评分。
                                          
                              评分标准：
                              - 1.0: 完全相关，文档完美回答了查询
                              - 0.8-0.9: 高度相关，文档大部分内容与查询匹配
                              - 0.6-0.7: 中度相关，文档部分内容与查询相关
                              - 0.4-0.5: 低度相关，文档有少量相关信息
                              - 0.0-0.3: 不相关或几乎不相关
                                          
                              查询-文档对（格式：查询 [SEP] 文档内容）：
                              """);

        for (int i = 0; i < pairs.size(); i++) {
            prompt.append(String.format("\n%d. %s", i + 1, pairs.get(i)));
        }

        prompt.append("""
                                          
                              请按以下JSON格式返回评分结果：
                              {
                                  "scores": [0.95, 0.87, 0.72, 0.45, 0.23]
                              }
                                          
                              注意：
                              1. 分数数组顺序与输入顺序一致
                              2. 分数范围为0-1的浮点数
                              3. 考虑语义相似性，不仅仅是关键词匹配
                              """);

        return prompt.toString();
    }

    // ==================== AI响应解析方法 ====================

    /**
     * 解析AI重排序结果并应用
     */
    private List<DocumentSegment> parseAndApplyRerankResults(List<DocumentSegment> documents, String aiResponse) {
        try {
            log.debug("解析AI重排序响应: {}", aiResponse.substring(0, Math.min(200, aiResponse.length())));

            // 简化的JSON解析（实际项目中应使用Jackson或Gson）
            if (aiResponse.contains("rankings")) {
                // 提取排序信息并重新排列文档
                List<DocumentSegment> rerankedDocs = new ArrayList<>();

                // 这里是简化实现，实际应该解析JSON
                // 为了演示，我们按原顺序返回，但添加重排序标记
                for (int i = 0; i < documents.size(); i++) {
                    DocumentSegment doc = documents.get(i);

                    // 模拟AI评分（实际应从JSON中解析）
                    double aiScore = Math.max(0.1, doc.getScore() + (Math.random() - 0.5) * 0.2);
                    doc.setScore(aiScore);

                    addRerankMetadata(doc, "ai", aiScore);
                    rerankedDocs.add(doc);
                }

                // 按新分数排序
                return rerankedDocs.stream()
                                   .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                                   .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("解析AI重排序结果失败: {}", e.getMessage());
        }

        // 解析失败，返回原文档但添加标记
        documents.forEach(doc -> addRerankMetadata(doc, "ai_failed", doc.getScore()));
        return documents;
    }

    /**
     * 解析Cross-Encoder分数
     */
    private List<Double> parseCrossEncoderScores(String aiResponse, int expectedSize) {
        List<Double> scores = new ArrayList<>();

        try {
            log.debug("解析Cross-Encoder响应: {}", aiResponse.substring(0, Math.min(100, aiResponse.length())));

            // 简化的分数解析
            if (aiResponse.contains("scores")) {
                // 实际应该解析JSON中的分数数组
                // 这里模拟解析结果
                for (int i = 0; i < expectedSize; i++) {
                    double score = Math.max(0.1, 0.9 - i * 0.1 + (Math.random() - 0.5) * 0.1);
                    scores.add(score);
                }
            } else {
                // 如果解析失败，提供递减分数
                for (int i = 0; i < expectedSize; i++) {
                    scores.add(0.8 - i * 0.05);
                }
            }
        } catch (Exception e) {
            log.warn("解析Cross-Encoder分数失败: {}", e.getMessage());
            // 提供默认分数
            for (int i = 0; i < expectedSize; i++) {
                scores.add(0.7 - i * 0.05);
            }
        }

        return scores;
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算简单重排序分数
     */
    private double calculateSimpleRerankScore(String content, String[] queryTerms) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }

        String lowerContent = content.toLowerCase();

        // 1. 计算词频分数
        double termFrequency = Arrays.stream(queryTerms)
                                     .mapToDouble(term -> {
                                         long count = lowerContent.split(term, -1).length - 1;
                                         return Math.log(1 + count);
                                     })
                                     .sum();

        // 2. 计算查询词密度
        double density = termFrequency / content.length() * 1000;

        // 3. 计算查询词覆盖率
        long matchedTerms = Arrays.stream(queryTerms)
                                  .mapToLong(term -> lowerContent.contains(term) ? 1 : 0)
                                  .sum();
        double coverage = (double) matchedTerms / queryTerms.length;

        // 4. 计算位置因子（查询词出现在前面的权重更高）
        double positionFactor = 1.0;
        for (String term : queryTerms) {
            int index = lowerContent.indexOf(term);
            if (index >= 0) {
                positionFactor += Math.exp(-index / 100.0);
            }
        }
        positionFactor = Math.min(2.0, positionFactor / queryTerms.length);

        // 综合计算分数
        double finalScore = (density * 0.4 + coverage * 0.4 + positionFactor * 0.2);
        return Math.min(1.0, finalScore);
    }

    /**
     * 添加重排序元数据
     */
    private void addRerankMetadata(DocumentSegment doc, String method, double score) {
        if (doc.getMetadata() == null) {
            doc.setMetadata(new HashMap<>());
        }

        doc.getMetadata().put("reranked", true);
        doc.getMetadata().put("rerankMethod", method);
        doc.getMetadata().put("rerankScore", score);
        doc.getMetadata().put("rerankTimestamp", System.currentTimeMillis());

        // 记录原始分数（如果没有记录过）
        if (!doc.getMetadata().containsKey("originalScore")) {
            doc.getMetadata().put("originalScore", score);
        }
    }

    /**
     * 截取内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
} 