package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.HybridSearchService;
import cn.mojoup.ai.rag.service.KeywordSearchService;
import cn.mojoup.ai.rag.service.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 混合检索服务实现类
 *
 * @author matt
 */
@Slf4j
@Service
public class HybridSearchServiceImpl implements HybridSearchService {

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private KeywordSearchService keywordSearchService;

    @Override
    public List<DocumentSegment> search(String query, String knowledgeBaseId,
                                        List<String> keywords, Double keywordWeight,
                                        Double semanticWeight, Boolean enableRerank,
                                        Integer limit, Double threshold) {
        log.debug("执行混合检索: keywordWeight={}, semanticWeight={}", keywordWeight, semanticWeight);

        // 1. 执行关键词检索
        List<DocumentSegment> keywordResults = keywordSearchService.search(
                query, keywords, knowledgeBaseId, limit);

        // 2. 执行语义检索
        List<DocumentSegment> semanticResults = vectorSearchService.semanticSearch(
                query, knowledgeBaseId, "text-embedding-ada-002", limit, threshold, false);

        // 3. 合并结果并计算混合分数
        List<DocumentSegment> hybridResults = mergeResults(
                keywordResults, semanticResults, keywordWeight, semanticWeight);

        // 4. 重排序
        if (enableRerank && hybridResults.size() > 1) {
            hybridResults = vectorSearchService.rerankDocuments(hybridResults, query);
        }

        return hybridResults.stream()
                            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                            .limit(limit)
                            .collect(Collectors.toList());
    }

    @Override
    public List<DocumentSegment> mergeResults(List<DocumentSegment> keywordResults,
                                              List<DocumentSegment> semanticResults,
                                              Double keywordWeight, Double semanticWeight) {
        log.debug("合并检索结果: keywordCount={}, semanticCount={}",
                  keywordResults.size(), semanticResults.size());

        Map<String, DocumentSegment> mergedResults = new HashMap<>();

        // 处理关键词结果
        for (DocumentSegment doc : keywordResults) {
            doc.setScore(doc.getScore() * keywordWeight);
            mergedResults.put(doc.getSegmentId(), doc);
        }

        // 处理语义结果
        for (DocumentSegment doc : semanticResults) {
            String id = doc.getSegmentId();
            if (mergedResults.containsKey(id)) {
                // 合并分数
                DocumentSegment existing = mergedResults.get(id);
                existing.setScore(existing.getScore() + doc.getScore() * semanticWeight);
            } else {
                doc.setScore(doc.getScore() * semanticWeight);
                mergedResults.put(id, doc);
            }
        }

        return new ArrayList<>(mergedResults.values());
    }

    @Override
    public void calculateHybridScores(List<DocumentSegment> documents,
                                      Double keywordWeight, Double semanticWeight) {
        log.debug("计算混合分数: docCount={}, keywordWeight={}, semanticWeight={}",
                  documents.size(), keywordWeight, semanticWeight);

        // TODO: 实现更复杂的混合分数计算算法
        // 可以考虑：
        // 1. 文档长度归一化
        // 2. 时间衰减因子
        // 3. 权威性评分
        // 4. 用户反馈学习

        documents.forEach(doc -> {
            // 当前只是简单的加权平均，实际可以更复杂
            double normalizedScore = Math.min(1.0, doc.getScore());
            doc.setScore(normalizedScore);
        });
    }
}

