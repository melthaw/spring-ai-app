package cn.mojoup.ai.knowledgebase.repository;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库分类Repository
 * 
 * @author matt
 */
@Repository
public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, Long> {

    /**
     * 根据知识库ID查找所有分类
     */
    @Query("SELECT c FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND c.enabled = true ORDER BY c.sortOrder ASC, c.createTime ASC")
    List<KnowledgeCategory> findByKnowledgeBaseId(@Param("kbId") Long kbId);

    /**
     * 根据知识库ID和父分类ID查找子分类
     */
    @Query("SELECT c FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND c.parentId = :parentId AND c.enabled = true ORDER BY c.sortOrder ASC, c.createTime ASC")
    List<KnowledgeCategory> findByKnowledgeBaseIdAndParentId(@Param("kbId") Long kbId, @Param("parentId") Long parentId);

    /**
     * 查找根分类（父分类ID为空或0）
     */
    @Query("SELECT c FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND (c.parentId IS NULL OR c.parentId = 0) AND c.enabled = true ORDER BY c.sortOrder ASC, c.createTime ASC")
    List<KnowledgeCategory> findRootCategoriesByKnowledgeBaseId(@Param("kbId") Long kbId);

    /**
     * 根据分类路径查找
     */
    Optional<KnowledgeCategory> findByKnowledgeBaseIdAndCategoryPath(Long kbId, String categoryPath);

    /**
     * 检查分类名称在同一知识库和父分类下是否重复
     */
    @Query("SELECT COUNT(c) > 0 FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND c.parentId = :parentId AND c.categoryName = :categoryName AND c.id != :excludeId")
    boolean existsCategoryNameInSameParent(@Param("kbId") Long kbId, @Param("parentId") Long parentId, @Param("categoryName") String categoryName, @Param("excludeId") Long excludeId);

    /**
     * 查找指定分类的所有子分类（递归）
     */
    @Query(value = "WITH RECURSIVE category_tree AS (" +
                   "  SELECT id, category_name, parent_id, level, category_path " +
                   "  FROM knowledge_category " +
                   "  WHERE id = :categoryId " +
                   "  UNION ALL " +
                   "  SELECT c.id, c.category_name, c.parent_id, c.level, c.category_path " +
                   "  FROM knowledge_category c " +
                   "  INNER JOIN category_tree ct ON c.parent_id = ct.id " +
                   ") " +
                   "SELECT * FROM category_tree WHERE id != :categoryId",
           nativeQuery = true)
    List<Object[]> findAllChildCategories(@Param("categoryId") Long categoryId);

    /**
     * 查找分类的父级路径
     */
    @Query(value = "WITH RECURSIVE category_path AS (" +
                   "  SELECT id, category_name, parent_id, level, 0 as depth " +
                   "  FROM knowledge_category " +
                   "  WHERE id = :categoryId " +
                   "  UNION ALL " +
                   "  SELECT c.id, c.category_name, c.parent_id, c.level, cp.depth + 1 " +
                   "  FROM knowledge_category c " +
                   "  INNER JOIN category_path cp ON c.id = cp.parent_id " +
                   ") " +
                   "SELECT * FROM category_path ORDER BY depth DESC",
           nativeQuery = true)
    List<Object[]> findCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * 统计分类下的文档数量
     */
    @Query("SELECT COUNT(d) FROM KnowledgeDocument d WHERE d.category.id = :categoryId AND d.enabled = true")
    Long countDocumentsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 统计分类及其子分类下的文档总数
     */
    @Query(value = "WITH RECURSIVE category_tree AS (" +
                   "  SELECT id FROM knowledge_category WHERE id = :categoryId " +
                   "  UNION ALL " +
                   "  SELECT c.id FROM knowledge_category c " +
                   "  INNER JOIN category_tree ct ON c.parent_id = ct.id " +
                   ") " +
                   "SELECT COUNT(*) FROM knowledge_document d " +
                   "WHERE d.category_id IN (SELECT id FROM category_tree) AND d.enabled = true",
           nativeQuery = true)
    Long countTotalDocumentsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查找指定层级的分类
     */
    @Query("SELECT c FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND c.level = :level AND c.enabled = true ORDER BY c.sortOrder ASC")
    List<KnowledgeCategory> findByKnowledgeBaseIdAndLevel(@Param("kbId") Long kbId, @Param("level") Integer level);

    /**
     * 获取分类的最大排序值
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND c.parentId = :parentId")
    Integer getMaxSortOrderByParent(@Param("kbId") Long kbId, @Param("parentId") Long parentId);

    /**
     * 搜索分类（按名称或描述）
     */
    @Query("SELECT c FROM KnowledgeCategory c WHERE c.knowledgeBase.id = :kbId AND (c.categoryName LIKE %:keyword% OR c.description LIKE %:keyword%) AND c.enabled = true ORDER BY c.level ASC, c.sortOrder ASC")
    List<KnowledgeCategory> searchByKeyword(@Param("kbId") Long kbId, @Param("keyword") String keyword);
} 