package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;
import java.util.Map;

/**
 * 结构化检索服务接口
 *
 * @author matt
 */
public interface StructuredSearchService {

    /**
     * 执行结构化检索
     */
    List<DocumentSegment> search(String query, String knowledgeBaseId,
                                 Map<String, Object> filters,
                                 List<String> requiredFields,
                                 String sortBy, String sortOrder,
                                 Integer limit, Double threshold);

    /**
     * 应用过滤条件
     */
    List<DocumentSegment> applyFilters(List<DocumentSegment> documents, Map<String, Object> filters);

    /**
     * 排序文档
     */
    List<DocumentSegment> sortDocuments(List<DocumentSegment> documents, String sortBy, String sortOrder);

    /**
     * 验证必需字段
     */
    boolean validateRequiredFields(DocumentSegment document, List<String> requiredFields);
} 