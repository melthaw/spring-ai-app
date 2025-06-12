package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 摘要生成服务接口
 *
 * @author matt
 */
public interface SummaryGenerationService {

    /**
     * 生成摘要
     */
    String generateSummary(String question, List<DocumentSegment> documents,
                           String summaryType, Integer summaryLength, Double temperature);

    /**
     * 生成抽取式摘要
     */
    String generateExtractiveSummary(String content, Integer summaryLength);

    /**
     * 生成生成式摘要
     */
    String generateAbstractiveSummary(String question, String content,
                                      Integer summaryLength, Double temperature);

    /**
     * 计算摘要质量分数
     */
    Double calculateSummaryQuality(String summary, String originalContent);
} 