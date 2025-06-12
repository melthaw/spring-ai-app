package cn.mojoup.ai.upload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件上传响应模型
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 单个文件信息（单文件上传）
     */
    private FileInfo fileInfo;
    
    /**
     * 多个文件信息（多文件上传）
     */
    private List<FileInfo> fileInfos;
    
    /**
     * 上传文件数量
     */
    private Integer fileCount;
    
    /**
     * 总文件大小
     */
    private Long totalSize;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;
    
    /**
     * 创建成功响应
     */
    public static UploadResponse success(FileInfo fileInfo) {
        return UploadResponse.builder()
                .success(true)
                .message("File uploaded successfully")
                .fileInfo(fileInfo)
                .fileCount(1)
                .totalSize(fileInfo.getFileSize())
                .build();
    }
    
    /**
     * 创建成功响应（多文件）
     */
    public static UploadResponse success(List<FileInfo> fileInfos) {
        long totalSize = fileInfos.stream().mapToLong(FileInfo::getFileSize).sum();
        return UploadResponse.builder()
                .success(true)
                .message("Files uploaded successfully")
                .fileInfos(fileInfos)
                .fileCount(fileInfos.size())
                .totalSize(totalSize)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static UploadResponse failure(String message) {
        return UploadResponse.builder()
                .success(false)
                .message(message)
                .fileCount(0)
                .totalSize(0L)
                .build();
    }
} 