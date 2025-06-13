package cn.mojoup.ai.upload.dto;

import cn.mojoup.ai.upload.domain.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息列表DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoListDTO {
    
    private Long id;
    private String fileId;
    private String originalFilename;
    private String fileExtension;
    private Long fileSize;
    private String contentType;
    private StorageType storageType;
    private LocalDateTime uploadTime;
    private String uploadUserId;
    private String tags;
    private Boolean enabled;
} 