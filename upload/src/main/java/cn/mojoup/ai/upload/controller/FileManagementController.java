package cn.mojoup.ai.upload.controller;

import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.service.impl.JpaFileInfoStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器（JPA扩展功能）
 * 仅在使用JPA存储服务时可用
 * 
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/files/management")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件管理相关接口")
public class FileManagementController {
    
    private final JpaFileInfoStorageService jpaFileInfoStorageService;
    
    @GetMapping("/search")
    @Operation(summary = "搜索文件", description = "根据关键词搜索文件")
    public ResponseEntity<Page<FileInfo>> searchFiles(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching files with keyword: {}, page: {}, size: {}", keyword, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileInfo> result = jpaFileInfoStorageService.searchFiles(keyword, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/advanced-search")
    @Operation(summary = "高级搜索文件", description = "根据多个条件搜索文件")
    public ResponseEntity<Page<FileInfo>> advancedSearch(
            @Parameter(description = "文件名") @RequestParam(required = false) String fileName,
            @Parameter(description = "文件类型") @RequestParam(required = false) String contentType,
            @Parameter(description = "上传用户ID") @RequestParam(required = false) String uploadUserId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "最小文件大小") @RequestParam(required = false) Long minSize,
            @Parameter(description = "最大文件大小") @RequestParam(required = false) Long maxSize,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Advanced searching files with conditions");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileInfo> result = jpaFileInfoStorageService.advancedSearch(
                fileName, contentType, uploadUserId, startTime, endTime, minSize, maxSize, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "分页查询用户文件", description = "分页查询指定用户的文件")
    public ResponseEntity<Page<FileInfo>> getUserFiles(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting files for user: {}, page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileInfo> result = jpaFileInfoStorageService.findByUploadUserId(userId, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/statistics/{userId}")
    @Operation(summary = "用户文件统计", description = "获取用户文件统计信息")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        
        log.info("Getting statistics for user: {}", userId);
        
        long fileCount = jpaFileInfoStorageService.countByUploadUserId(userId);
        long totalSize = jpaFileInfoStorageService.sumFileSizeByUploadUserId(userId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("userId", userId);
        statistics.put("fileCount", fileCount);
        statistics.put("totalSize", totalSize);
        statistics.put("averageSize", fileCount > 0 ? totalSize / fileCount : 0);
        
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/duplicates/{md5Hash}")
    @Operation(summary = "查找重复文件", description = "根据MD5哈希值查找重复文件")
    public ResponseEntity<List<FileInfo>> findDuplicates(
            @Parameter(description = "MD5哈希值") @PathVariable String md5Hash) {
        
        log.info("Finding duplicates for MD5: {}", md5Hash);
        
        List<FileInfo> duplicates = jpaFileInfoStorageService.findDuplicatesByMd5(md5Hash);
        
        return ResponseEntity.ok(duplicates);
    }
    
    @PostMapping("/batch-delete")
    @Operation(summary = "批量软删除文件", description = "批量软删除多个文件")
    public ResponseEntity<Map<String, Object>> batchSoftDelete(
            @Parameter(description = "文件ID列表") @RequestBody List<String> fileIds) {
        
        log.info("Batch soft deleting {} files", fileIds.size());
        
        int deletedCount = jpaFileInfoStorageService.batchSoftDelete(fileIds);
        
        Map<String, Object> result = new HashMap<>();
        result.put("requestedCount", fileIds.size());
        result.put("deletedCount", deletedCount);
        result.put("success", deletedCount > 0);
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/restore/{fileId}")
    @Operation(summary = "恢复文件", description = "恢复软删除的文件")
    public ResponseEntity<Map<String, Object>> restoreFile(
            @Parameter(description = "文件ID") @PathVariable String fileId) {
        
        log.info("Restoring file: {}", fileId);
        
        boolean restored = jpaFileInfoStorageService.restoreFileInfo(fileId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", fileId);
        result.put("restored", restored);
        result.put("message", restored ? "文件恢复成功" : "文件不存在或恢复失败");
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期文件", description = "清理指定时间之前的过期文件")
    public ResponseEntity<Map<String, Object>> cleanupExpiredFiles(
            @Parameter(description = "过期时间") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expireTime) {
        
        log.info("Cleaning up expired files before: {}", expireTime);
        
        List<FileInfo> expiredFiles = jpaFileInfoStorageService.cleanupExpiredFiles(expireTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("expireTime", expireTime);
        result.put("cleanedCount", expiredFiles.size());
        result.put("cleanedFiles", expiredFiles.stream().map(FileInfo::getFileId).toList());
        
        return ResponseEntity.ok(result);
    }
} 