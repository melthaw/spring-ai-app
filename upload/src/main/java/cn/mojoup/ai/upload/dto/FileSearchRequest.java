package cn.mojoup.ai.upload.dto;

import cn.mojoup.ai.upload.domain.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件搜索请求DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRequest {
    
    private String filename;
    private String contentType;
    private String uploadedBy;
    private StorageType storageType;
    private String tags;
    private Long minSize;
    private Long maxSize;
    private LocalDateTime uploadedAfter;
    private LocalDateTime uploadedBefore;
} 