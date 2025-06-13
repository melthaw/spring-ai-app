package cn.mojoup.ai.upload.dto;

import cn.mojoup.ai.upload.domain.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件信息详情DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDetailDTO {
    
    private Long id;
    private String fileId;
    private String originalFilename;
    private String storedFilename;
    private String fileExtension;
    private String relativePath;
    private String fullPath;
    private Long fileSize;
    private String contentType;
    private String md5Hash;
    private String sha256Hash;
    private StorageType storageType;
    private String bucketName;
    private LocalDateTime uploadTime;
    private String uploadUserId;
    private String accessUrl;
    private String tags;
    private Map<String, Object> metadata;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
} 