package cn.mojoup.ai.knowledgebase.repository;

import cn.mojoup.ai.knowledgebase.domain.DocumentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文档嵌入Repository
 * 
 * @author matt
 */
@Repository
public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {

    /**
     * 根据文档ID查找嵌入记录
     */
    @Query("SELECT e FROM DocumentEmbedding e WHERE e.document.id = :documentId AND e.enabled = true ORDER BY e.createTime DESC")
    List<DocumentEmbedding> findByDocumentIdOrderByCreateTimeDesc(@Param("documentId") Long documentId);

    /**
     * 根据文档ID和嵌入模型查找嵌入记录
     */
    @Query("SELECT e FROM DocumentEmbedding e WHERE e.document.id = :documentId AND e.embeddingModel = :model AND e.enabled = true ORDER BY e.createTime DESC")
    List<DocumentEmbedding> findByDocumentIdAndEmbeddingModel(@Param("documentId") Long documentId, @Param("model") String embeddingModel);

    /**
     * 查找最新的嵌入记录
     */
    @Query("SELECT e FROM DocumentEmbedding e WHERE e.document.id = :documentId AND e.enabled = true ORDER BY e.createTime DESC")
    Optional<DocumentEmbedding> findLatestByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据状态查找嵌入记录
     */
    List<DocumentEmbedding> findByStatusAndEnabledTrueOrderByCreateTimeAsc(DocumentEmbedding.EmbeddingStatus status);

    /**
     * 查找失败且可重试的嵌入记录
     */
    @Query("SELECT e FROM DocumentEmbedding e WHERE e.status = :status AND e.retryCount < e.maxRetryCount AND e.enabled = true ORDER BY e.createTime ASC")
    List<DocumentEmbedding> findRetryableEmbeddings(@Param("status") DocumentEmbedding.EmbeddingStatus status);

    /**
     * 统计文档的嵌入数量
     */
    @Query("SELECT COUNT(e) FROM DocumentEmbedding e WHERE e.document.id = :documentId AND e.enabled = true")
    Long countByDocumentId(@Param("documentId") Long documentId);

    /**
     * 统计不同状态的嵌入数量
     */
    @Query("SELECT e.status, COUNT(e) FROM DocumentEmbedding e WHERE e.enabled = true GROUP BY e.status")
    List<Object[]> countByStatus();

    /**
     * 根据向量存储ID查找嵌入记录
     */
    Optional<DocumentEmbedding> findByVectorStoreIdAndEnabledTrue(String vectorStoreId);

    /**
     * 查找高质量的嵌入记录
     */
    @Query("SELECT e FROM DocumentEmbedding e WHERE e.qualityScore >= :minScore AND e.enabled = true ORDER BY e.qualityScore DESC")
    List<DocumentEmbedding> findHighQualityEmbeddings(@Param("minScore") Double minScore);

    /**
     * 删除文档的所有嵌入记录
     */
    @Query("UPDATE DocumentEmbedding e SET e.enabled = false WHERE e.document.id = :documentId")
    void disableByDocumentId(@Param("documentId") Long documentId);
} 