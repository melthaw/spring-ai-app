package cn.mojoup.ai.upload.service;

import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.domain.UploadRequest;
import cn.mojoup.ai.upload.domain.UploadResponse;
import cn.mojoup.ai.upload.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * 删除文件
     * 
     * @param relativePath 相对路径
     * @return 是否成功
     */
    public boolean deleteFile(String relativePath) {
        try {
            log.info("Deleting file: {}", relativePath);
            boolean result = fileStorageService.deleteFile(relativePath);
            log.info("File deletion result: {} for {}", result, relativePath);
            return result;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", relativePath, e);
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
} 