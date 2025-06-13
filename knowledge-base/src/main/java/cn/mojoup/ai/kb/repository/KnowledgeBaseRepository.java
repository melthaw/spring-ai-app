package cn.mojoup.ai.kb.repository;

import cn.mojoup.ai.kb.entity.KnowledgeBase;
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
 * 知识库Repository接口
 *
 * @author matt
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, String>, JpaSpecificationExecutor<KnowledgeBase> {

    /**
     * 根据名称查找知识库（未删除）
     */
    Optional<KnowledgeBase> findByKbNameAndDeletedFalse(String kbName);

    /**
     * 根据创建者查找知识库
     */
    List<KnowledgeBase> findByCreatorIdAndDeletedFalse(String creatorId);

    /**
     * 根据组织ID查找知识库
     */
    List<KnowledgeBase> findByOrganizationIdAndDeletedFalse(String organizationId);

    /**
     * 根据部门ID查找知识库
     */
    List<KnowledgeBase> findByDepartmentIdAndDeletedFalse(String departmentId);

    /**
     * 根据状态查找知识库
     */
    List<KnowledgeBase> findByStatusAndDeletedFalse(KnowledgeBase.KnowledgeBaseStatus status);

    /**
     * 根据访问级别查找知识库
     */
    List<KnowledgeBase> findByAccessLevelAndDeletedFalse(KnowledgeBase.AccessLevel accessLevel);

    /**
     * 分页查询用户可访问的知识库
     */
    @Query("SELECT DISTINCT kb FROM KnowledgeBase kb " +
           "LEFT JOIN kb.permissions p " +
           "WHERE kb.deleted = false " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel = 'PUBLIC' " +
           "     OR (p.principalType = 'USER' AND p.principalId = :userId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'DEPARTMENT' AND p.principalId = :departmentId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'ORGANIZATION' AND p.principalId = :organizationId AND p.status = 'ACTIVE'))")
    Page<KnowledgeBase> findAccessibleKnowledgeBases(@Param("userId") String userId,
                                                     @Param("departmentId") String departmentId,
                                                     @Param("organizationId") String organizationId,
                                                     Pageable pageable);

    /**
     * 查询用户在指定组织下的知识库
     */
    @Query("SELECT DISTINCT kb FROM KnowledgeBase kb " +
           "LEFT JOIN kb.permissions p " +
           "WHERE kb.deleted = false " +
           "AND kb.organizationId = :organizationId " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel IN ('PUBLIC', 'INTERNAL') " +
           "     OR (p.principalType = 'USER' AND p.principalId = :userId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'DEPARTMENT' AND p.principalId = :departmentId AND p.status = 'ACTIVE'))")
    List<KnowledgeBase> findByOrganizationAndUser(@Param("organizationId") String organizationId,
                                                  @Param("userId") String userId,
                                                  @Param("departmentId") String departmentId);

    /**
     * 查询用户在指定部门下的知识库
     */
    @Query("SELECT DISTINCT kb FROM KnowledgeBase kb " +
           "LEFT JOIN kb.permissions p " +
           "WHERE kb.deleted = false " +
           "AND kb.departmentId = :departmentId " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel IN ('PUBLIC', 'INTERNAL') " +
           "     OR (p.principalType = 'USER' AND p.principalId = :userId AND p.status = 'ACTIVE'))")
    List<KnowledgeBase> findByDepartmentAndUser(@Param("departmentId") String departmentId,
                                               @Param("userId") String userId);

    /**
     * 检查用户是否有权限访问知识库
     */
    @Query("SELECT CASE WHEN COUNT(kb) > 0 THEN true ELSE false END " +
           "FROM KnowledgeBase kb " +
           "LEFT JOIN kb.permissions p " +
           "WHERE kb.kbId = :kbId " +
           "AND kb.deleted = false " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel = 'PUBLIC' " +
           "     OR (p.principalType = 'USER' AND p.principalId = :userId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'DEPARTMENT' AND p.principalId = :departmentId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'ORGANIZATION' AND p.principalId = :organizationId AND p.status = 'ACTIVE'))")
    boolean hasAccess(@Param("kbId") String kbId,
                     @Param("userId") String userId,
                     @Param("departmentId") String departmentId,
                     @Param("organizationId") String organizationId);

    /**
     * 统计用户可访问的知识库数量
     */
    @Query("SELECT COUNT(DISTINCT kb.kbId) FROM KnowledgeBase kb " +
           "LEFT JOIN kb.permissions p " +
           "WHERE kb.deleted = false " +
           "AND (kb.creatorId = :userId " +
           "     OR kb.accessLevel = 'PUBLIC' " +
           "     OR (p.principalType = 'USER' AND p.principalId = :userId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'DEPARTMENT' AND p.principalId = :departmentId AND p.status = 'ACTIVE') " +
           "     OR (p.principalType = 'ORGANIZATION' AND p.principalId = :organizationId AND p.status = 'ACTIVE'))")
    Long countAccessibleKnowledgeBases(@Param("userId") String userId,
                                      @Param("departmentId") String departmentId,
                                      @Param("organizationId") String organizationId);

    /**
     * 根据类型统计知识库数量
     */
    @Query("SELECT kb.kbType, COUNT(kb) FROM KnowledgeBase kb " +
           "WHERE kb.deleted = false " +
           "GROUP BY kb.kbType")
    List<Object[]> countByType();

    /**
     * 根据状态统计知识库数量
     */
    @Query("SELECT kb.status, COUNT(kb) FROM KnowledgeBase kb " +
           "WHERE kb.deleted = false " +
           "GROUP BY kb.status")
    List<Object[]> countByStatus();

    /**
     * 查找需要维护的知识库（文档数量超过限制）
     */
    @Query("SELECT kb FROM KnowledgeBase kb " +
           "WHERE kb.deleted = false " +
           "AND kb.documentCount > :threshold " +
           "ORDER BY kb.documentCount DESC")
    List<KnowledgeBase> findKnowledgeBasesForMaintenance(@Param("threshold") Integer threshold);

    /**
     * 根据ID查找未删除的知识库
     */
    Optional<KnowledgeBase> findByKbIdAndDeletedFalse(String kbId);

    /**
     * 检查知识库名称是否在组织内唯一
     */
    @Query("SELECT CASE WHEN COUNT(kb) > 0 THEN true ELSE false END " +
           "FROM KnowledgeBase kb " +
           "WHERE kb.kbName = :kbName " +
           "AND kb.organizationId = :organizationId " +
           "AND kb.deleted = false " +
           "AND (:excludeId IS NULL OR kb.kbId != :excludeId)")
    boolean existsByNameInOrganization(@Param("kbName") String kbName,
                                      @Param("organizationId") String organizationId,
                                      @Param("excludeId") String excludeId);
} 