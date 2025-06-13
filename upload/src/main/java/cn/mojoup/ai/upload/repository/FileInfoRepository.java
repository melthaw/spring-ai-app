package cn.mojoup.ai.upload.repository;

import cn.mojoup.ai.upload.domain.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件信息Repository
 * 
 * @author matt
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    
    /**
     * 根据文件ID查找文件信息
     */
    Optional<FileInfo> findByFileIdAndEnabledTrue(String fileId);
    
    /**
     * 根据文件ID查找文件信息（包括已删除的）
     */
    Optional<FileInfo> findByFileId(String fileId);
    
    /**
     * 根据相对路径查找文件信息
     */
    Optional<FileInfo> findByRelativePathAndEnabledTrue(String relativePath);
    
    /**
     * 根据上传用户ID查找文件信息
     */
    List<FileInfo> findByUploadUserIdAndEnabledTrueOrderByUploadTimeDesc(String uploadUserId);
    
    /**
     * 根据上传用户ID分页查找文件信息
     */
    Page<FileInfo> findByUploadUserIdAndEnabledTrueOrderByUploadTimeDesc(String uploadUserId, Pageable pageable);
    
    /**
     * 根据标签查找文件信息
     */
    List<FileInfo> findByTagsContainingAndEnabledTrueOrderByUploadTimeDesc(String tags);
    
    /**
     * 根据文件类型查找文件信息
     */
    List<FileInfo> findByContentTypeAndEnabledTrueOrderByUploadTimeDesc(String contentType);
    
    /**
     * 根据文件扩展名查找文件信息
     */
    List<FileInfo> findByFileExtensionAndEnabledTrueOrderByUploadTimeDesc(String fileExtension);
    
    /**
     * 根据上传时间范围查找文件信息
     */
    List<FileInfo> findByUploadTimeBetweenAndEnabledTrueOrderByUploadTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据文件大小范围查找文件信息
     */
    List<FileInfo> findByFileSizeBetweenAndEnabledTrueOrderByUploadTimeDesc(
            Long minSize, Long maxSize);
    
    /**
     * 检查文件ID是否存在
     */
    boolean existsByFileIdAndEnabledTrue(String fileId);
    
    /**
     * 检查相对路径是否存在
     */
    boolean existsByRelativePathAndEnabledTrue(String relativePath);
    
    /**
     * 软删除文件信息
     */
    @Modifying
    @Query("UPDATE FileInfo f SET f.enabled = false, f.updatedAt = :updatedAt, f.updatedBy = :updatedBy WHERE f.fileId = :fileId")
    int softDeleteByFileId(@Param("fileId") String fileId, 
                          @Param("updatedAt") LocalDateTime updatedAt, 
                          @Param("updatedBy") String updatedBy);
    
    /**
     * 批量软删除文件信息
     */
    @Modifying
    @Query("UPDATE FileInfo f SET f.enabled = false, f.updatedAt = :updatedAt, f.updatedBy = :updatedBy WHERE f.fileId IN :fileIds")
    int softDeleteByFileIds(@Param("fileIds") List<String> fileIds, 
                           @Param("updatedAt") LocalDateTime updatedAt, 
                           @Param("updatedBy") String updatedBy);
    
    /**
     * 恢复软删除的文件信息
     */
    @Modifying
    @Query("UPDATE FileInfo f SET f.enabled = true, f.updatedAt = :updatedAt, f.updatedBy = :updatedBy WHERE f.fileId = :fileId")
    int restoreByFileId(@Param("fileId") String fileId, 
                       @Param("updatedAt") LocalDateTime updatedAt, 
                       @Param("updatedBy") String updatedBy);
    
    /**
     * 物理删除文件信息
     */
    void deleteByFileId(String fileId);
    
    /**
     * 批量物理删除文件信息
     */
    void deleteByFileIdIn(List<String> fileIds);
    
    /**
     * 统计用户上传的文件数量
     */
    @Query("SELECT COUNT(f) FROM FileInfo f WHERE f.uploadUserId = :uploadUserId AND f.enabled = true")
    long countByUploadUserId(@Param("uploadUserId") String uploadUserId);
    
    /**
     * 统计用户上传的文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f WHERE f.uploadUserId = :uploadUserId AND f.enabled = true")
    long sumFileSizeByUploadUserId(@Param("uploadUserId") String uploadUserId);
    
    /**
     * 查找重复的MD5哈希值
     */
    @Query("SELECT f FROM FileInfo f WHERE f.md5Hash = :md5Hash AND f.enabled = true")
    List<FileInfo> findByMd5Hash(@Param("md5Hash") String md5Hash);
    
    /**
     * 查找重复的SHA256哈希值
     */
    @Query("SELECT f FROM FileInfo f WHERE f.sha256Hash = :sha256Hash AND f.enabled = true")
    List<FileInfo> findBySha256Hash(@Param("sha256Hash") String sha256Hash);
    
    /**
     * 查找过期的文件（用于清理）
     */
    @Query("SELECT f FROM FileInfo f WHERE f.uploadTime < :expireTime AND f.enabled = true")
    List<FileInfo> findExpiredFiles(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 全文搜索文件信息
     */
    @Query("SELECT f FROM FileInfo f WHERE " +
           "(f.originalFileName LIKE %:keyword% OR " +
           "f.tags LIKE %:keyword% OR " +
           "f.contentType LIKE %:keyword%) AND " +
           "f.enabled = true " +
           "ORDER BY f.uploadTime DESC")
    Page<FileInfo> searchFiles(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 高级搜索文件信息
     */
    @Query("SELECT f FROM FileInfo f WHERE " +
           "(:fileName IS NULL OR f.originalFileName LIKE %:fileName%) AND " +
           "(:contentType IS NULL OR f.contentType = :contentType) AND " +
           "(:uploadUserId IS NULL OR f.uploadUserId = :uploadUserId) AND " +
           "(:startTime IS NULL OR f.uploadTime >= :startTime) AND " +
           "(:endTime IS NULL OR f.uploadTime <= :endTime) AND " +
           "(:minSize IS NULL OR f.fileSize >= :minSize) AND " +
           "(:maxSize IS NULL OR f.fileSize <= :maxSize) AND " +
           "f.enabled = true " +
           "ORDER BY f.uploadTime DESC")
    Page<FileInfo> advancedSearch(@Param("fileName") String fileName,
                                       @Param("contentType") String contentType,
                                       @Param("uploadUserId") String uploadUserId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("minSize") Long minSize,
                                       @Param("maxSize") Long maxSize,
                                       Pageable pageable);
} 