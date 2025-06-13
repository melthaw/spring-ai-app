package cn.mojoup.ai.upload.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件信息模型
 * 
 * @author matt
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_info", indexes = {
        @Index(name = "idx_file_id", columnList = "file_id"),
        @Index(name = "idx_upload_user_id", columnList = "upload_user_id"),
        @Index(name = "idx_relative_path", columnList = "relative_path"),
        @Index(name = "idx_upload_time", columnList = "upload_time"),
        @Index(name = "idx_enabled", columnList = "enabled")
})
public class FileInfo {
    
    /**
     * 主键ID（仅用于数据库，API不暴露）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    
    /**
     * 文件ID（UUID）
     */
    @Column(name = "file_id", unique = true, nullable = false, length = 36)
    private String fileId;
    
    /**
     * 原始文件名
     */
    @Column(name = "original_file_name", nullable = false, length = 500)
    private String originalFileName;
    
    /**
     * 存储文件名（可能经过重命名）
     */
    @Column(name = "stored_file_name", nullable = false, length = 500)
    private String storedFileName;
    
    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;
    
    /**
     * 相对存储路径
     */
    @Column(name = "relative_path", nullable = false, length = 1000)
    private String relativePath;
    
    /**
     * 完整存储路径
     */
    @Column(name = "full_path", length = 2000)
    private String fullPath;
    
    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    /**
     * MIME类型
     */
    @Column(name = "content_type", length = 200)
    private String contentType;
    
    /**
     * MD5哈希值
     */
    @Column(name = "md5_hash", length = 32)
    private String md5Hash;
    
    /**
     * SHA256哈希值
     */
    @Column(name = "sha256_hash", length = 64)
    private String sha256Hash;
    
    /**
     * 存储类型（LOCAL/MINIO）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;
    
    /**
     * 存储桶名称（MinIO使用）
     */
    @Column(name = "bucket_name", length = 100)
    private String bucketName;
    
    /**
     * 上传时间
     */
    @CreationTimestamp
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    /**
     * 上传用户ID
     */
    @Column(name = "upload_user_id", length = 50)
    private String uploadUserId;
    
    /**
     * 文件访问URL
     */
    @Column(name = "access_url", length = 2000)
    private String accessUrl;
    
    /**
     * 文件标签
     */
    @Column(name = "tags", length = 1000)
    private String tags;
    
    /**
     * 扩展元数据
     */
    @Transient
    private Map<String, Object> metadata;
    
    /**
     * 元数据JSON字符串（数据库存储字段）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    @JsonIgnore
    private String metadataJson;
    
    /**
     * 是否启用（软删除标识）
     */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;
    
    /**
     * 创建者
     */
    @Column(name = "created_by", length = 50)
    @JsonIgnore
    private String createdBy;
    
    /**
     * 更新者
     */
    @Column(name = "updated_by", length = 50)
    @JsonIgnore
    private String updatedBy;
    
    /**
     * 版本号（乐观锁）
     */
    @Version
    @Column(name = "version")
    @JsonIgnore
    private Long version;
    
    // ==================== 元数据处理方法 ====================
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * JPA生命周期回调：加载后处理
     */
    @PostLoad
    public void postLoad() {
        if (metadataJson != null && !metadataJson.trim().isEmpty()) {
            try {
                this.metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse metadata JSON: {}", e.getMessage());
                this.metadata = new HashMap<>();
            }
        } else {
            this.metadata = new HashMap<>();
        }
    }
    
    /**
     * JPA生命周期回调：保存前处理
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (metadata != null && !metadata.isEmpty()) {
            try {
                this.metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata to JSON: {}", e.getMessage());
                this.metadataJson = "{}";
            }
        } else {
            this.metadataJson = "{}";
        }
    }
    
    /**
     * 设置元数据
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        // 立即同步到JSON字段
        if (metadata != null && !metadata.isEmpty()) {
            try {
                this.metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata to JSON: {}", e.getMessage());
                this.metadataJson = "{}";
            }
        } else {
            this.metadataJson = "{}";
        }
    }
} 