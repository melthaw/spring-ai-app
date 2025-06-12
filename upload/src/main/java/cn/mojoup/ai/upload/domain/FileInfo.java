package cn.mojoup.ai.upload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件信息模型
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    
    /**
     * 文件ID（UUID）
     */
    private String fileId;
    
    /**
     * 原始文件名
     */
    private String originalFileName;
    
    /**
     * 存储文件名（可能经过重命名）
     */
    private String storedFileName;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 相对存储路径
     */
    private String relativePath;
    
    /**
     * 完整存储路径
     */
    private String fullPath;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * MIME类型
     */
    private String contentType;
    
    /**
     * MD5哈希值
     */
    private String md5Hash;
    
    /**
     * SHA256哈希值
     */
    private String sha256Hash;
    
    /**
     * 存储类型（LOCAL/MINIO）
     */
    private StorageType storageType;
    
    /**
     * 存储桶名称（MinIO使用）
     */
    private String bucketName;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 上传用户ID
     */
    private String uploadUserId;
    
    /**
     * 文件访问URL
     */
    private String accessUrl;
    
    /**
     * 文件标签
     */
    private String tags;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 是否启用（软删除标识）
     */
    private Boolean enabled;
} 