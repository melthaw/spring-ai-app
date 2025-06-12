package cn.mojoup.ai.upload.controller;

import cn.mojoup.ai.upload.domain.UploadRequest;
import cn.mojoup.ai.upload.domain.UploadResponse;
import cn.mojoup.ai.upload.service.FileUploadService;
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
            @Parameter(description = "上传用户ID")
            @RequestParam(value = "uploadUserId", required = false) String uploadUserId,
            @Parameter(description = "文件标签")
            @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "自定义存储路径")
            @RequestParam(value = "customPath", required = false) String customPath,
            @Parameter(description = "是否覆盖同名文件")
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
            @Parameter(description = "是否生成唯一文件名")
            @RequestParam(value = "generateUniqueName", defaultValue = "true") Boolean generateUniqueName) {
        
        UploadRequest request = UploadRequest.builder()
                .uploadUserId(uploadUserId)
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
            @Parameter(description = "上传用户ID")
            @RequestParam(value = "uploadUserId", required = false) String uploadUserId,
            @Parameter(description = "文件标签")
            @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "自定义存储路径")
            @RequestParam(value = "customPath", required = false) String customPath,
            @Parameter(description = "是否覆盖同名文件")
            @RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
            @Parameter(description = "是否生成唯一文件名")
            @RequestParam(value = "generateUniqueName", defaultValue = "true") Boolean generateUniqueName) {
        
        UploadRequest request = UploadRequest.builder()
                .uploadUserId(uploadUserId)
                .tags(tags)
                .customPath(customPath)
                .overwrite(overwrite)
                .generateUniqueName(generateUniqueName)
                .build();
        
        List<MultipartFile> fileList = Arrays.asList(files);
        UploadResponse response = fileUploadService.uploadMultipleFiles(fileList, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/download/{**relativePath}")
    @Operation(summary = "文件下载", description = "根据相对路径下载文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件相对路径", required = true)
            @PathVariable String relativePath,
            @Parameter(description = "是否以附件方式下载")
            @RequestParam(value = "attachment", defaultValue = "false") Boolean attachment) {
        
        try {
            Resource resource = fileUploadService.getFileResource(relativePath);
            
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            
            // 设置文件名（支持中文）
            String filename = getFilenameFromPath(relativePath);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            
            if (attachment) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename);
            }
            
            // 设置内容类型
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode filename", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Failed to download file: {}", relativePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{**relativePath}")
    @Operation(summary = "删除文件", description = "根据相对路径删除文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> deleteFile(
            @Parameter(description = "文件相对路径", required = true)
            @PathVariable String relativePath) {
        
        try {
            boolean deleted = fileUploadService.deleteFile(relativePath);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "File deleted successfully",
                        "path", relativePath
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "File not found or failed to delete",
                        "path", relativePath
                ));
            }
            
        } catch (Exception e) {
            log.error("Failed to delete file: {}", relativePath, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error: " + e.getMessage(),
                    "path", relativePath
            ));
        }
    }
    
    @GetMapping("/exists/{**relativePath}")
    @Operation(summary = "检查文件是否存在", description = "根据相对路径检查文件是否存在")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @Parameter(description = "文件相对路径", required = true)
            @PathVariable String relativePath) {
        
        try {
            boolean exists = fileUploadService.fileExists(relativePath);
            
            return ResponseEntity.ok(Map.of(
                    "exists", exists,
                    "path", relativePath
            ));
            
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", relativePath, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "exists", false,
                    "path", relativePath,
                    "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/url/{**relativePath}")
    @Operation(summary = "获取文件访问URL", description = "根据相对路径获取文件的访问URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> getFileUrl(
            @Parameter(description = "文件相对路径", required = true)
            @PathVariable String relativePath) {
        
        try {
            if (!fileUploadService.fileExists(relativePath)) {
                return ResponseEntity.notFound().build();
            }
            
            String url = fileUploadService.getFileUrl(relativePath);
            
            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "path", relativePath
            ));
            
        } catch (Exception e) {
            log.error("Failed to get file URL: {}", relativePath, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "path", relativePath,
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