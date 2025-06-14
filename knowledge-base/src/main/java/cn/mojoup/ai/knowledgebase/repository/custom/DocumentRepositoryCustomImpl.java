package cn.mojoup.ai.knowledgebase.repository.custom;

import cn.mojoup.ai.knowledgebase.domain.DocumentSearchRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档自定义Repository实现
 * 使用JPA Criteria实现动态查询
 * 
 * @author matt
 */
@RequiredArgsConstructor
public class DocumentRepositoryCustomImpl implements DocumentRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<KnowledgeDocument> searchDocuments(DocumentSearchRequest searchRequest, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 查询数据
        CriteriaQuery<KnowledgeDocument> query = cb.createQuery(KnowledgeDocument.class);
        Root<KnowledgeDocument> root = query.from(KnowledgeDocument.class);
        
        // 构建查询条件
        List<Predicate> predicates = buildPredicates(cb, root, searchRequest);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // 添加排序
        addOrderBy(cb, query, root, searchRequest);
        
        // 执行查询
        TypedQuery<KnowledgeDocument> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<KnowledgeDocument> results = typedQuery.getResultList();
        
        // 查询总数
        long total = countDocuments(searchRequest);
        
        return new PageImpl<>(results, pageable, total);
    }

    /**
     * 构建查询条件
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<KnowledgeDocument> root, 
                                          DocumentSearchRequest searchRequest) {
        List<Predicate> predicates = new ArrayList<>();

        // 关键词搜索（标题、内容、摘要）
        if (StringUtils.hasText(searchRequest.getKeyword())) {
            String keyword = "%" + searchRequest.getKeyword().trim() + "%";
            Predicate titlePredicate = cb.like(root.get("title"), keyword);
            Predicate contentPredicate = cb.like(root.get("content"), keyword);
            Predicate summaryPredicate = cb.like(root.get("summary"), keyword);
            predicates.add(cb.or(titlePredicate, contentPredicate, summaryPredicate));
        }

        // 知识库ID
        if (searchRequest.getKbId() != null) {
            predicates.add(cb.equal(root.get("kbId"), searchRequest.getKbId()));
        }

        // 分类ID
        if (searchRequest.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("categoryId"), searchRequest.getCategoryId()));
        }

        // 分类ID列表
        if (!CollectionUtils.isEmpty(searchRequest.getCategoryIds())) {
            predicates.add(root.get("categoryId").in(searchRequest.getCategoryIds()));
        }

        // 文档类型
        if (!CollectionUtils.isEmpty(searchRequest.getDocTypes())) {
            predicates.add(root.get("docType").in(searchRequest.getDocTypes()));
        }

        // 文档状态
        if (!CollectionUtils.isEmpty(searchRequest.getStatuses())) {
            predicates.add(root.get("status").in(searchRequest.getStatuses()));
        }

        // 访问级别
        if (!CollectionUtils.isEmpty(searchRequest.getAccessLevels())) {
            predicates.add(root.get("accessLevel").in(searchRequest.getAccessLevels()));
        }

        // 创建者ID
        if (StringUtils.hasText(searchRequest.getCreatedBy())) {
            predicates.add(cb.equal(root.get("createdBy"), searchRequest.getCreatedBy()));
        }

        // 标签搜索
        if (StringUtils.hasText(searchRequest.getTag())) {
            String tag = "%" + searchRequest.getTag().trim() + "%";
            predicates.add(cb.like(root.get("tags"), tag));
        }

        // 标签列表
        if (!CollectionUtils.isEmpty(searchRequest.getTags())) {
            List<Predicate> tagPredicates = new ArrayList<>();
            for (String tag : searchRequest.getTags()) {
                tagPredicates.add(cb.like(root.get("tags"), "%" + tag + "%"));
            }
            predicates.add(cb.or(tagPredicates.toArray(new Predicate[0])));
        }

        // 是否启用
        if (searchRequest.getEnabled() != null) {
            predicates.add(cb.equal(root.get("enabled"), searchRequest.getEnabled()));
        }

        // 创建时间范围
        if (searchRequest.getCreateTimeStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), searchRequest.getCreateTimeStart()));
        }
        if (searchRequest.getCreateTimeEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), searchRequest.getCreateTimeEnd()));
        }

        // 更新时间范围
        if (searchRequest.getUpdateTimeStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("updateTime"), searchRequest.getUpdateTimeStart()));
        }
        if (searchRequest.getUpdateTimeEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("updateTime"), searchRequest.getUpdateTimeEnd()));
        }

        // 文件大小范围
        if (searchRequest.getMinFileSize() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fileSize"), searchRequest.getMinFileSize()));
        }
        if (searchRequest.getMaxFileSize() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fileSize"), searchRequest.getMaxFileSize()));
        }

        // 字符数范围
        if (searchRequest.getMinCharCount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("charCount"), searchRequest.getMinCharCount()));
        }
        if (searchRequest.getMaxCharCount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("charCount"), searchRequest.getMaxCharCount()));
        }

        // 是否包含嵌入数据 - 简化实现，使用字段判断
        if (searchRequest.getHasEmbedding() != null) {
            if (searchRequest.getHasEmbedding()) {
                // 如果需要包含嵌入数据
                predicates.add(cb.isNotNull(root.get("embeddingId")));
            } else {
                // 如果不需要嵌入数据
                predicates.add(cb.isNull(root.get("embeddingId")));
            }
        }

        // 嵌入模型
        if (StringUtils.hasText(searchRequest.getEmbeddingModel())) {
            predicates.add(cb.equal(root.get("embeddingModel"), searchRequest.getEmbeddingModel()));
        }

        // 嵌入状态
        if (searchRequest.getEmbeddingStatus() != null) {
            predicates.add(cb.equal(root.get("embeddingStatus"), searchRequest.getEmbeddingStatus()));
        }

        return predicates;
    }

    /**
     * 添加排序
     */
    private void addOrderBy(CriteriaBuilder cb, CriteriaQuery<KnowledgeDocument> query, Root<KnowledgeDocument> root,
                           DocumentSearchRequest searchRequest) {
        String sortBy = searchRequest.getSortBy();
        if (!StringUtils.hasText(sortBy)) {
            sortBy = "createTime"; // 默认按创建时间排序
        }

        boolean isAsc = searchRequest.getSortDirection() == null || 
                       searchRequest.getSortDirection() == DocumentSearchRequest.SortDirection.ASC;

        Order order = isAsc ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy));
        query.orderBy(order);
    }

    /**
     * 统计符合条件的记录数
     */
    private long countDocuments(DocumentSearchRequest searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<KnowledgeDocument> root = countQuery.from(KnowledgeDocument.class);
        
        countQuery.select(cb.count(root));
        
        List<Predicate> predicates = buildPredicates(cb, root, searchRequest);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
} 