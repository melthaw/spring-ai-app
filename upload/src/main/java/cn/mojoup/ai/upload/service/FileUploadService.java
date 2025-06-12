package cn.mojoup.ai.upload.service;

import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.domain.FileUploadEvent;
import cn.mojoup.ai.upload.domain.UploadRequest;
import cn.mojoup.ai.upload.domain.UploadResponse;
import cn.mojoup.ai.upload.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 文件上传业务服务
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final FileInfoStorageService fileInfoStorageService;
    
    /**
     * 上传单个文件
     * 
     * @param file 上传的文件
     * @param request 上传请求参数
     * @return 上传响应
     */
    public UploadResponse uploadSingleFile(MultipartFile file, UploadRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Starting single file upload: {}", file.getOriginalFilename());
            
            FileInfo fileInfo = fileStorageService.uploadFile(file, request);
            
            // 发布文件上传成功事件
            FileUploadEvent event = new FileUploadEvent(this, fileInfo, FileUploadEvent.EventType.FILE_UPLOADED);
            eventPublisher.publishEvent(event);
            
            UploadResponse response = UploadResponse.success(fileInfo);
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            log.info("Single file upload completed: {} in {} ms", 
                    file.getOriginalFilename(), response.getProcessingTime());
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to upload single file: {}", file.getOriginalFilename(), e);
            
            UploadResponse response = UploadResponse.failure("Failed to upload file: " + e.getMessage());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            return response;
        }
    }
    
    /**
     * 上传多个文件
     * 
     * @param files 上传的文件列表
     * @param request 上传请求参数
     * @return 上传响应
     */
    public UploadResponse uploadMultipleFiles(List<MultipartFile> files, UploadRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Starting multiple files upload: {} files", files.size());
            
            List<FileInfo> fileInfos = fileStorageService.uploadFiles(files, request);
            
            // 发布批量文件上传成功事件
            FileUploadEvent batchEvent = new FileUploadEvent(
                    this, 
                    fileInfos.isEmpty() ? null : fileInfos.get(0), // 使用第一个文件作为代表
                    FileUploadEvent.EventType.BATCH_FILES_UPLOADED
            );
            batchEvent.setExtraData(fileInfos); // 将所有文件信息放到extraData中
            eventPublisher.publishEvent(batchEvent);
            
            UploadResponse response = UploadResponse.success(fileInfos);
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            log.info("Multiple files upload completed: {} files in {} ms", 
                    fileInfos.size(), response.getProcessingTime());
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to upload multiple files", e);
            
            UploadResponse response = UploadResponse.failure("Failed to upload files: " + e.getMessage());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            return response;
        }
    }
    
    /**
     * 根据相对路径获取文件资源
     * 
     * @param relativePath 相对路径
     * @return Spring Resource
     */
    public Resource getFileResource(String relativePath) {
        try {
            log.debug("Getting file resource: {}", relativePath);
            return fileStorageService.getResource(relativePath);
        } catch (Exception e) {
            log.error("Failed to get file resource: {}", relativePath, e);
            throw e;
        }
    }
    
    /**
     * 根据文件ID删除文件（推荐使用）
     * 
     * @param fileId 文件ID
     * @return 是否成功
     */
    public boolean deleteFileById(String fileId) {
        try {
            log.info("Deleting file by ID: {}", fileId);
            
            // 根据fileId查询文件信息
            Optional<FileInfo> fileInfoOpt = fileInfoStorageService.findByFileId(fileId);
            if (fileInfoOpt.isEmpty()) {
                log.warn("File not found: {}", fileId);
                return false;
            }
            
            FileInfo fileInfo = fileInfoOpt.get();
            
            // 删除实际文件
            boolean result = fileStorageService.deleteFile(fileInfo.getRelativePath());
            
            if (result) {
                // 发布文件删除事件
                FileUploadEvent event = new FileUploadEvent(this, fileInfo, FileUploadEvent.EventType.FILE_DELETED);
                eventPublisher.publishEvent(event);
                
                log.info("File deleted successfully: fileId={}, path={}", fileId, fileInfo.getRelativePath());
            } else {
                log.warn("Failed to delete file: fileId={}, path={}", fileId, fileInfo.getRelativePath());
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileId, e);
            return false;
        }
    }
    

    
    /**
     * 检查文件是否存在
     * 
     * @param relativePath 相对路径
     * @return 是否存在
     */
    public boolean fileExists(String relativePath) {
        try {
            return fileStorageService.fileExists(relativePath);
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", relativePath, e);
            return false;
        }
    }
    
    /**
     * 获取文件访问URL
     * 
     * @param relativePath 相对路径
     * @return 访问URL
     */
    public String getFileUrl(String relativePath) {
        try {
            return fileStorageService.getFileUrl(relativePath);
        } catch (Exception e) {
            log.error("Failed to get file URL: {}", relativePath, e);
            return null;
        }
    }
    
    /**
     * 根据文件ID获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    public Optional<FileInfo> getFileInfoById(String fileId) {
        try {
            return fileInfoStorageService.findByFileId(fileId);
        } catch (Exception e) {
            log.error("Failed to get file info by ID: {}", fileId, e);
            return Optional.empty();
        }
    }
    
    /**
     * 根据文件ID获取文件资源
     * 
     * @param fileId 文件ID
     * @return Spring Resource
     */
    public Resource getFileResourceById(String fileId) {
        try {
            Optional<FileInfo> fileInfoOpt = fileInfoStorageService.findByFileId(fileId);
            if (fileInfoOpt.isEmpty()) {
                throw new RuntimeException("File not found: " + fileId);
            }
            
            FileInfo fileInfo = fileInfoOpt.get();
            return fileStorageService.getResource(fileInfo.getRelativePath());
        } catch (Exception e) {
            log.error("Failed to get file resource by ID: {}", fileId, e);
            throw e;
        }
    }
    
    /**
     * 根据文件ID检查文件是否存在
     * 
     * @param fileId 文件ID
     * @return 是否存在
     */
    public boolean fileExistsById(String fileId) {
        try {
            return fileInfoStorageService.existsByFileId(fileId);
        } catch (Exception e) {
            log.error("Failed to check file existence by ID: {}", fileId, e);
            return false;
        }
    }
    
    /**
     * 根据文件ID获取文件访问URL
     * 
     * @param fileId 文件ID
     * @return 访问URL
     */
    public String getFileUrlById(String fileId) {
        try {
            Optional<FileInfo> fileInfoOpt = fileInfoStorageService.findByFileId(fileId);
            if (fileInfoOpt.isEmpty()) {
                return null;
            }
            
            FileInfo fileInfo = fileInfoOpt.get();
            return fileStorageService.getFileUrl(fileInfo.getRelativePath());
        } catch (Exception e) {
            log.error("Failed to get file URL by ID: {}", fileId, e);
            return null;
        }
    }
} 