package cn.mojoup.ai.upload.service.impl;

import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.service.FileInfoStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存实现的文件信息存储服务
 * 仅用于演示和开发测试，生产环境应使用数据库实现
 * 
 * @author matt
 */
@Slf4j
@Service
public class InMemoryFileInfoStorageService implements FileInfoStorageService {
    
    /**
     * 内存存储，key为fileId，value为FileInfo
     */
    private final Map<String, FileInfo> fileInfoStore = new ConcurrentHashMap<>();
    
    @Override
    public FileInfo saveFileInfo(FileInfo fileInfo) {
        if (fileInfo == null || !StringUtils.hasText(fileInfo.getFileId())) {
            throw new IllegalArgumentException("FileInfo or fileId cannot be null");
        }
        
        log.debug("Saving file info: {}", fileInfo.getFileId());
        fileInfoStore.put(fileInfo.getFileId(), fileInfo);
        return fileInfo;
    }
    
    @Override
    public List<FileInfo> saveFileInfos(List<FileInfo> fileInfos) {
        if (fileInfos == null || fileInfos.isEmpty()) {
            return List.of();
        }
        
        log.debug("Saving {} file infos", fileInfos.size());
        
        for (FileInfo fileInfo : fileInfos) {
            saveFileInfo(fileInfo);
        }
        
        return fileInfos;
    }
    
    @Override
    public Optional<FileInfo> findByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return Optional.empty();
        }
        
        FileInfo fileInfo = fileInfoStore.get(fileId);
        log.debug("Finding file info by id {}: {}", fileId, fileInfo != null ? "found" : "not found");
        
        return Optional.ofNullable(fileInfo);
    }
    
    @Override
    public Optional<FileInfo> findByRelativePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return Optional.empty();
        }
        
        log.debug("Finding file info by relative path: {}", relativePath);
        
        return fileInfoStore.values().stream()
                .filter(fileInfo -> relativePath.equals(fileInfo.getRelativePath()))
                .findFirst();
    }
    
    @Override
    public List<FileInfo> findByUploadUserId(String uploadUserId) {
        if (!StringUtils.hasText(uploadUserId)) {
            return List.of();
        }
        
        log.debug("Finding file infos by upload user id: {}", uploadUserId);
        
        return fileInfoStore.values().stream()
                .filter(fileInfo -> uploadUserId.equals(fileInfo.getUploadUserId()))
                .filter(fileInfo -> Boolean.TRUE.equals(fileInfo.getEnabled()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FileInfo> findByTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        
        log.debug("Finding file infos by tags: {}", tags);
        
        return fileInfoStore.values().stream()
                .filter(fileInfo -> tags.equals(fileInfo.getTags()))
                .filter(fileInfo -> Boolean.TRUE.equals(fileInfo.getEnabled()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean softDeleteByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        FileInfo fileInfo = fileInfoStore.get(fileId);
        if (fileInfo != null) {
            fileInfo.setEnabled(false);
            log.debug("Soft deleted file info: {}", fileId);
            return true;
        }
        
        log.debug("File info not found for soft delete: {}", fileId);
        return false;
    }
    
    @Override
    public boolean deleteByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        FileInfo removed = fileInfoStore.remove(fileId);
        boolean deleted = removed != null;
        
        log.debug("Physical deleted file info {}: {}", fileId, deleted ? "success" : "not found");
        return deleted;
    }
    
    @Override
    public boolean existsByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        boolean exists = fileInfoStore.containsKey(fileId);
        log.debug("File info exists check for {}: {}", fileId, exists);
        return exists;
    }
    
    @Override
    public FileInfo updateFileInfo(FileInfo fileInfo) {
        if (fileInfo == null || !StringUtils.hasText(fileInfo.getFileId())) {
            throw new IllegalArgumentException("FileInfo or fileId cannot be null");
        }
        
        if (!fileInfoStore.containsKey(fileInfo.getFileId())) {
            throw new IllegalArgumentException("FileInfo not found: " + fileInfo.getFileId());
        }
        
        log.debug("Updating file info: {}", fileInfo.getFileId());
        fileInfoStore.put(fileInfo.getFileId(), fileInfo);
        return fileInfo;
    }
    
    /**
     * 获取所有文件信息（用于调试）
     */
    public List<FileInfo> findAll() {
        log.debug("Finding all file infos, total: {}", fileInfoStore.size());
        return List.copyOf(fileInfoStore.values());
    }
    
    /**
     * 清空所有文件信息（用于测试）
     */
    public void clear() {
        log.debug("Clearing all file infos");
        fileInfoStore.clear();
    }
} 