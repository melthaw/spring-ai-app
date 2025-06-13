package cn.mojoup.ai.kb.repository;

import cn.mojoup.ai.kb.entity.DocumentNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文档节点Repository接口
 *
 * @author matt
 */
@Repository
public interface DocumentNodeRepository extends JpaRepository<DocumentNode, String>, JpaSpecificationExecutor<DocumentNode> {

    /**
     * 根据知识库ID查找根节点
     */
    List<DocumentNode> findByKnowledgeBase_KbIdAndParentIsNullAndDeletedFalse(String kbId);

    /**
     * 根据父节点ID查找子节点
     */
    List<DocumentNode> findByParent_NodeIdAndDeletedFalseOrderBySortOrderAsc(String parentId);

    /**
     * 根据知识库ID和节点名称查找节点
     */
    Optional<DocumentNode> findByKnowledgeBase_KbIdAndNodeNameAndDeletedFalse(String kbId, String nodeName);

    /**
     * 根据文件ID查找文档节点
     */
    Optional<DocumentNode> findByFileIdAndDeletedFalse(String fileId);

    /**
     * 根据节点路径查找节点
     */
    Optional<DocumentNode> findByKnowledgeBase_KbIdAndNodePathAndDeletedFalse(String kbId, String nodePath);

    /**
     * 查找指定深度的节点
     */
    List<DocumentNode> findByKnowledgeBase_KbIdAndLevelAndDeletedFalse(String kbId, Integer level);

    /**
     * 查找指定类型的节点
     */
    List<DocumentNode> findByKnowledgeBase_KbIdAndNodeTypeAndDeletedFalse(String kbId, DocumentNode.NodeType nodeType);

    /**
     * 查找已嵌入的文档节点
     */
    List<DocumentNode> findByKnowledgeBase_KbIdAndEmbeddedTrueAndDeletedFalse(String kbId);

    /**
     * 查找未嵌入的文档节点
     */
    List<DocumentNode> findByKnowledgeBase_KbIdAndEmbeddedFalseAndNodeTypeAndDeletedFalse(String kbId, DocumentNode.NodeType nodeType);

    /**
     * 根据嵌入状态查找文档节点
     */
    List<DocumentNode> findByKnowledgeBase_KbIdAndEmbeddingStatusAndDeletedFalse(String kbId, DocumentNode.EmbeddingStatus embeddingStatus);

    /**
     * 分页查询用户可访问的文档节点
     */
    @Query("SELECT DISTINCT dn FROM DocumentNode dn " +
           "LEFT JOIN dn.permissions dnp " +
           "LEFT JOIN dn.knowledgeBase kb " +
           "LEFT JOIN kb.permissions kbp " +
           "WHERE dn.deleted = false " +
           "AND kb.deleted = false " +
           "AND dn.knowledgeBase.kbId = :kbId " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel = 'PUBLIC' " +
           "     OR dn.creatorId = :userId " +
           "     OR dn.accessLevel = 'PUBLIC' " +
           "     OR (kbp.principalType = 'USER' AND kbp.principalId = :userId AND kbp.status = 'ACTIVE') " +
           "     OR (kbp.principalType = 'DEPARTMENT' AND kbp.principalId = :departmentId AND kbp.status = 'ACTIVE') " +
           "     OR (kbp.principalType = 'ORGANIZATION' AND kbp.principalId = :organizationId AND kbp.status = 'ACTIVE') " +
           "     OR (dnp.principalType = 'USER' AND dnp.principalId = :userId AND dnp.status = 'ACTIVE') " +
           "     OR (dnp.principalType = 'DEPARTMENT' AND dnp.principalId = :departmentId AND dnp.status = 'ACTIVE') " +
           "     OR (dnp.principalType = 'ORGANIZATION' AND dnp.principalId = :organizationId AND dnp.status = 'ACTIVE'))")
    Page<DocumentNode> findAccessibleNodes(@Param("kbId") String kbId,
                                          @Param("userId") String userId,
                                          @Param("departmentId") String departmentId,
                                          @Param("organizationId") String organizationId,
                                          Pageable pageable);

