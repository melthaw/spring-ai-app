package cn.mojoup.ai.rag.service.impl;


import cn.mojoup.ai.rag.domain.DocumentSegment;
import cn.mojoup.ai.rag.service.StructuredSearchService;
import cn.mojoup.ai.rag.service.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结构化检索服务实现类
 *
 * @author matt
 */
@Slf4j
@Service
public class StructuredSearchServiceImpl implements StructuredSearchService {

    @Autowired
    private VectorSearchService vectorSearchService;

    @Override
    public List<DocumentSegment> search(String query, String knowledgeBaseId,
                                        Map<String, Object> filters,
                                        List<String> requiredFields,
                                        String sortBy, String sortOrder,
                                        Integer limit, Double threshold) {
        log.debug("执行结构化检索: filters={}, sortBy={}", filters, sortBy);

        // 1. 执行基础向量检索
        List<DocumentSegment> results = vectorSearchService.search(query, knowledgeBaseId, limit, threshold);

        // 2. 应用过滤条件
        if (filters != null && !filters.isEmpty()) {
            results = applyFilters(results, filters);
        }

        // 3. 验证必需字段
        if (requiredFields != null && !requiredFields.isEmpty()) {
            results = results.stream()
                             .filter(doc -> validateRequiredFields(doc, requiredFields))
                             .collect(Collectors.toList());
        }

        // 4. 应用排序
        if (sortBy != null) {
            results = sortDocuments(results, sortBy, sortOrder);
        }

        return results.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<DocumentSegment> applyFilters(List<DocumentSegment> documents, Map<String, Object> filters) {
        log.debug("应用过滤条件: docCount={}, filterCount={}", documents.size(), filters.size());

        return documents.stream()
                        .filter(doc -> applyDocumentFilters(doc, filters))
                        .collect(Collectors.toList());
    }

    @Override
    public List<DocumentSegment> sortDocuments(List<DocumentSegment> documents, String sortBy, String sortOrder) {
        log.debug("排序文档: docCount={}, sortBy={}, sortOrder={}", documents.size(), sortBy, sortOrder);

        Comparator<DocumentSegment> comparator = getComparator(sortBy);

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return documents.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public boolean validateRequiredFields(DocumentSegment document, List<String> requiredFields) {
        log.debug("验证必需字段: docId={}, requiredFields={}", document.getDocumentId(), requiredFields);

        for (String field : requiredFields) {
            if (!hasField(document, field)) {
                return false;
            }
        }
        return true;
    }

    // ==================== 私有辅助方法 ====================

    private boolean applyDocumentFilters(DocumentSegment doc, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String field = filter.getKey();
            Object value = filter.getValue();

            if (!matchesFilter(doc, field, value)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesFilter(DocumentSegment doc, String field, Object value) {
        switch (field.toLowerCase()) {
            case "documenttype":
                return value.equals(doc.getDocumentType());
            case "minscore":
                return doc.getScore() >= (Double) value;
            case "maxscore":
                return doc.getScore() <= (Double) value;
            case "minlength":
                return doc.getLength() >= (Integer) value;
            case "maxlength":
                return doc.getLength() <= (Integer) value;
            case "author":
                return matchesMetadataField(doc, "author", value);
            case "category":
                return matchesMetadataField(doc, "category", value);
            case "publishdate":
                return matchesMetadataField(doc, "publishDate", value);
            case "tags":
                return matchesTags(doc, value);
            case "source":
                return doc.getSource() != null && doc.getSource().contains(value.toString());
            default:
                // 尝试匹配元数据字段
                return matchesMetadataField(doc, field, value);
        }
    }

    private boolean matchesMetadataField(DocumentSegment doc, String field, Object value) {
        if (doc.getMetadata() == null) {
            return false;
        }
        Object metadataValue = doc.getMetadata().get(field);
        return metadataValue != null && metadataValue.equals(value);
    }

    private boolean matchesTags(DocumentSegment doc, Object value) {
        if (doc.getTags() == null) {
            return false;
        }
        String tagToMatch = value.toString();
        for (String tag : doc.getTags()) {
            if (tag.equalsIgnoreCase(tagToMatch)) {
                return true;
            }
        }
        return false;
    }

    private Comparator<DocumentSegment> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "score":
                return Comparator.comparing(DocumentSegment::getScore);
            case "position":
                return Comparator.comparing(DocumentSegment::getPosition,
                                            Comparator.nullsLast(Comparator.naturalOrder()));
            case "length":
                return Comparator.comparing(DocumentSegment::getLength,
                                            Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdat":
                return Comparator.comparing(DocumentSegment::getCreatedAt,
                                            Comparator.nullsLast(Comparator.naturalOrder()));
            case "title":
                return Comparator.comparing(DocumentSegment::getTitle,
                                            Comparator.nullsLast(Comparator.naturalOrder()));
            case "documenttype":
                return Comparator.comparing(DocumentSegment::getDocumentType,
                                            Comparator.nullsLast(Comparator.naturalOrder()));
            default:
                return Comparator.comparing(DocumentSegment::getScore);
        }
    }

    private boolean hasField(DocumentSegment document, String field) {
        switch (field.toLowerCase()) {
            case "title":
                return document.getTitle() != null && !document.getTitle().isEmpty();
            case "content":
                return document.getContent() != null && !document.getContent().isEmpty();
            case "source":
                return document.getSource() != null && !document.getSource().isEmpty();
            case "author":
                return hasMetadataField(document, "author");
            case "publishdate":
                return hasMetadataField(document, "publishDate");
            case "tags":
                return document.getTags() != null && document.getTags().length > 0;
            case "score":
                return document.getScore() != null;
            case "position":
                return document.getPosition() != null;
            case "length":
                return document.getLength() != null;
            default:
                // 检查元数据字段
                return hasMetadataField(document, field);
        }
    }

    private boolean hasMetadataField(DocumentSegment document, String field) {
        return document.getMetadata() != null &&
               document.getMetadata().containsKey(field) &&
               document.getMetadata().get(field) != null;
    }
}
