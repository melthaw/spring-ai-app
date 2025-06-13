package cn.mojoup.ai.upload.service.impl;

import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.repository.FileInfoRepository;
import cn.mojoup.ai.upload.service.FileInfoStorageService;
import cn.mojoup.ai.upload.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基于JPA的文件信息存储服务实现
 * 
 * @author matt
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaFileInfoStorageService implements FileInfoStorageService {
    
    private final FileInfoRepository fileInfoRepository;
    
    @Override
    @Transactional
    public FileInfo saveFileInfo(FileInfo fileInfo) {
        if (fileInfo == null || !StringUtils.hasText(fileInfo.getFileId())) {
            throw new IllegalArgumentException("FileInfo or fileId cannot be null");
        }
        
        log.debug("Saving file info to database: {}", fileInfo.getFileId());
        
        try {
            FileInfo savedFileInfo = fileInfoRepository.save(fileInfo);
            
            log.debug("File info saved successfully: {}", savedFileInfo.getFileId());
            return savedFileInfo;
            
        } catch (Exception e) {
            log.error("Failed to save file info: {}", fileInfo.getFileId(), e);
            throw new RuntimeException("Failed to save file info", e);
        }
    }
    
    @Override
    @Transactional
    public List<FileInfo> saveFileInfos(List<FileInfo> fileInfos) {
        if (fileInfos == null || fileInfos.isEmpty()) {
            return List.of();
        }
        
        log.debug("Saving {} file infos to database", fileInfos.size());
        
        try {
            List<FileInfo> savedFileInfos = fileInfoRepository.saveAll(fileInfos);
            
            log.debug("Batch saved {} file infos successfully", savedFileInfos.size());
            
            return savedFileInfos;
                    
        } catch (Exception e) {
            log.error("Failed to batch save file infos", e);
            throw new RuntimeException("Failed to batch save file infos", e);
        }
    }
    
    @Override
    public Optional<FileInfo> findByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return Optional.empty();
        }
        
        log.debug("Finding file info by id: {}", fileId);
        
        try {
            Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByFileIdAndEnabledTrue(fileId);
            
            if (fileInfoOpt.isPresent()) {
                log.debug("File info found: {}", fileId);
                return fileInfoOpt;
            } else {
                log.debug("File info not found: {}", fileId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Failed to find file info by id: {}", fileId, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<FileInfo> findByRelativePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return Optional.empty();
        }
        
        log.debug("Finding file info by relative path: {}", relativePath);
        
        try {
            Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByRelativePathAndEnabledTrue(relativePath);
            
            if (fileInfoOpt.isPresent()) {
                log.debug("File info found by path: {}", relativePath);
                return fileInfoOpt;
            } else {
                log.debug("File info not found by path: {}", relativePath);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Failed to find file info by path: {}", relativePath, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<FileInfo> findByUploadUserId(String uploadUserId) {
        if (!StringUtils.hasText(uploadUserId)) {
            return List.of();
        }
        
        log.debug("Finding file infos by upload user id: {}", uploadUserId);
        
        try {
            List<FileInfo> fileInfos = fileInfoRepository.findByUploadUserIdAndEnabledTrueOrderByUploadTimeDesc(uploadUserId);
            
            log.debug("Found {} file infos for user: {}", fileInfos.size(), uploadUserId);
            
            return fileInfos;
                    
        } catch (Exception e) {
            log.error("Failed to find file infos by user id: {}", uploadUserId, e);
            return List.of();
        }
    }
    
    @Override
    public List<FileInfo> findByTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        
        log.debug("Finding file infos by tags: {}", tags);
        
        try {
            List<FileInfo> fileInfos = fileInfoRepository.findByTagsContainingAndEnabledTrueOrderByUploadTimeDesc(tags);
            
            log.debug("Found {} file infos with tags: {}", fileInfos.size(), tags);
            
            return fileInfos;
                    
        } catch (Exception e) {
            log.error("Failed to find file infos by tags: {}", tags, e);
            return List.of();
        }
    }
    
    @Override
    @Transactional
    public boolean softDeleteByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        log.debug("Soft deleting file info: {}", fileId);
        
        try {
            String currentUserId = getCurrentUserId();
            int updatedRows = fileInfoRepository.softDeleteByFileId(fileId, LocalDateTime.now(), currentUserId);
            
            boolean deleted = updatedRows > 0;
            log.debug("Soft deleted file info {}: {}", fileId, deleted ? "success" : "not found");
            
            return deleted;
            
        } catch (Exception e) {
            log.error("Failed to soft delete file info: {}", fileId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        log.debug("Physical deleting file info: {}", fileId);
        
        try {
            Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByFileId(fileId);
            
            if (fileInfoOpt.isPresent()) {
                fileInfoRepository.deleteByFileId(fileId);
                log.debug("Physical deleted file info: {}", fileId);
                return true;
            } else {
                log.debug("File info not found for deletion: {}", fileId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to physical delete file info: {}", fileId, e);
            return false;
        }
    }
    
    @Override
    public boolean existsByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        try {
            boolean exists = fileInfoRepository.existsByFileIdAndEnabledTrue(fileId);
            log.debug("File info exists check for {}: {}", fileId, exists);
            return exists;
            
        } catch (Exception e) {
            log.error("Failed to check file info existence: {}", fileId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public FileInfo updateFileInfo(FileInfo fileInfo) {
        if (fileInfo == null || !StringUtils.hasText(fileInfo.getFileId())) {
            throw new IllegalArgumentException("FileInfo or fileId cannot be null");
        }
        
        log.debug("Updating file info: {}", fileInfo.getFileId());
        
        try {
            Optional<FileInfo> existingFileInfoOpt = fileInfoRepository.findByFileId(fileInfo.getFileId());
            
            if (existingFileInfoOpt.isEmpty()) {
                throw new IllegalArgumentException("FileInfo not found: " + fileInfo.getFileId());
            }
            
            FileInfo existingFileInfo = existingFileInfoOpt.get();
            String currentUserId = getCurrentUserId();
            
            // 更新允许修改的字段
            if (fileInfo.getAccessUrl() != null) {
                existingFileInfo.setAccessUrl(fileInfo.getAccessUrl());
            }
            if (fileInfo.getTags() != null) {
                existingFileInfo.setTags(fileInfo.getTags());
            }
            if (fileInfo.getMetadata() != null) {
                existingFileInfo.setMetadata(fileInfo.getMetadata());
            }
            if (fileInfo.getEnabled() != null) {
                existingFileInfo.setEnabled(fileInfo.getEnabled());
            }
            
            existingFileInfo.setUpdatedBy(currentUserId);
            
            FileInfo updatedFileInfo = fileInfoRepository.save(existingFileInfo);
            
            log.debug("File info updated successfully: {}", fileInfo.getFileId());
            return updatedFileInfo;
            
        } catch (Exception e) {
            log.error("Failed to update file info: {}", fileInfo.getFileId(), e);
            throw new RuntimeException("Failed to update file info", e);
        }
    }
    
    // ==================== 扩展方法 ====================
    
    /**
     * 分页查询用户的文件信息
     */
    public Page<FileInfo> findByUploadUserId(String uploadUserId, Pageable pageable) {
        if (!StringUtils.hasText(uploadUserId)) {
            return Page.empty();
        }
        
        log.debug("Finding file infos by user id with pagination: {}", uploadUserId);
        
        try {
            Page<FileInfo> fileInfoPage = fileInfoRepository.findByUploadUserIdAndEnabledTrueOrderByUploadTimeDesc(uploadUserId, pageable);
            
            return fileInfoPage;
            
        } catch (Exception e) {
            log.error("Failed to find file infos by user id with pagination: {}", uploadUserId, e);
            return Page.empty();
        }
    }
    
    /**
     * 搜索文件信息
     */
    public Page<FileInfo> searchFiles(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return Page.empty();
        }
        
        log.debug("Searching file infos with keyword: {}", keyword);
        
        try {
            Page<FileInfo> fileInfoPage = fileInfoRepository.searchFiles(keyword, pageable);
            
            return fileInfoPage;
            
        } catch (Exception e) {
            log.error("Failed to search file infos with keyword: {}", keyword, e);
            return Page.empty();
        }
    }
    
    /**
     * 高级搜索文件信息
     */
    public Page<FileInfo> advancedSearch(String fileName, String contentType, String uploadUserId,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        Long minSize, Long maxSize, Pageable pageable) {
        log.debug("Advanced searching file infos");
        
        try {
            Page<FileInfo> fileInfoPage = fileInfoRepository.advancedSearch(
                    fileName, contentType, uploadUserId, startTime, endTime, minSize, maxSize, pageable);
            
            return fileInfoPage;
            
        } catch (Exception e) {
            log.error("Failed to advanced search file infos", e);
            return Page.empty();
        }
    }
    
    /**
     * 统计用户上传的文件数量
     */
    public long countByUploadUserId(String uploadUserId) {
        if (!StringUtils.hasText(uploadUserId)) {
            return 0;
        }
        
        try {
            return fileInfoRepository.countByUploadUserId(uploadUserId);
        } catch (Exception e) {
            log.error("Failed to count files by user id: {}", uploadUserId, e);
            return 0;
        }
    }
    
    /**
     * 统计用户上传的文件总大小
     */
    public long sumFileSizeByUploadUserId(String uploadUserId) {
        if (!StringUtils.hasText(uploadUserId)) {
            return 0;
        }
        
        try {
            return fileInfoRepository.sumFileSizeByUploadUserId(uploadUserId);
        } catch (Exception e) {
            log.error("Failed to sum file size by user id: {}", uploadUserId, e);
            return 0;
        }
    }
    
    /**
     * 查找重复的文件（基于MD5）
     */
    public List<FileInfo> findDuplicatesByMd5(String md5Hash) {
        if (!StringUtils.hasText(md5Hash)) {
            return List.of();
        }
        
        try {
            List<FileInfo> fileInfos = fileInfoRepository.findByMd5Hash(md5Hash);
            
            return fileInfos;
                    
        } catch (Exception e) {
            log.error("Failed to find duplicates by MD5: {}", md5Hash, e);
            return List.of();
        }
    }
    
    /**
     * 批量软删除文件信息
     */
    @Transactional
    public int batchSoftDelete(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return 0;
        }
        
        log.debug("Batch soft deleting {} file infos", fileIds.size());
        
        try {
            String currentUserId = getCurrentUserId();
            int updatedRows = fileInfoRepository.softDeleteByFileIds(fileIds, LocalDateTime.now(), currentUserId);
            
            log.debug("Batch soft deleted {} file infos", updatedRows);
            return updatedRows;
            
        } catch (Exception e) {
            log.error("Failed to batch soft delete file infos", e);
            return 0;
        }
    }
    
    /**
     * 恢复软删除的文件信息
     */
    @Transactional
    public boolean restoreFileInfo(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            return false;
        }
        
        log.debug("Restoring file info: {}", fileId);
        
        try {
            String currentUserId = getCurrentUserId();
            int updatedRows = fileInfoRepository.restoreByFileId(fileId, LocalDateTime.now(), currentUserId);
            
            boolean restored = updatedRows > 0;
            log.debug("Restored file info {}: {}", fileId, restored ? "success" : "not found");
            
            return restored;
            
        } catch (Exception e) {
            log.error("Failed to restore file info: {}", fileId, e);
            return false;
        }
    }
    
    /**
     * 清理过期文件
     */
    @Transactional
    public List<FileInfo> cleanupExpiredFiles(LocalDateTime expireTime) {
        log.debug("Cleaning up expired files before: {}", expireTime);
        
        try {
            List<FileInfo> expiredFileInfos = fileInfoRepository.findExpiredFiles(expireTime);
            
            if (!expiredFileInfos.isEmpty()) {
                List<String> expiredFileIds = expiredFileInfos.stream()
                        .map(FileInfo::getFileId)
                        .collect(Collectors.toList());
                
                batchSoftDelete(expiredFileIds);
                
                log.info("Cleaned up {} expired files", expiredFileInfos.size());
            }
            
            return expiredFileInfos;
                    
        } catch (Exception e) {
            log.error("Failed to cleanup expired files", e);
            return List.of();
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            return SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            log.warn("Failed to get current user id, using default", e);
            return "system";
        }
    }
} 