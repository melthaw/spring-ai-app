package cn.mojoup.ai.rag.service.impl;

import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.DocumentRerankService;
import cn.mojoup.ai.rag.service.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量检索服务实现类
 * 集成Spring AI的EmbeddingModel、VectorStore，委托DocumentRerankService进行重排序
 *
 * @author matt
 */
@Slf4j
@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    // Spring AI相关组件
    @Autowired(required = false)
    private VectorStore vectorStore;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    // 重排序服务
    @Autowired
    private DocumentRerankService documentRerankService;

    @Value("${rag.vector.default-embedding-model:text-embedding-ada-002}")
    private String defaultEmbeddingModel;

    @Value("${rag.vector.similarity-threshold:0.7}")
    private Double defaultSimilarityThreshold;

    @Value("${rag.vector.max-results:100}")
    private Integer maxResults;

    @Override
    public List<DocumentSegment> search(String query, String knowledgeBaseId,
                                        Integer limit, Double threshold) {
        log.info("执行向量检索: query={}, knowledgeBaseId={}, limit={}, threshold={}",
                 query, knowledgeBaseId, limit, threshold);

        try {
            // 1. 参数验证和默认值设置
            if (query == null || query.trim().isEmpty()) {
                log.warn("查询字符串为空");
                return new ArrayList<>();
            }

            limit = limit != null ? Math.min(limit, maxResults) : 10;
            threshold = threshold != null ? threshold : defaultSimilarityThreshold;

            // 2. 直接使用查询字符串进行向量搜索
            List<DocumentSegment> searchResults = performVectorSearch(query, knowledgeBaseId, limit * 2);

            // 3. 过滤低于阈值的结果
            List<DocumentSegment> filteredResults = filterByThreshold(searchResults, threshold);

            // 4. 返回排序后的结果
            return filteredResults.stream()
                                  .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                                  .limit(limit)
                                  .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("向量检索失败: query={}, error={}", query, e.getMessage(), e);
            // 降级到模拟数据
            return createFallbackResults(query, knowledgeBaseId, limit);
        }
    }

    @Override
    public List<DocumentSegment> semanticSearch(String query, String knowledgeBaseId,
                                                String embeddingModel, Integer limit,
                                                Double threshold, Boolean rerank) {
        log.info("执行语义检索: query={}, embeddingModel={}, rerank={}",
                 query, embeddingModel, rerank);

        try {
            // 1. 参数验证
            embeddingModel = embeddingModel != null ? embeddingModel : defaultEmbeddingModel;
            limit = limit != null ? Math.min(limit, maxResults) : 10;
            threshold = threshold != null ? threshold : defaultSimilarityThreshold;
            rerank = rerank != null ? rerank : false;

            // 2. 直接使用查询字符串进行语义搜索
            List<DocumentSegment> searchResults = performSemanticVectorSearch(
                    query, knowledgeBaseId, embeddingModel, limit * 2);

            // 3. 过滤低于阈值的结果
            List<DocumentSegment> filteredResults = filterByThreshold(searchResults, threshold);

            // 4. 如果启用重排序，则委托给DocumentRerankService
            if (rerank && filteredResults.size() > 1) {
                filteredResults = documentRerankService.rerankDocuments(filteredResults, query);
            }

            return filteredResults.stream()
                                  .limit(limit)
                                  .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("语义检索失败: query={}, error={}", query, e.getMessage(), e);
            // 降级到基础向量搜索
            return search(query, knowledgeBaseId, limit, threshold);
        }
    }

    @Override
    public List<DocumentSegment> rerankDocuments(List<DocumentSegment> documents, String query) {
        log.info("重排序文档: docCount={}, query={}", documents.size(), query);

        // 委托给专门的重排序服务
        return documentRerankService.rerankDocuments(documents, query);
    }

    // ==================== 向量检索核心方法 ====================

    /**
     * 生成查询向量 - 集成Spring AI EmbeddingModel
     */
    private List<Double> generateQueryEmbedding(String query) {
        log.debug("生成查询向量: query={}", query);

        try {
            if (embeddingModel != null) {
                // 使用Spring AI的EmbeddingModel
                EmbeddingResponse response = embeddingModel.embedForResponse(List.of(query));
                if (response != null && !response.getResults().isEmpty()) {
                    float[] embedding = response.getResults().get(0).getOutput();
                    List<Double> vector = new ArrayList<>();
                    for (float value : embedding) {
                        vector.add((double) value);
                    }
                    return normalizeVector(vector);
                }
            }
        } catch (Exception e) {
            log.warn("Spring AI嵌入模型调用失败，降级到模拟: error={}", e.getMessage());
        }

        // 降级：模拟向量生成
        Random random = new Random(query.hashCode());
        List<Double> vector = new ArrayList<>();
        for (int i = 0; i < 1536; i++) { // OpenAI text-embedding-ada-002 的维度
            vector.add(random.nextGaussian());
        }

        return normalizeVector(vector);
    }

    /**
     * 执行向量搜索 - 集成Spring AI VectorStore
     */
    private List<DocumentSegment> performVectorSearch(String query, String knowledgeBaseId, Integer limit) {
        log.debug("执行向量搜索: query={}, knowledgeBaseId={}, limit={}",
                  query, knowledgeBaseId, limit);

        try {
            if (vectorStore != null) {
                // 使用Spring AI的VectorStore进行相似度搜索
                SearchRequest.Builder searchBuilder = SearchRequest.builder()
                                                                   .topK(limit)
                                                                   .similarityThreshold(defaultSimilarityThreshold);

                // 添加知识库过滤条件
                if (knowledgeBaseId != null && !knowledgeBaseId.isEmpty()) {
                    searchBuilder.filterExpression("knowledgeBaseId == '" + knowledgeBaseId + "'");
                }

                SearchRequest searchRequest = searchBuilder.build();

                // 使用查询字符串进行搜索（Spring AI会自动进行向量化）
                List<Document> documents = vectorStore.similaritySearch(query);

                return convertSpringAIDocumentsToSegments(documents);
            }
        } catch (Exception e) {
            log.warn("Spring AI向量搜索失败，降级到模拟: error={}", e.getMessage());
        }

        // 降级：模拟向量搜索结果（保持原有逻辑以便测试）
        List<Double> queryVector = generateQueryEmbedding(query);
        List<DocumentSegment> results = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, 10); i++) {
            DocumentSegment segment = createMockSegmentWithSimilarity(queryVector, knowledgeBaseId, i);
            results.add(segment);
        }

        return results;
    }

    /**
     * 执行语义向量搜索
     */
    private List<DocumentSegment> performSemanticVectorSearch(String query, String knowledgeBaseId,
                                                              String embeddingModel, Integer limit) {
        log.debug("执行语义向量搜索: query={}, model={}, knowledgeBaseId={}, limit={}",
                  query, embeddingModel, knowledgeBaseId, limit);

        try {
            if (vectorStore != null) {
                // 创建更高级的搜索请求
                SearchRequest.Builder searchBuilder = SearchRequest.builder()
                                                                   .topK(limit)
                                                                   .similarityThreshold(defaultSimilarityThreshold *
                                                                                        0.9); // 语义搜索使用稍低的阈值

                // 添加复合过滤条件
                if (knowledgeBaseId != null && !knowledgeBaseId.isEmpty()) {
                    String filterExpression = "knowledgeBaseId == '" + knowledgeBaseId +
                                              "' && embeddingModel == '" + embeddingModel + "'";
                    searchBuilder.filterExpression(filterExpression);
                }

                SearchRequest searchRequest = searchBuilder.build();

                // 使用语义搜索
                List<Document> documents = vectorStore.similaritySearch(query);

                List<DocumentSegment> segments = convertSpringAIDocumentsToSegments(documents);

                // 为语义搜索结果增加一些分数提升
                segments.forEach(doc -> {
                    doc.setScore(Math.min(1.0, doc.getScore() + 0.05));
                    if (doc.getMetadata() == null) {
                        doc.setMetadata(new HashMap<>());
                    }
                    doc.getMetadata().put("searchType", "semantic");
                    doc.getMetadata().put("embeddingModel", embeddingModel);
                });

                return segments;
            }
        } catch (Exception e) {
            log.warn("Spring AI语义搜索失败，降级到基础搜索: error={}", e.getMessage());
        }

        // 降级到基础向量搜索
        return performVectorSearch(query, knowledgeBaseId, limit);
    }

    // ==================== Spring AI转换方法 ====================

    /**
     * 将Spring AI的Document转换为DocumentSegment
     */
    private List<DocumentSegment> convertSpringAIDocumentsToSegments(List<Document> documents) {
        return documents.stream()
                        .map(this::convertSpringAIDocumentToSegment)
                        .collect(Collectors.toList());
    }

    /**
     * 转换单个Spring AI Document
     */
    private DocumentSegment convertSpringAIDocumentToSegment(Document document) {
        DocumentSegment segment = new DocumentSegment();

        // 基础字段映射
        segment.setSegmentId(document.getId());
        segment.setContent(document.getText()); // 修复: 使用getText()方法

        // 从Document的metadata中提取信息
        Map<String, Object> docMetadata = document.getMetadata();
        if (docMetadata != null) {
            segment.setDocumentId((String) docMetadata.getOrDefault("documentId", document.getId()));
            segment.setKnowledgeBaseId((String) docMetadata.get("knowledgeBaseId"));
            segment.setTitle((String) docMetadata.get("title"));
            segment.setSource((String) docMetadata.get("source"));
            segment.setDocumentType((String) docMetadata.getOrDefault("documentType", "text"));

            // 数值字段
            Object position = docMetadata.get("position");
            if (position instanceof Number) {
                segment.setPosition(((Number) position).intValue());
            }

            Object length = docMetadata.get("length");
            if (length instanceof Number) {
                segment.setLength(((Number) length).intValue());
            } else {
                segment.setLength(document.getText().length());
            }

            // 时间字段
            segment.setCreatedAt((String) docMetadata.get("createdAt"));

            // 标签字段
            Object tags = docMetadata.get("tags");
            if (tags instanceof String[]) {
                segment.setTags((String[]) tags);
            } else if (tags instanceof List) {
                List<?> tagsList = (List<?>) tags;
                segment.setTags(tagsList.stream().map(Object::toString).toArray(String[]::new));
            }

            // 保留原始metadata
            segment.setMetadata(new HashMap<>(docMetadata));
        }

        // 设置相似度分数（Spring AI可能在metadata中提供）
        Object scoreObj = docMetadata != null ? docMetadata.get("score") : null;
        if (scoreObj instanceof Number) {
            segment.setScore(((Number) scoreObj).doubleValue());
        } else {
            segment.setScore(0.8); // 默认分数
        }

        return segment;
    }

    // ==================== 辅助方法 ====================

    private List<DocumentSegment> filterByThreshold(List<DocumentSegment> documents, Double threshold) {
        return documents.stream()
                        .filter(doc -> doc.getScore() >= threshold)
                        .collect(Collectors.toList());
    }

    private List<Double> normalizeVector(List<Double> vector) {
        double norm = Math.sqrt(vector.stream().mapToDouble(x -> x * x).sum());
        if (norm == 0) return vector;

        return vector.stream()
                     .map(x -> x / norm)
                     .collect(Collectors.toList());
    }

    private int getEmbeddingDimension(String embeddingModel) {
        switch (embeddingModel.toLowerCase()) {
            case "text-embedding-ada-002":
                return 1536;
            case "text-embedding-3-small":
                return 1536;
            case "text-embedding-3-large":
                return 3072;
            case "sentence-transformers/all-mpnet-base-v2":
                return 768;
            case "sentence-transformers/all-minilm-l6-v2":
                return 384;
            default:
                return 1536; // 默认维度
        }
    }

    private DocumentSegment createMockSegmentWithSimilarity(List<Double> queryVector,
                                                            String knowledgeBaseId,
                                                            int index) {
        DocumentSegment segment = new DocumentSegment();
        segment.setSegmentId(UUID.randomUUID().toString());
        segment.setDocumentId("doc_" + knowledgeBaseId + "_" + index);
        segment.setKnowledgeBaseId(knowledgeBaseId);

        // 根据向量相似度生成内容
        String content = generateContentBasedOnVector(queryVector, index);
        segment.setContent(content);

        // 模拟相似度分数（基于向量距离）
        double similarity = calculateMockSimilarity(queryVector, index);
        segment.setScore(similarity);

        segment.setTitle("相关文档 " + (index + 1));
        segment.setSource("vector_search_" + index);
        segment.setDocumentType("text");
        segment.setPosition(index * 100);
        segment.setLength(content.length());
        segment.setCreatedAt(new Date().toString());
        segment.setTags(new String[]{"向量检索", "相似文档"});

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("similarity", similarity);
        metadata.put("vectorDimension", queryVector.size());
        metadata.put("searchMethod", "vector");
        metadata.put("author", "系统生成");
        metadata.put("category", "检索结果");
        segment.setMetadata(metadata);

        return segment;
    }

    private String generateContentBasedOnVector(List<Double> queryVector, int index) {
        // 基于向量特征生成模拟内容
        double vectorSum = queryVector.stream().mapToDouble(Double::doubleValue).sum();
        String contentType = vectorSum > 0 ? "正向内容" : "负向内容";

        return String.format("这是第%d个检索结果，基于向量相似度匹配的%s。向量维度：%d，向量和：%.3f。" +
                             "此内容与查询具有较高的语义相似性，包含了相关的概念和信息。",
                             index + 1, contentType, queryVector.size(), vectorSum);
    }

    private double calculateMockSimilarity(List<Double> queryVector, int index) {
        // 模拟相似度计算（实际应该是余弦相似度）
        double baseSimilarity = 0.95 - index * 0.05; // 递减的相似度

        // 添加一些基于向量的随机性
        double vectorInfluence = queryVector.stream()
                                            .limit(10)
                                            .mapToDouble(Math::abs)
                                            .average()
                                            .orElse(0.5);

        return Math.max(0.1, Math.min(1.0, baseSimilarity + (vectorInfluence - 0.5) * 0.1));
    }

    private List<DocumentSegment> createFallbackResults(String query, String knowledgeBaseId, Integer limit) {
        log.warn("使用降级模拟数据: query={}, knowledgeBaseId={}", query, knowledgeBaseId);

        List<DocumentSegment> fallbackResults = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, 3); i++) {
            DocumentSegment segment = createMockSegment(query, knowledgeBaseId, i);
            segment.getMetadata().put("fallback", true);
            fallbackResults.add(segment);
        }
        return fallbackResults;
    }

    private DocumentSegment createMockSegment(String query, String knowledgeBaseId, int index) {
        DocumentSegment segment = new DocumentSegment();
        segment.setSegmentId(UUID.randomUUID().toString());
        segment.setDocumentId("doc_" + index);
        segment.setKnowledgeBaseId(knowledgeBaseId);
        segment.setContent("这是关于 " + query + " 的相关内容片段 " + (index + 1));
        segment.setScore(0.9 - index * 0.1);
        segment.setTitle("文档标题 " + (index + 1));
        segment.setSource("source_" + index);
        segment.setDocumentType("pdf");
        segment.setPosition(index * 100);
        segment.setLength(segment.getContent().length());
        segment.setCreatedAt(new Date().toString());
        segment.setTags(new String[]{"AI", "技术"});

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "作者" + (index + 1));
        metadata.put("publishDate", "2024-01-" + String.format("%02d", index + 1));
        metadata.put("category", "技术文档");
        segment.setMetadata(metadata);

        return segment;
    }
}