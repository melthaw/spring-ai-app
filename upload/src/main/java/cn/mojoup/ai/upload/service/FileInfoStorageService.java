package cn.mojoup.ai.upload.service;

import cn.mojoup.ai.upload.domain.FileInfo;

import java.util.List;
import java.util.Optional;

/**
 * 文件信息存储服务接口
 * 用于将文件信息持久化到数据库或其他存储介质
 * 
 * @author matt
 */
public interface FileInfoStorageService {
    
    /**
     * 保存文件信息
     * 
     * @param fileInfo 文件信息
     * @return 保存后的文件信息
     */
    FileInfo saveFileInfo(FileInfo fileInfo);
    
    /**
     * 批量保存文件信息
     * 
     * @param fileInfos 文件信息列表
     * @return 保存后的文件信息列表
     */
    List<FileInfo> saveFileInfos(List<FileInfo> fileInfos);
    
    /**
     * 根据文件ID查询文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    Optional<FileInfo> findByFileId(String fileId);
    
    /**
     * 根据相对路径查询文件信息
     * 
     * @param relativePath 相对路径
     * @return 文件信息
     */
    Optional<FileInfo> findByRelativePath(String relativePath);
    
    /**
     * 根据上传用户ID查询文件列表
     * 
     * @param uploadUserId 上传用户ID
     * @return 文件信息列表
     */
    List<FileInfo> findByUploadUserId(String uploadUserId);
    
    /**
     * 根据标签查询文件列表
     * 
     * @param tags 标签
     * @return 文件信息列表
     */
    List<FileInfo> findByTags(String tags);
    
    /**
     * 软删除文件信息（设置enabled=false）
     * 
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean softDeleteByFileId(String fileId);
    
    /**
     * 物理删除文件信息
     * 
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean deleteByFileId(String fileId);
    
    /**
     * 检查文件是否存在
     * 
     * @param fileId 文件ID
     * @return 是否存在
     */
    boolean existsByFileId(String fileId);
    
    /**
     * 更新文件信息
     * 
     * @param fileInfo 文件信息
     * @return 更新后的文件信息
     */
    FileInfo updateFileInfo(FileInfo fileInfo);
} 