    /**
     * 检查用户是否有权限访问节点
     */
    @Query("SELECT CASE WHEN COUNT(dn) > 0 THEN true ELSE false END " +
           "FROM DocumentNode dn " +
           "LEFT JOIN dn.permissions dnp " +
           "LEFT JOIN dn.knowledgeBase kb " +
           "LEFT JOIN kb.permissions kbp " +
           "WHERE dn.nodeId = :nodeId " +
           "AND dn.deleted = false " +
           "AND kb.deleted = false " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel = 'PUBLIC' " +
           "     OR dn.creatorId = :userId " +
           "     OR dn.accessLevel = 'PUBLIC' " +
           "     OR (kbp.principalType = 'USER' AND kbp.principalId = :userId AND kbp.status = 'ACTIVE') " +
           "     OR (kbp.principalType = 'DEPARTMENT' AND kbp.principalId = :departmentId AND kbp.status = 'ACTIVE') " +
           "     OR (kbp.principalType = 'ORGANIZATION' AND kbp.principalId = :organizationId AND kbp.status = 'ACTIVE') " +
           "     OR (dnp.principalType = 'USER' AND dnp.principalId = :userId AND dnp.status = 'ACTIVE') " +
           "     OR (dnp.principalType = 'DEPARTMENT' AND dnp.principalId = :departmentId AND dnp.status = 'ACTIVE') " +
           "     OR (dnp.principalType = 'ORGANIZATION' AND dnp.principalId = :organizationId AND dnp.status = 'ACTIVE'))")
    boolean hasAccess(@Param("nodeId") String nodeId,
                     @Param("userId") String userId,
                     @Param("departmentId") String departmentId,
                     @Param("organizationId") String organizationId);

    /**
     * 获取节点的所有子孙节点
     */
    @Query("SELECT dn FROM DocumentNode dn " +
           "WHERE dn.deleted = false " +
           "AND dn.nodePath LIKE CONCAT(:nodePath, '%') " +
           "ORDER BY dn.level, dn.sortOrder")
    List<DocumentNode> findDescendantNodes(@Param("nodePath") String nodePath);

    /**
     * 统计知识库下的文档数量
     */
    @Query("SELECT COUNT(dn) FROM DocumentNode dn " +
           "WHERE dn.knowledgeBase.kbId = :kbId " +
           "AND dn.nodeType = 'DOCUMENT' " +
           "AND dn.deleted = false")
    Long countDocumentsByKnowledgeBase(@Param("kbId") String kbId);

    /**
     * 统计知识库下的文件夹数量
     */
    @Query("SELECT COUNT(dn) FROM DocumentNode dn " +
           "WHERE dn.knowledgeBase.kbId = :kbId " +
           "AND dn.nodeType = 'FOLDER' " +
           "AND dn.deleted = false")
    Long countFoldersByKnowledgeBase(@Param("kbId") String kbId);

    /**
     * 计算知识库的总文件大小
     */
    @Query("SELECT COALESCE(SUM(dn.fileSize), 0) FROM DocumentNode dn " +
           "WHERE dn.knowledgeBase.kbId = :kbId " +
           "AND dn.nodeType = 'DOCUMENT' " +
           "AND dn.deleted = false")
    Long calculateTotalSizeByKnowledgeBase(@Param("kbId") String kbId);

    /**
     * 查找需要嵌入的文档
     */
    @Query("SELECT dn FROM DocumentNode dn " +
           "WHERE dn.knowledgeBase.kbId = :kbId " +
           "AND dn.nodeType = 'DOCUMENT' " +
           "AND dn.embedded = false " +
           "AND dn.embeddingStatus IN ('PENDING', 'FAILED') " +
           "AND dn.deleted = false " +
           "ORDER BY dn.createTime ASC")
    List<DocumentNode> findDocumentsForEmbedding(@Param("kbId") String kbId);

    /**
     * 根据标签查找文档节点
     */
    @Query("SELECT dn FROM DocumentNode dn " +
           "WHERE dn.knowledgeBase.kbId = :kbId " +
           "AND dn.tags LIKE CONCAT('%', :tag, '%') " +
           "AND dn.deleted = false")
    List<DocumentNode> findByTag(@Param("kbId") String kbId, @Param("tag") String tag);

    /**
     * 检查节点名称在同一父节点下是否唯一
     */
    @Query("SELECT CASE WHEN COUNT(dn) > 0 THEN true ELSE false END " +
           "FROM DocumentNode dn " +
           "WHERE dn.nodeName = :nodeName " +
           "AND (:parentId IS NULL AND dn.parent IS NULL OR dn.parent.nodeId = :parentId) " +
           "AND dn.knowledgeBase.kbId = :kbId " +
           "AND dn.deleted = false " +
           "AND (:excludeId IS NULL OR dn.nodeId != :excludeId)")
    boolean existsByNameInParent(@Param("nodeName") String nodeName,
                                @Param("parentId") String parentId,
                                @Param("kbId") String kbId,
                                @Param("excludeId") String excludeId);

    /**
     * 根据ID查找未删除的节点
     */
    Optional<DocumentNode> findByNodeIdAndDeletedFalse(String nodeId);

    /**
     * 查找最大排序值
     */
    @Query("SELECT COALESCE(MAX(dn.sortOrder), 0) FROM DocumentNode dn " +
           "WHERE (:parentId IS NULL AND dn.parent IS NULL OR dn.parent.nodeId = :parentId) " +
           "AND dn.knowledgeBase.kbId = :kbId " +
           "AND dn.deleted = false")
    Integer findMaxSortOrder(@Param("parentId") String parentId, @Param("kbId") String kbId);
} 