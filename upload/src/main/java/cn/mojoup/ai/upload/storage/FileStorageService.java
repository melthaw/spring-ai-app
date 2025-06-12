package cn.mojoup.ai.upload.storage;

import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.domain.UploadRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件存储服务接口
 * 
 * @author matt
 */
public interface FileStorageService {
    
    /**
     * 上传单个文件
     * 
     * @param file 上传的文件
     * @param request 上传请求参数
     * @return 文件信息
     */
    FileInfo uploadFile(MultipartFile file, UploadRequest request);
    
    /**
     * 上传多个文件
     * 
     * @param files 上传的文件列表
     * @param request 上传请求参数
     * @return 文件信息列表
     */
    List<FileInfo> uploadFiles(List<MultipartFile> files, UploadRequest request);
    
    /**
     * 根据相对路径获取文件资源
     * 
     * @param relativePath 相对路径
     * @return Spring Resource
     */
    Resource getResource(String relativePath);
    
    /**
     * 删除文件
     * 
     * @param relativePath 相对路径
     * @return 是否成功
     */
    boolean deleteFile(String relativePath);
    
    /**
     * 检查文件是否存在
     * 
     * @param relativePath 相对路径
     * @return 是否存在
     */
    boolean fileExists(String relativePath);
    
    /**
     * 获取文件访问URL
     * 
     * @param relativePath 相对路径
     * @return 访问URL
     */
    String getFileUrl(String relativePath);
    
    /**
     * 验证文件
     * 
     * @param file 上传的文件
     */
    void validateFile(MultipartFile file);
} 