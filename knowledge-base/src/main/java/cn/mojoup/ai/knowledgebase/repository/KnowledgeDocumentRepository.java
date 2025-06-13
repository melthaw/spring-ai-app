package cn.mojoup.ai.knowledgebase.repository;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument;
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
 * 知识库文档Repository
 * 
 * @author matt
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    /**
     * 根据文件ID查找文档
     */
    Optional<KnowledgeDocument> findByFileId(String fileId);

    /**
     * 根据分类ID查找文档列表
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.category.id = :categoryId AND d.enabled = true ORDER BY d.sortOrder ASC, d.createTime DESC")
    List<KnowledgeDocument> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID分页查找文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.category.id = :categoryId AND d.enabled = true ORDER BY d.sortOrder ASC, d.createTime DESC")
    Page<KnowledgeDocument> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 根据知识库ID查找文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.kbId = :kbId AND d.enabled = true ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> findByKbId(@Param("kbId") Long kbId, Pageable pageable);

    /**
     * 根据状态查找文档
     */
    Page<KnowledgeDocument> findByStatusAndEnabledTrueOrderByCreateTimeDesc(KnowledgeDocument.DocumentStatus status, Pageable pageable);

    /**
     * 搜索文档（按标题和内容）
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "(d.title LIKE %:keyword% OR d.content LIKE %:keyword% OR d.summary LIKE %:keyword%) " +
           "AND d.enabled = true " +
           "ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 在指定知识库中搜索文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "d.kbId = :kbId " +
           "AND (d.title LIKE %:keyword% OR d.content LIKE %:keyword% OR d.summary LIKE %:keyword%) " +
           "AND d.enabled = true " +
           "ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> searchByKbIdAndKeyword(@Param("kbId") Long kbId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 在指定分类中搜索文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "d.category.id = :categoryId " +
           "AND (d.title LIKE %:keyword% OR d.content LIKE %:keyword% OR d.summary LIKE %:keyword%) " +
           "AND d.enabled = true " +
           "ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> searchByCategoryIdAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据文档类型查找
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.kbId = :kbId AND d.docType = :docType AND d.enabled = true ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> findByKbIdAndDocType(@Param("kbId") Long kbId, @Param("docType") KnowledgeDocument.DocumentType docType, Pageable pageable);

    /**
     * 根据创建者查找文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.createdBy = :createdBy AND d.enabled = true ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> findByCreatedBy(@Param("createdBy") String createdBy, Pageable pageable);

    /**
     * 查找处理失败的文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.status = :status AND d.enabled = true ORDER BY d.createTime DESC")
    List<KnowledgeDocument> findFailedDocuments(@Param("status") KnowledgeDocument.DocumentStatus status);

    /**
     * 查找需要重新处理的文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "d.status IN (:statuses) " +
           "AND d.enabled = true " +
           "ORDER BY d.createTime ASC")
    List<KnowledgeDocument> findDocumentsToReprocess(@Param("statuses") List<KnowledgeDocument.DocumentStatus> statuses);

    /**
     * 统计知识库中不同状态的文档数量
     */
    @Query("SELECT d.status, COUNT(d) FROM KnowledgeDocument d WHERE d.kbId = :kbId AND d.enabled = true GROUP BY d.status")
    List<Object[]> countByKbIdAndStatus(@Param("kbId") Long kbId);

    /**
     * 统计分类中不同状态的文档数量
     */
    @Query("SELECT d.status, COUNT(d) FROM KnowledgeDocument d WHERE d.category.id = :categoryId AND d.enabled = true GROUP BY d.status")
    List<Object[]> countByCategoryIdAndStatus(@Param("categoryId") Long categoryId);

    /**
     * 统计知识库的文档总大小
     */
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM KnowledgeDocument d WHERE d.kbId = :kbId AND d.enabled = true")
    Long sumFileSizeByKbId(@Param("kbId") Long kbId);

    /**
     * 查找最近创建的文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "d.kbId = :kbId " +
           "AND d.createTime >= :since " +
           "AND d.enabled = true " +
           "ORDER BY d.createTime DESC")
    List<KnowledgeDocument> findRecentDocuments(@Param("kbId") Long kbId, @Param("since") LocalDateTime since);

    /**
     * 查找大文件文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.fileSize > :sizeThreshold AND d.enabled = true ORDER BY d.fileSize DESC")
    List<KnowledgeDocument> findLargeDocuments(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * 根据标签查找文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE d.tags LIKE %:tag% AND d.enabled = true ORDER BY d.createTime DESC")
    Page<KnowledgeDocument> findByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * 查找处理时间超过阈值的文档
     */
    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "d.processStartTime IS NOT NULL " +
           "AND d.processEndTime IS NOT NULL " +
           "AND (EXTRACT(EPOCH FROM (d.processEndTime - d.processStartTime)) * 1000) > :thresholdMs " +
           "AND d.enabled = true " +
           "ORDER BY (d.processEndTime - d.processStartTime) DESC")
    List<KnowledgeDocument> findSlowProcessingDocuments(@Param("thresholdMs") Long thresholdMs);

    /**
     * 统计不同文档类型的数量
     */
    @Query("SELECT d.docType, COUNT(d) FROM KnowledgeDocument d WHERE d.kbId = :kbId AND d.enabled = true GROUP BY d.docType")
    List<Object[]> countByKbIdAndDocType(@Param("kbId") Long kbId);

    /**
     * 查找重复文件ID的文档
     */
    @Query("SELECT d.fileId, COUNT(d) FROM KnowledgeDocument d WHERE d.enabled = true GROUP BY d.fileId HAVING COUNT(d) > 1")
    List<Object[]> findDuplicateFileIds();

    /**
     * 获取分类下文档的最大排序值
     */
    @Query("SELECT COALESCE(MAX(d.sortOrder), 0) FROM KnowledgeDocument d WHERE d.category.id = :categoryId")
    Integer getMaxSortOrderByCategoryId(@Param("categoryId") Long categoryId);
} 