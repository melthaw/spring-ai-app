package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.SummaryGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 摘要生成服务实现类
 *
 * @author matt
 */
@Slf4j
@Service
public class SummaryGenerationServiceImpl implements SummaryGenerationService {

    @Autowired
    private ChatClient chatClient;

    @Override
    public String generateSummary(String question, List<DocumentSegment> documents,
                                  String summaryType, Integer summaryLength, Double temperature) {
        log.debug("生成摘要: summaryType={}, summaryLength={}", summaryType, summaryLength);

        // 合并所有文档内容
        StringBuilder allContent = new StringBuilder();
        for (DocumentSegment doc : documents) {
            allContent.append(doc.getContent()).append(" ");
        }

        String summary;
        switch (summaryType.toLowerCase()) {
            case "extractive":
                // 抽取式摘要：选择最重要的句子
                summary = generateExtractiveSummary(allContent.toString(), summaryLength);
                break;
            case "abstractive":
                // 生成式摘要：重新生成内容
                summary = generateAbstractiveSummary(question, allContent.toString(), summaryLength, temperature);
                break;
            case "hybrid":
                // 混合式摘要：结合抽取和生成
                summary = generateHybridSummary(question, allContent.toString(), summaryLength, temperature);
                break;
            default:
                summary = allContent.toString().substring(0, Math.min(summaryLength, allContent.length()));
        }

        return summary;
    }

    @Override
    public String generateExtractiveSummary(String content, Integer summaryLength) {
        log.debug("生成抽取式摘要: contentLength={}, summaryLength={}", content.length(), summaryLength);

        // TODO: 实现更智能的抽取式摘要算法
        // 1. 句子分割
        // 2. 句子重要性评分（TF-IDF, TextRank等）
        // 3. 选择最重要的句子

        // 简单实现：按句子长度和位置选择
        String[] sentences = content.split("[。！？]");
        StringBuilder summary = new StringBuilder();

        // 优先选择较长的句子（通常包含更多信息）
        Arrays.sort(sentences, (a, b) -> Integer.compare(b.length(), a.length()));

        for (String sentence : sentences) {
            if (summary.length() + sentence.length() < summaryLength) {
                summary.append(sentence.trim()).append("。");
            } else {
                break;
            }
        }

        return summary.toString();
    }

    @Override
    public String generateAbstractiveSummary(String question, String content,
                                             Integer summaryLength, Double temperature) {
        log.debug("生成生成式摘要: question={}, contentLength={}, summaryLength={}",
                  question, content.length(), summaryLength);

        // TODO: 使用Spring AI的ChatClient生成生成式摘要
        // 构建摘要提示词
        String prompt = buildSummaryPrompt(question, content, summaryLength);

        // 模拟生成式摘要
        return String.format("关于\"%s\"的摘要：基于提供的内容，这是一个长度约为%d字符的生成式摘要，重新组织和表达了原始内容的核心观点。",
                             question, summaryLength);
    }

    @Override
    public Double calculateSummaryQuality(String summary, String originalContent) {
        log.debug("计算摘要质量: summaryLength={}, originalLength={}",
                  summary.length(), originalContent.length());

        // TODO: 实现更全面的摘要质量评估
        // 可以考虑：
        // 1. 内容覆盖度
        // 2. 语言流畅度
        // 3. 信息压缩比
        // 4. 关键信息保留度

        // 简单的质量评分
        double compressionRatio = (double) summary.length() / originalContent.length();
        double idealRatio = 0.3; // 理想压缩比为30%

        // 计算与理想压缩比的偏差
        double ratioScore = 1.0 - Math.abs(compressionRatio - idealRatio) / idealRatio;

        // 检查关键词保留情况
        double keywordRetention = calculateKeywordRetention(summary, originalContent);

        return (ratioScore + keywordRetention) / 2.0;
    }

    // ==================== 私有辅助方法 ====================

    private String generateHybridSummary(String question, String content,
                                         Integer summaryLength, Double temperature) {
        log.debug("生成混合式摘要: question={}, summaryLength={}", question, summaryLength);

        // 先生成抽取式摘要作为基础
        String extractive = generateExtractiveSummary(content, summaryLength / 2);

        // 再基于抽取式摘要生成生成式摘要
        String abstractive = generateAbstractiveSummary(question, extractive, summaryLength / 2, temperature);

        return extractive + " " + abstractive;
    }

    private String buildSummaryPrompt(String question, String content, Integer summaryLength) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请对以下内容生成摘要：\n\n");
        prompt.append("原始内容：\n").append(content).append("\n\n");
        prompt.append("问题上下文：").append(question).append("\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 摘要长度约为").append(summaryLength).append("字符\n");
        prompt.append("2. 保留核心信息和关键观点\n");
        prompt.append("3. 语言简洁明了\n");
        prompt.append("4. 结构清晰\n\n");
        prompt.append("摘要：");

        return prompt.toString();
    }

    private double calculateKeywordRetention(String summary, String originalContent) {
        // 简单的关键词保留度计算
        String[] originalWords = originalContent.toLowerCase().split("\\s+");
        String[] summaryWords = summary.toLowerCase().split("\\s+");

        // 统计重要词汇的保留情况
        long retainedKeywords = Arrays.stream(originalWords)
                                      .filter(word -> word.length() > 3) // 只考虑较长的词
                                      .filter(word -> Arrays.asList(summaryWords).contains(word))
                                      .count();

        long totalKeywords = Arrays.stream(originalWords)
                                   .filter(word -> word.length() > 3)
                                   .count();

        return totalKeywords > 0 ? (double) retainedKeywords / totalKeywords : 0.0;
    }
} 