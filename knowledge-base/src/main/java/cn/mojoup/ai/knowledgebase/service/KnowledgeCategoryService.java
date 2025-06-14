package cn.mojoup.ai.knowledgebase.service;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeCategory;

import java.util.List;
import java.util.Optional;

/**
 * 知识分类管理服务接口
 * 
 * @author matt
 */
public interface KnowledgeCategoryService {

    /**
     * 创建分类
     */
    KnowledgeCategory createCategory(CreateCategoryRequest request);

    /**
     * 更新分类信息
     */
    KnowledgeCategory updateCategory(Long id, CreateCategoryRequest request);

    /**
     * 根据ID获取分类详情
     */
    Optional<KnowledgeCategory> getCategoryById(Long id);

    /**
     * 删除分类（逻辑删除）
     */
    void deleteCategory(Long id, String operatorId);

    /**
     * 恢复分类
     */
    void restoreCategory(Long id, String operatorId);

    /**
     * 移动分类到新的父分类
     */
    void moveCategory(Long categoryId, Long newParentId, String operatorId);

    /**
     * 获取知识库的分类树结构
     */
    List<KnowledgeCategory> getCategoryTree(Long kbId);

    /**
     * 获取知识库的顶级分类
     */
    List<KnowledgeCategory> getTopLevelCategories(Long kbId);

    /**
     * 获取子分类
     */
    List<KnowledgeCategory> getChildCategories(Long parentId);

    /**
     * 获取分类的完整路径（从根到叶子节点）
     */
    String getCategoryPath(Long categoryId);

    /**
     * 搜索分类
     */
    List<KnowledgeCategory> searchCategories(Long kbId, String keyword);

    /**
     * 检查分类名称在同级下是否可用
     */
    boolean isCategoryNameAvailable(Long kbId, Long parentId, String categoryName);

    /**
     * 创建分类请求
     */
    record CreateCategoryRequest(
            String categoryName,
            String description,
            String iconUrl,
            Long knowledgeBaseId,
            Long parentId,
            String createdBy,
            Integer sortOrder
    ) {}
} 