package cn.mojoup.ai.knowledgebase.service.impl;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeCategory;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeCategoryRepository;
import cn.mojoup.ai.knowledgebase.repository.KnowledgeDocumentRepository;
import cn.mojoup.ai.knowledgebase.service.KnowledgeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 知识分类管理服务实现
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeCategoryServiceImpl implements KnowledgeCategoryService {

    private final KnowledgeCategoryRepository categoryRepository;
    private final KnowledgeDocumentRepository documentRepository;

    @Override
    @Transactional
    public KnowledgeCategory createCategory(CreateCategoryRequest request) {
        log.info("创建知识分类: name={}, kbId={}, parentId={}", 
                request.categoryName(), request.knowledgeBaseId(), request.parentId());

        // 检查分类名称是否可用
        if (!isCategoryNameAvailable(request.knowledgeBaseId(), request.parentId(), request.categoryName())) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        // 构建分类路径
        String categoryPath = buildCategoryPath(request.knowledgeBaseId(), request.parentId(), request.categoryName());

        // 计算深度
        int depth = calculateDepth(request.parentId());

        KnowledgeCategory category = KnowledgeCategory.builder()
                .categoryName(request.categoryName())
                .description(request.description())
                .iconUrl(request.iconUrl())
                .knowledgeBaseId(request.knowledgeBaseId())
                .parentId(request.parentId())
                .categoryPath(categoryPath)
                .depth(depth)
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .isDeleted(false)
                .documentCount(0)
                .createdBy(request.createdBy())
                .updatedBy(request.createdBy())
                .build();

        category = categoryRepository.save(category);
        log.info("知识分类创建成功: id={}, path={}", category.getId(), category.getCategoryPath());

        return category;
    }

    @Override
    @Transactional
    public KnowledgeCategory updateCategory(Long id, CreateCategoryRequest request) {
        log.info("更新知识分类: id={}, name={}", id, request.categoryName());

        KnowledgeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));

        // 检查名称变更时的可用性
        if (!category.getCategoryName().equals(request.categoryName())) {
            if (!isCategoryNameAvailable(request.knowledgeBaseId(), category.getParentId(), request.categoryName())) {
                throw new IllegalArgumentException("分类名称已存在");
            }
        }

        // 更新基本信息
        category.setCategoryName(request.categoryName());
        category.setDescription(request.description());
        category.setIconUrl(request.iconUrl());
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : category.getSortOrder());
        category.setUpdatedBy(request.createdBy());
        category.setUpdateTime(LocalDateTime.now());

        // 如果名称变更，需要更新路径
        if (!category.getCategoryName().equals(request.categoryName())) {
            updateCategoryPathRecursively(category);
        }

        category = categoryRepository.save(category);
        log.info("知识分类更新成功: id={}", id);

        return category;
    }

    @Override
    public Optional<KnowledgeCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, String operatorId) {
        log.info("删除知识分类: id={}, operatorId={}", id, operatorId);

        KnowledgeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));

        // 检查是否有子分类
        List<KnowledgeCategory> children = categoryRepository.findByParentIdAndIsDeletedFalse(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("分类下有子分类，无法删除");
        }

        // 检查是否有文档
        Long documentCount = documentRepository.countByCategoryId(id);
        if (documentCount > 0) {
            throw new IllegalArgumentException("分类下有文档，无法删除");
        }

        category.setIsDeleted(true);
        category.setUpdatedBy(operatorId);
        category.setUpdateTime(LocalDateTime.now());

        categoryRepository.save(category);
        log.info("知识分类删除成功: id={}", id);
    }

    @Override
    @Transactional
    public void restoreCategory(Long id, String operatorId) {
        log.info("恢复知识分类: id={}, operatorId={}", id, operatorId);

        KnowledgeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));

        category.setIsDeleted(false);
        category.setUpdatedBy(operatorId);
        category.setUpdateTime(LocalDateTime.now());

        categoryRepository.save(category);
        log.info("知识分类恢复成功: id={}", id);
    }

    @Override
    @Transactional
    public void moveCategory(Long categoryId, Long newParentId, String operatorId) {
        log.info("移动知识分类: categoryId={}, newParentId={}, operatorId={}", categoryId, newParentId, operatorId);

        KnowledgeCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + categoryId));

        // 检查目标父分类是否存在
        if (newParentId != null) {
            categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("目标父分类不存在: " + newParentId));
        }

        // 检查是否会形成循环引用
        if (newParentId != null && isCircularReference(categoryId, newParentId)) {
            throw new IllegalArgumentException("不能移动到子分类下");
        }

        // 检查新位置下名称是否可用
        if (!isCategoryNameAvailable(category.getKnowledgeBaseId(), newParentId, category.getCategoryName())) {
            throw new IllegalArgumentException("目标位置下分类名称已存在");
        }

        // 更新分类信息
        category.setParentId(newParentId);
        category.setDepth(calculateDepth(newParentId));
        category.setUpdatedBy(operatorId);
        category.setUpdateTime(LocalDateTime.now());

        // 更新路径
        updateCategoryPathRecursively(category);

        categoryRepository.save(category);
        log.info("知识分类移动成功: categoryId={}", categoryId);
    }

    @Override
    public List<KnowledgeCategory> getCategoryTree(Long kbId) {
        List<KnowledgeCategory> allCategories = categoryRepository.findByKnowledgeBaseIdAndIsDeletedFalseOrderBySortOrderAscCreateTimeAsc(kbId);
        return buildTree(allCategories, null);
    }

    @Override
    public List<KnowledgeCategory> getTopLevelCategories(Long kbId) {
        return categoryRepository.findByKnowledgeBaseIdAndParentIdIsNullAndIsDeletedFalseOrderBySortOrderAscCreateTimeAsc(kbId);
    }

    @Override
    public List<KnowledgeCategory> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsDeletedFalseOrderBySortOrderAscCreateTimeAsc(parentId);
    }

    @Override
    public String getCategoryPath(Long categoryId) {
        Optional<KnowledgeCategory> category = categoryRepository.findById(categoryId);
        return category.map(KnowledgeCategory::getCategoryPath).orElse("");
    }

    @Override
    public List<KnowledgeCategory> searchCategories(Long kbId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return categoryRepository.findByKnowledgeBaseIdAndCategoryNameContainingAndIsDeletedFalse(kbId, keyword);
    }

    @Override
    public boolean isCategoryNameAvailable(Long kbId, Long parentId, String categoryName) {
        return !categoryRepository.existsByKnowledgeBaseIdAndParentIdAndCategoryNameAndIsDeletedFalse(
                kbId, parentId, categoryName);
    }

    /**
     * 构建分类路径
     */
    private String buildCategoryPath(Long kbId, Long parentId, String categoryName) {
        if (parentId == null) {
            return categoryName;
        }

        Optional<KnowledgeCategory> parent = categoryRepository.findById(parentId);
        if (parent.isPresent()) {
            return parent.get().getCategoryPath() + "/" + categoryName;
        }

        return categoryName;
    }

    /**
     * 计算分类深度
     */
    private int calculateDepth(Long parentId) {
        if (parentId == null) {
            return 0;
        }

        Optional<KnowledgeCategory> parent = categoryRepository.findById(parentId);
        return parent.map(category -> category.getDepth() + 1).orElse(0);
    }

    /**
     * 递归更新分类路径
     */
    private void updateCategoryPathRecursively(KnowledgeCategory category) {
        String newPath = buildCategoryPath(category.getKnowledgeBaseId(), category.getParentId(), category.getCategoryName());
        category.setCategoryPath(newPath);

        // 更新所有子分类
        List<KnowledgeCategory> children = categoryRepository.findByParentIdAndIsDeletedFalse(category.getId());
        for (KnowledgeCategory child : children) {
            updateCategoryPathRecursively(child);
            categoryRepository.save(child);
        }
    }

    /**
     * 检查是否会形成循环引用
     */
    private boolean isCircularReference(Long categoryId, Long newParentId) {
        if (newParentId == null) {
            return false;
        }

        Long currentParentId = newParentId;
        while (currentParentId != null) {
            if (currentParentId.equals(categoryId)) {
                return true;
            }

            Optional<KnowledgeCategory> parent = categoryRepository.findById(currentParentId);
            currentParentId = parent.map(KnowledgeCategory::getParentId).orElse(null);
        }

        return false;
    }

    /**
     * 构建树结构
     */
    private List<KnowledgeCategory> buildTree(List<KnowledgeCategory> allCategories, Long parentId) {
        List<KnowledgeCategory> result = new ArrayList<>();

        for (KnowledgeCategory category : allCategories) {
            if ((parentId == null && category.getParentId() == null) ||
                (parentId != null && parentId.equals(category.getParentId()))) {
                
                List<KnowledgeCategory> children = buildTree(allCategories, category.getId());
                // 注意：这里假设KnowledgeCategory有设置子分类的方法，如果没有可以不设置
                result.add(category);
            }
        }

        return result;
    }
} 