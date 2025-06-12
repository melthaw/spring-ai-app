package cn.mojoup.ai.upload.controller;

import cn.mojoup.ai.upload.domain.UploadRequest;
import cn.mojoup.ai.upload.domain.UploadResponse;
import cn.mojoup.ai.upload.service.FileUploadService;
import cn.mojoup.ai.upload.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件上传控制器
 * 
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/single")
    @Operation(summary = "单个文件上传", description = "上传单个文件到存储系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<UploadResponse> uploadSingleFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件标签")
            @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "自定义存储路径")
            @RequestParam(value = "customPath", required = false) String customPath,
            @Parameter(description = "是否覆盖同名文件")
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
            @Parameter(description = "是否生成唯一文件名")
            @RequestParam(value = "generateUniqueName", defaultValue = "true") Boolean generateUniqueName) {
        
        // 从Spring Security获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        
        UploadRequest request = UploadRequest.builder()
                .uploadUserId(currentUserId)
                .tags(tags)
                .customPath(customPath)
                .overwrite(overwrite)
                .generateUniqueName(generateUniqueName)
                .build();
        
        UploadResponse response = fileUploadService.uploadSingleFile(file, request);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/multiple")
    @Operation(summary = "多个文件上传", description = "批量上传多个文件到存储系统")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<UploadResponse> uploadMultipleFiles(
            @Parameter(description = "上传的文件列表", required = true)
            @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "文件标签")
            @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "自定义存储路径")
            @RequestParam(value = "customPath", required = false) String customPath,
            @Parameter(description = "是否覆盖同名文件")
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
            @Parameter(description = "是否生成唯一文件名")
            @RequestParam(value = "generateUniqueName", defaultValue = "true") Boolean generateUniqueName) {
        
        // 从Spring Security获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        
        UploadRequest request = UploadRequest.builder()
                .uploadUserId(currentUserId)
                .tags(tags)
                .customPath(customPath)
                .overwrite(overwrite)
                .generateUniqueName(generateUniqueName)
                .build();
        
        List<MultipartFile> fileList = Arrays.asList(files);
        UploadResponse response = fileUploadService.uploadMultipleFiles(fileList, request);
        
        return ResponseEntity.ok(response);
    }
    

    
    @DeleteMapping("/id/{fileId}")
    @Operation(summary = "根据文件ID删除文件", description = "根据文件UUID删除文件（推荐使用）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> deleteFileById(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        try {
            boolean deleted = fileUploadService.deleteFileById(fileId);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "File deleted successfully",
                        "fileId", fileId
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "File not found or failed to delete",
                        "fileId", fileId
                ));
            }
            
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "fileId", fileId
            ));
        }
    }
    

    

    
    @GetMapping("/info/id/{fileId}")
    @Operation(summary = "根据文件ID获取文件信息", description = "根据文件UUID获取详细的文件信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getFileInfoById(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        try {
            Optional<cn.mojoup.ai.upload.domain.FileInfo> fileInfoOpt = fileUploadService.getFileInfoById(fileId);
            
            if (fileInfoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fileInfo", fileInfoOpt.get()
            ));
            
        } catch (Exception e) {
            log.error("Failed to get file info: {}", fileId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "fileId", fileId,
                    "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/download/id/{fileId}")
    @Operation(summary = "根据文件ID下载文件", description = "根据文件UUID下载文件（推荐使用）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Resource> downloadFileById(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            @Parameter(description = "是否以附件方式下载")
            @RequestParam(value = "attachment", defaultValue = "false") Boolean attachment) {
        
        try {
            Resource resource = fileUploadService.getFileResourceById(fileId);
            Optional<cn.mojoup.ai.upload.domain.FileInfo> fileInfoOpt = fileUploadService.getFileInfoById(fileId);
            
            if (resource == null || !resource.exists() || fileInfoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            cn.mojoup.ai.upload.domain.FileInfo fileInfo = fileInfoOpt.get();
            
            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            
            // 设置文件名（支持中文）
            String filename = fileInfo.getOriginalFileName();
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            
            if (attachment) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
            }
            
            // 设置内容类型
            headers.add(HttpHeaders.CONTENT_TYPE, 
                    fileInfo.getContentType() != null ? fileInfo.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode filename", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Failed to download file: {}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/exists/id/{fileId}")
    @Operation(summary = "根据文件ID检查文件是否存在", description = "根据文件UUID检查文件是否存在")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> checkFileExistsById(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        try {
            boolean exists = fileUploadService.fileExistsById(fileId);
            
            return ResponseEntity.ok(Map.of(
                    "exists", exists,
                    "fileId", fileId
            ));
            
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", fileId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "exists", false,
                    "fileId", fileId,
                    "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/url/id/{fileId}")
    @Operation(summary = "根据文件ID获取文件访问URL", description = "根据文件UUID获取文件的访问URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getFileUrlById(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        try {
            if (!fileUploadService.fileExistsById(fileId)) {
                return ResponseEntity.notFound().build();
            }
            
            String url = fileUploadService.getFileUrlById(fileId);
            
            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "fileId", fileId
            ));
            
        } catch (Exception e) {
            log.error("Failed to get file URL: {}", fileId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "fileId", fileId,
                    "error", e.getMessage()
            ));
        }
    }
    

    
    /**
     * 从路径中提取文件名
     */
    private String getFilenameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "unknown";
        }
        
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
} 