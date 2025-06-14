package cn.mojoup.ai.knowledgebase.repository.custom;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeBase;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeBaseSearchRequest;
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
 * 知识库自定义Repository实现
 * 使用JPA Criteria实现动态查询
 * 
 * @author matt
 */
@RequiredArgsConstructor
public class KnowledgeBaseRepositoryCustomImpl implements KnowledgeBaseRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public Page<KnowledgeBase> searchKnowledgeBases(KnowledgeBaseSearchRequest searchRequest, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 查询数据
        CriteriaQuery<KnowledgeBase> query = cb.createQuery(KnowledgeBase.class);
        Root<KnowledgeBase> root = query.from(KnowledgeBase.class);
        
        // 构建查询条件
        List<Predicate> predicates = buildPredicates(cb, root, searchRequest);
        
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // 添加排序
        addOrderBy(cb, query, root, searchRequest);
        
        // 执行查询
        TypedQuery<KnowledgeBase> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<KnowledgeBase> results = typedQuery.getResultList();
        
        // 查询总数
        long total = countKnowledgeBases(searchRequest);
        
        return new PageImpl<>(results, pageable, total);
    }

    /**
     * 构建查询条件
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<KnowledgeBase> root, 
                                          KnowledgeBaseSearchRequest searchRequest) {
        List<Predicate> predicates = new ArrayList<>();

        // 关键词搜索（名称或描述）
        if (StringUtils.hasText(searchRequest.getKeyword())) {
            String keyword = "%" + searchRequest.getKeyword().trim() + "%";
            Predicate namePredicate = cb.like(root.get("kbName"), keyword);
            Predicate descPredicate = cb.like(root.get("description"), keyword);
            predicates.add(cb.or(namePredicate, descPredicate));
        }

        // 所有者ID
        if (StringUtils.hasText(searchRequest.getOwnerId())) {
            predicates.add(cb.equal(root.get("ownerId"), searchRequest.getOwnerId()));
        }

        // 知识库类型
        if (!CollectionUtils.isEmpty(searchRequest.getKbTypes())) {
            predicates.add(root.get("kbType").in(searchRequest.getKbTypes()));
        }

        // 访问级别
        if (!CollectionUtils.isEmpty(searchRequest.getAccessLevels())) {
            predicates.add(root.get("accessLevel").in(searchRequest.getAccessLevels()));
        }

        // 状态
        if (!CollectionUtils.isEmpty(searchRequest.getStatuses())) {
            predicates.add(root.get("status").in(searchRequest.getStatuses()));
        }

        // 是否公开
        if (searchRequest.getIsPublic() != null) {
            predicates.add(cb.equal(root.get("isPublic"), searchRequest.getIsPublic()));
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

        // 文档数量范围
        if (searchRequest.getMinDocumentCount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("documentCount"), searchRequest.getMinDocumentCount()));
        }
        if (searchRequest.getMaxDocumentCount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("documentCount"), searchRequest.getMaxDocumentCount()));
        }

        // 存储大小范围
        if (searchRequest.getMinTotalSize() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalSize"), searchRequest.getMinTotalSize()));
        }
        if (searchRequest.getMaxTotalSize() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalSize"), searchRequest.getMaxTotalSize()));
        }

        return predicates;
    }

    /**
     * 添加排序
     */
    private void addOrderBy(CriteriaBuilder cb, CriteriaQuery<KnowledgeBase> query, Root<KnowledgeBase> root,
                           KnowledgeBaseSearchRequest searchRequest) {
        String sortBy = searchRequest.getSortBy();
        if (!StringUtils.hasText(sortBy)) {
            sortBy = "createTime"; // 默认按创建时间排序
        }

        boolean isAsc = searchRequest.getSortDirection() == null || 
                       searchRequest.getSortDirection() == KnowledgeBaseSearchRequest.SortDirection.ASC;

        Order order = isAsc ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy));
        query.orderBy(order);
    }

    /**
     * 统计符合条件的记录数
     */
    private long countKnowledgeBases(KnowledgeBaseSearchRequest searchRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<KnowledgeBase> root = countQuery.from(KnowledgeBase.class);
        
        countQuery.select(cb.count(root));
        
        List<Predicate> predicates = buildPredicates(cb, root, searchRequest);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
} 