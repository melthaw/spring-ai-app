package cn.mojoup.ai.kb.controller;

import cn.mojoup.ai.kb.dto.CreateDocumentFromUploadRequest;
import cn.mojoup.ai.kb.dto.UpdateDocumentFileRequest;
import cn.mojoup.ai.kb.entity.DocumentNode;
import cn.mojoup.ai.kb.service.DocumentNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 文档上传集成控制器
 * 处理从上传文件创建文档节点的相关功能
 *
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/kb/{kbId}/documents")
@RequiredArgsConstructor
@Tag(name = "文档上传集成", description = "文档节点与文件上传的集成功能")
public class DocumentUploadController {

    private final DocumentNodeService documentNodeService;

    @PostMapping("/upload")
    @Operation(summary = "从上传文件创建文档节点", description = "基于已上传的文件创建新的文档节点")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "404", description = "文件或知识库不存在")
    })
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#kbId, authentication.name, 'CREATE_DOCUMENT')")
    public ResponseEntity<DocumentNode> createDocumentFromUpload(
            @Parameter(description = "知识库ID") @PathVariable String kbId,
            @Parameter(description = "父节点ID（可选）") @RequestParam(required = false) String parentId,
            @Valid @RequestBody CreateDocumentFromUploadRequest request) {
        
        log.info("创建文档节点从上传文件: kbId={}, parentId={}, fileId={}", 
                kbId, parentId, request.getFileId());
        
        DocumentNode documentNode = documentNodeService.createDocumentFromUpload(kbId, parentId, request);
        return ResponseEntity.ok(documentNode);
    }

    @PutMapping("/{nodeId}/file")
    @Operation(summary = "更新文档节点的文件", description = "替换文档节点关联的文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "404", description = "文档节点或文件不存在")
    })
    @PreAuthorize("@permissionService.checkDocumentNodeAccess(#nodeId, authentication.name, 'UPDATE')")
    public ResponseEntity<DocumentNode> updateDocumentFile(
            @Parameter(description = "知识库ID") @PathVariable String kbId,
            @Parameter(description = "文档节点ID") @PathVariable String nodeId,
            @Valid @RequestBody UpdateDocumentFileRequest request) {
        
        log.info("更新文档节点文件: nodeId={}, newFileId={}", nodeId, request.getNewFileId());
        
        DocumentNode documentNode = documentNodeService.updateDocumentFile(nodeId, request);
        return ResponseEntity.ok(documentNode);
    }

    @PostMapping("/batch-upload")
    @Operation(summary = "批量从上传文件创建文档节点", description = "基于多个已上传的文件批量创建文档节点")
    @PreAuthorize("@permissionService.checkKnowledgeBaseAccess(#kbId, authentication.name, 'CREATE_DOCUMENT')")
    public ResponseEntity<BatchCreateDocumentResult> batchCreateDocumentsFromUpload(
            @Parameter(description = "知识库ID") @PathVariable String kbId,
            @Parameter(description = "父节点ID（可选）") @RequestParam(required = false) String parentId,
            @Valid @RequestBody BatchCreateDocumentFromUploadRequest request) {
        
        log.info("批量创建文档节点从上传文件: kbId={}, parentId={}, fileCount={}", 
                kbId, parentId, request.getFileIds().size());
        
        // 这里需要实现批量创建的逻辑
        throw new UnsupportedOperationException("批量创建功能待实现");
    }

    /**
     * 批量创建文档请求DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchCreateDocumentFromUploadRequest {
        private java.util.List<String> fileIds;
        private String description;
        private String tags;
        private DocumentNode.AccessLevel accessLevel;
    }

    /**
     * 批量创建文档结果DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchCreateDocumentResult {
        private Integer totalCount;
        private Integer successCount;
        private Integer failureCount;
        private java.util.List<DocumentNode> successNodes;
        private java.util.List<String> failureReasons;
    }
} 