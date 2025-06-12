package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.*;
import cn.mojoup.ai.rag.service.RagAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG辅助服务实现类
 *
 * @author matt
 */
@Slf4j
@Service
public class RagAssistantServiceImpl implements RagAssistantService {

    @Override
    public String detectQueryIntent(String question) {
        log.debug("检测查询意图: {}", question);

        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("什么") || lowerQuestion.contains("是什么") ||
            lowerQuestion.contains("定义") || lowerQuestion.contains("概念")) {
            return "definition";
        } else if (lowerQuestion.contains("如何") || lowerQuestion.contains("怎么") ||
                   lowerQuestion.contains("方法") || lowerQuestion.contains("步骤")) {
            return "how_to";
        } else if (lowerQuestion.contains("为什么") || lowerQuestion.contains("原因") ||
                   lowerQuestion.contains("解释")) {
            return "explanation";
        } else if (lowerQuestion.contains("比较") || lowerQuestion.contains("区别") ||
                   lowerQuestion.contains("差异") || lowerQuestion.contains("对比")) {
            return "comparison";
        } else if (lowerQuestion.contains("优缺点") || lowerQuestion.contains("优势") ||
                   lowerQuestion.contains("缺点") || lowerQuestion.contains("利弊")) {
            return "analysis";
        } else if (lowerQuestion.contains("总结") || lowerQuestion.contains("摘要") ||
                   lowerQuestion.contains("概括")) {
            return "summary";
        } else if (lowerQuestion.contains("例子") || lowerQuestion.contains("案例") ||
                   lowerQuestion.contains("实例")) {
            return "example";
        } else {
            return "general_qa";
        }
    }

    @Override
    public String selectOptimalStrategy(String question, String intent) {
        log.debug("选择最优策略: intent={}, question={}", intent, question);

        switch (intent) {
            case "comparison":
                return "hybrid"; // 比较类问题使用混合检索
            case "definition":
                return "semantic"; // 定义类问题使用语义检索
            case "how_to":
                return "structured"; // 方法类问题使用结构化检索
            case "summary":
                return "multi_knowledge_base"; // 摘要类问题跨多知识库
            case "analysis":
                return "hybrid"; // 分析类问题使用混合检索
            case "example":
                return "keyword"; // 案例类问题使用关键词检索
            default:
                return "semantic"; // 默认使用语义检索
        }
    }

    @Override
    public List<Citation> generateCitations(List<DocumentSegment> documents, String citationStyle) {
        log.debug("生成引用: citationStyle={}, docCount={}", citationStyle, documents.size());

        List<Citation> citations = new ArrayList<>();

        for (DocumentSegment doc : documents) {
            Citation citation = new Citation();
            citation.setCitationText(formatCitation(doc, citationStyle));
            citation.setSourceTitle(doc.getTitle());
            citation.setSourceUrl(doc.getSource());
            citation.setSourceSegment(doc);

            // 设置额外的引用信息
            citation.setAuthor(extractAuthor(doc));
            citation.setPublishDate(extractPublishDate(doc));
            citation.setPageNumber(extractPageNumber(doc));

            citations.add(citation);
        }

        return citations;
    }

    @Override
    public List<ConversationMessage> updateConversationHistory(List<ConversationMessage> history,
                                                               String question, String answer) {
        log.debug("更新对话历史: historySize={}", history != null ? history.size() : 0);

        List<ConversationMessage> updated = new ArrayList<>(history != null ? history : new ArrayList<>());

        // 添加用户消息
        ConversationMessage userMsg = new ConversationMessage();
        userMsg.setRole("user");
        userMsg.setContent(question);
        userMsg.setTimestamp(LocalDateTime.now().toString());
        updated.add(userMsg);

        // 添加助手消息
        ConversationMessage assistantMsg = new ConversationMessage();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(answer);
        assistantMsg.setTimestamp(LocalDateTime.now().toString());
        updated.add(assistantMsg);

        // 保持历史长度在合理范围内（最多20条消息）
        if (updated.size() > 20) {
            updated = updated.subList(updated.size() - 20, updated.size());
        }

        return updated;
    }

    @Override
    public Integer estimateTokens(String question, String answer) {
        if (question == null && answer == null) {
            return 0;
        }

        int totalLength = (question != null ? question.length() : 0) +
                          (answer != null ? answer.length() : 0);

        // 简单的token估算：中文约2.5字符/token，英文约4字符/token
        // 这里使用平均值3字符/token
        return totalLength / 3;
    }

    @Override
    public Double calculateAverageScore(List<DocumentSegment> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0.0;
        }

        return documents.stream()
                        .mapToDouble(DocumentSegment::getScore)
                        .average()
                        .orElse(0.0);
    }

    @Override
    public List<String> generateQuerySuggestions(String partialQuery, Integer limit) {
        log.debug("生成查询建议: partialQuery={}, limit={}", partialQuery, limit);

        List<String> suggestions = new ArrayList<>();

        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            // 如果没有输入，返回常见问题
            suggestions.add("什么是人工智能？");
            suggestions.add("机器学习的基本概念");
            suggestions.add("深度学习和机器学习的区别");
            suggestions.add("如何入门人工智能？");
            suggestions.add("AI技术的应用场景");
        } else {
            // 基于输入生成建议
            suggestions.add(partialQuery + "的定义是什么？");
            suggestions.add(partialQuery + "有哪些特点？");
            suggestions.add(partialQuery + "的应用场景有哪些？");
            suggestions.add("如何使用" + partialQuery + "？");
            suggestions.add(partialQuery + "的优缺点是什么？");
            suggestions.add(partialQuery + "的发展历史");
            suggestions.add(partialQuery + "和相关技术的比较");
            suggestions.add(partialQuery + "的最佳实践");
        }

        return suggestions.stream()
                          .limit(limit)
                          .collect(Collectors.toList());
    }

    @Override
    public List<String> generateRelatedQueries(String currentQuery, Integer limit) {
        log.debug("生成相关查询: currentQuery={}, limit={}", currentQuery, limit);

        List<String> related = new ArrayList<>();

        // 基于当前查询生成相关问题
        related.add("与" + currentQuery + "相关的概念");
        related.add(currentQuery + "的发展历史");
        related.add(currentQuery + "的未来趋势");
        related.add(currentQuery + "的实际案例");
        related.add(currentQuery + "的最佳实践");
        related.add(currentQuery + "的常见问题");
        related.add(currentQuery + "的技术挑战");
        related.add(currentQuery + "的行业应用");

        return related.stream()
                      .limit(limit)
                      .collect(Collectors.toList());
    }

    @Override
    public List<QueryHistoryItem> fetchQueryHistory(QueryHistoryRequest request) {
        log.debug("获取查询历史: userId={}, limit={}", request.getUserId(), request.getLimit());

        // TODO: 从数据库获取真实的查询历史
        List<QueryHistoryItem> history = new ArrayList<>();

        for (int i = 0; i < Math.min(request.getLimit(), 5); i++) {
            QueryHistoryItem item = new QueryHistoryItem();
            item.setQueryId(UUID.randomUUID().toString());
            item.setQuestion("历史查询问题 " + (i + 1));
            item.setAnswer("历史查询回答 " + (i + 1));
            item.setQueryTime(LocalDateTime.now().minusHours(i).toString());
            item.setQueryType("simple");
            item.setSuccess(true);
            history.add(item);
        }

        return history;
    }

    @Override
    public BatchQueryResponse createBatchResponse(List<BatchQueryItem> results) {
        log.debug("创建批量查询响应: resultCount={}", results.size());

        BatchQueryResponse response = new BatchQueryResponse();
        response.setBatchId(UUID.randomUUID().toString());
        response.setResults(results);
        response.setTotalQuestions(results.size());

        int successCount = 0;
        long totalProcessingTime = 0;

        for (BatchQueryItem item : results) {
            if (item.getSuccess()) {
                successCount++;
            }
            totalProcessingTime += item.getProcessingTime();
        }

        response.setSuccessCount(successCount);
        response.setFailureCount(results.size() - successCount);
        response.setTotalProcessingTime(totalProcessingTime);

        return response;
    }

    @Override
    public BatchQueryItem processSingleBatchQuery(String question, BatchQueryRequest request,
                                                  List<DocumentSegment> documents, String answer) {
        log.debug("处理单个批量查询项: question={}", question);

        BatchQueryItem item = new BatchQueryItem();
        item.setQuestion(question);
        item.setAnswer(answer);
        item.setDocuments(documents);
        item.setSuccess(true);
        item.setProcessingTime(System.currentTimeMillis() % 1000); // 模拟处理时间

        return item;
    }

    // ==================== 私有辅助方法 ====================

    private String formatCitation(DocumentSegment doc, String style) {
        switch (style.toLowerCase()) {
            case "apa":
                return String.format("%s. %s. Retrieved from %s",
                                     doc.getTitle(),
                                     extractPublishDate(doc),
                                     doc.getSource());
            case "mla":
                return String.format("\"%s.\" %s, %s.",
                                     doc.getTitle(),
                                     doc.getSource(),
                                     extractPublishDate(doc));
            case "chicago":
                return String.format("%s. \"%s.\" Accessed %s. %s.",
                                     extractAuthor(doc),
                                     doc.getTitle(),
                                     extractPublishDate(doc),
                                     doc.getSource());
            default:
                return String.format("%s - %s", doc.getTitle(), doc.getSource());
        }
    }

    private String extractAuthor(DocumentSegment doc) {
        // TODO: 从文档元数据中提取作者信息
        if (doc.getMetadata() != null && doc.getMetadata().containsKey("author")) {
            return doc.getMetadata().get("author").toString();
        }
        return "Unknown Author";
    }

    private String extractPublishDate(DocumentSegment doc) {
        // TODO: 从文档元数据中提取发布日期
        if (doc.getMetadata() != null && doc.getMetadata().containsKey("publishDate")) {
            return doc.getMetadata().get("publishDate").toString();
        }
        return doc.getCreatedAt() != null ? doc.getCreatedAt() : "Unknown Date";
    }

    private String extractPageNumber(DocumentSegment doc) {
        // TODO: 从文档位置信息计算页码
        if (doc.getPosition() != null) {
            return String.valueOf(doc.getPosition() / 1000 + 1); // 假设每页1000字符
        }
        return "Unknown Page";
    }
} 