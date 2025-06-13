package cn.mojoup.ai.knowledgebase.repository;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 知识库Repository
 * 
 * @author matt
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    /**
     * 根据知识库编码查找
     */
    Optional<KnowledgeBase> findByKbCode(String kbCode);

    /**
     * 检查知识库编码是否存在
     */
    boolean existsByKbCode(String kbCode);

    /**
     * 根据所有者ID查找知识库列表
     */
    List<KnowledgeBase> findByOwnerIdOrderByCreateTimeDesc(String ownerId);

    /**
     * 根据所有者ID和状态查找知识库列表
     */
    List<KnowledgeBase> findByOwnerIdAndStatusOrderByCreateTimeDesc(String ownerId, KnowledgeBase.KnowledgeBaseStatus status);

    /**
     * 根据知识库类型查找
     */
    Page<KnowledgeBase> findByKbType(KnowledgeBase.KnowledgeBaseType kbType, Pageable pageable);

    /**
     * 查找公开的知识库
     */
    Page<KnowledgeBase> findByIsPublicTrueAndStatusOrderByCreateTimeDesc(KnowledgeBase.KnowledgeBaseStatus status, Pageable pageable);

    /**
     * 根据访问级别查找
     */
    Page<KnowledgeBase> findByAccessLevelAndStatusOrderByCreateTimeDesc(KnowledgeBase.AccessLevel accessLevel, KnowledgeBase.KnowledgeBaseStatus status, Pageable pageable);

    /**
     * 搜索知识库（按名称或描述）
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE " +
           "(kb.kbName LIKE %:keyword% OR kb.description LIKE %:keyword%) " +
           "AND kb.status = :status " +
           "ORDER BY kb.createTime DESC")
    Page<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword, 
                                       @Param("status") KnowledgeBase.KnowledgeBaseStatus status, 
                                       Pageable pageable);

    /**
     * 根据所有者搜索知识库
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE " +
           "kb.ownerId = :ownerId " +
           "AND (kb.kbName LIKE %:keyword% OR kb.description LIKE %:keyword%) " +
           "AND kb.status = :status " +
           "ORDER BY kb.createTime DESC")
    Page<KnowledgeBase> searchByOwnerAndKeyword(@Param("ownerId") String ownerId,
                                               @Param("keyword") String keyword,
                                               @Param("status") KnowledgeBase.KnowledgeBaseStatus status,
                                               Pageable pageable);

    /**
     * 统计所有者的知识库数量
     */
    @Query("SELECT COUNT(kb) FROM KnowledgeBase kb WHERE kb.ownerId = :ownerId AND kb.status = :status")
    Long countByOwnerIdAndStatus(@Param("ownerId") String ownerId, @Param("status") KnowledgeBase.KnowledgeBaseStatus status);

    /**
     * 查找最近更新的知识库
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE " +
           "kb.lastUpdated >= :since " +
           "AND kb.status = :status " +
           "ORDER BY kb.lastUpdated DESC")
    List<KnowledgeBase> findRecentlyUpdated(@Param("since") LocalDateTime since, 
                                           @Param("status") KnowledgeBase.KnowledgeBaseStatus status);

    /**
     * 查找大小超过阈值的知识库
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.totalSize > :sizeThreshold ORDER BY kb.totalSize DESC")
    List<KnowledgeBase> findLargeKnowledgeBases(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * 统计不同类型的知识库数量
     */
    @Query("SELECT kb.kbType, COUNT(kb) FROM KnowledgeBase kb WHERE kb.status = :status GROUP BY kb.kbType")
    List<Object[]> countByKbTypeAndStatus(@Param("status") KnowledgeBase.KnowledgeBaseStatus status);
} 