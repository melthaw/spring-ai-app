package cn.mojoup.ai.knowledgebase.controller;

import cn.mojoup.ai.knowledgebase.domain.DocumentUploadRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument;
import cn.mojoup.ai.knowledgebase.domain.DocumentEmbedding;
import cn.mojoup.ai.knowledgebase.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 知识库文档管理控制器
 * 整合upload模块和rag模块的能力
 * 
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge-base/documents")
@RequiredArgsConstructor
@Validated
@Tag(name = "知识库文档管理", description = "知识库文档上传、管理和嵌入相关接口")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;

    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "上传文档到知识库，支持自动嵌入处理")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "413", description = "文件过大")
    })
    public ResponseEntity<KnowledgeDocument> uploadDocument(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档标题") @RequestParam("title") String title,
            @Parameter(description = "文档描述") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "分类ID") @RequestParam("categoryId") Long categoryId,
            @Parameter(description = "文档标签") @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "访问级别") @RequestParam(value = "accessLevel", defaultValue = "PRIVATE") String accessLevel,
            @Parameter(description = "排序顺序") @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder,
            @Parameter(description = "上传用户ID") @RequestParam("uploadUserId") String uploadUserId,
            @Parameter(description = "是否立即嵌入") @RequestParam(value = "immediateEmbedding", defaultValue = "true") Boolean immediateEmbedding,
            @Parameter(description = "嵌入模型") @RequestParam(value = "embeddingModel", defaultValue = "text-embedding-ada-002") String embeddingModel,
            @Parameter(description = "分块策略") @RequestParam(value = "chunkStrategy", defaultValue = "FIXED_SIZE") String chunkStrategy,
            @Parameter(description = "分块大小") @RequestParam(value = "chunkSize", defaultValue = "1000") Integer chunkSize,
            @Parameter(description = "分块重叠") @RequestParam(value = "chunkOverlap", defaultValue = "200") Integer chunkOverlap) {

        log.info("上传文档: title={}, filename={}, categoryId={}", title, file.getOriginalFilename(), categoryId);

        try {
            // 构建上传请求
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setCategoryId(categoryId);
            request.setTags(tags);
            request.setAccessLevel(KnowledgeDocument.AccessLevel.valueOf(accessLevel));
            request.setSortOrder(sortOrder);
            request.setUploadUserId(uploadUserId);
            request.setImmediateEmbedding(immediateEmbedding);
            request.setEmbeddingModel(embeddingModel);
            request.setChunkStrategy(DocumentEmbedding.ChunkStrategy.valueOf(chunkStrategy));
            request.setChunkSize(chunkSize);
            request.setChunkOverlap(chunkOverlap);

            KnowledgeDocument document = documentService.uploadDocument(file, request);
            return ResponseEntity.ok(document);

        } catch (IllegalArgumentException e) {
            log.error("文档上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("文档上传失败: title={}", title, e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "文档上传失败: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文档", description = "批量上传多个文档到知识库")
    public ResponseEntity<List<KnowledgeDocument>> uploadDocuments(
            @Parameter(description = "上传的文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "基础标题") @RequestParam("title") String title,
            @Parameter(description = "文档描述") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "分类ID") @RequestParam("categoryId") Long categoryId,
            @Parameter(description = "上传用户ID") @RequestParam("uploadUserId") String uploadUserId,
            @Parameter(description = "是否立即嵌入") @RequestParam(value = "immediateEmbedding", defaultValue = "true") Boolean immediateEmbedding) {

        log.info("批量上传文档: fileCount={}, categoryId={}", files.size(), categoryId);

        try {
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setCategoryId(categoryId);
            request.setUploadUserId(uploadUserId);
            request.setImmediateEmbedding(immediateEmbedding);

            List<KnowledgeDocument> documents = documentService.uploadDocuments(files, request);
            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            log.error("批量上传文档失败", e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "批量上传失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文档详情", description = "根据ID获取文档详细信息")
    public ResponseEntity<KnowledgeDocument> getDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {

        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档", description = "逻辑删除文档，同时删除文件和向量数据")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {

        log.info("删除文档: id={}, operatorId={}", id, operatorId);

        try {
            documentService.deleteDocument(id, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文档删除成功",
                    "documentId", id
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("删除文档失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "删除文档失败: " + e.getMessage(),
                            "documentId", id
                    ));
        }
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取分类下的文档", description = "分页获取指定分类下的文档列表")
    public ResponseEntity<Page<KnowledgeDocument>> getDocumentsByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeDocument> documents = documentService.getDocumentsByCategory(categoryId, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/knowledge-base/{kbId}")
    @Operation(summary = "获取知识库下的文档", description = "分页获取指定知识库下的文档列表")
    public ResponseEntity<Page<KnowledgeDocument>> getDocumentsByKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeDocument> documents = documentService.getDocumentsByKnowledgeBase(kbId, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索文档", description = "根据关键词搜索文档")
    public ResponseEntity<Page<KnowledgeDocument>> searchDocuments(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeDocument> result = documentService.searchDocuments(keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reprocess")
    @Operation(summary = "重新处理文档", description = "重新解析和嵌入文档")
    public ResponseEntity<Map<String, Object>> reprocessDocument(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "嵌入模型") @RequestParam(value = "embeddingModel", defaultValue = "text-embedding-ada-002") String embeddingModel,
            @Parameter(description = "分块策略") @RequestParam(value = "chunkStrategy", defaultValue = "FIXED_SIZE") String chunkStrategy,
            @Parameter(description = "分块大小") @RequestParam(value = "chunkSize", defaultValue = "1000") Integer chunkSize,
            @Parameter(description = "分块重叠") @RequestParam(value = "chunkOverlap", defaultValue = "200") Integer chunkOverlap) {

        log.info("重新处理文档: id={}, model={}", id, embeddingModel);

        try {
            DocumentUploadRequest embeddingConfig = new DocumentUploadRequest();
            embeddingConfig.setEmbeddingModel(embeddingModel);
            embeddingConfig.setChunkStrategy(DocumentEmbedding.ChunkStrategy.valueOf(chunkStrategy));
            embeddingConfig.setChunkSize(chunkSize);
            embeddingConfig.setChunkOverlap(chunkOverlap);

            documentService.reprocessDocument(id, embeddingConfig);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文档重新处理已提交",
                    "documentId", id
            ));

        } catch (Exception e) {
            log.error("重新处理文档失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "重新处理失败: " + e.getMessage(),
                            "documentId", id
                    ));
        }
    }

    @GetMapping("/{id}/embeddings")
    @Operation(summary = "获取文档嵌入列表", description = "获取文档的所有嵌入结果")
    public ResponseEntity<List<DocumentEmbedding>> getDocumentEmbeddings(
            @Parameter(description = "文档ID") @PathVariable Long id) {

        List<DocumentEmbedding> embeddings = documentService.getDocumentEmbeddings(id);
        return ResponseEntity.ok(embeddings);
    }

    @PostMapping("/{id}/embeddings")
    @Operation(summary = "创建文档嵌入", description = "为文档创建新的嵌入")
    public ResponseEntity<DocumentEmbedding> createDocumentEmbedding(
            @Parameter(description = "文档ID") @PathVariable Long id,
            @Parameter(description = "嵌入模型") @RequestParam("embeddingModel") String embeddingModel,
            @Parameter(description = "分块策略") @RequestParam(value = "chunkStrategy", defaultValue = "FIXED_SIZE") String chunkStrategy,
            @Parameter(description = "分块大小") @RequestParam(value = "chunkSize", defaultValue = "1000") Integer chunkSize,
            @Parameter(description = "分块重叠") @RequestParam(value = "chunkOverlap", defaultValue = "200") Integer chunkOverlap) {

        try {
            DocumentEmbedding embedding = documentService.createDocumentEmbedding(
                    id, embeddingModel,
                    DocumentEmbedding.ChunkStrategy.valueOf(chunkStrategy),
                    chunkSize, chunkOverlap);

            return ResponseEntity.ok(embedding);

        } catch (Exception e) {
            log.error("创建文档嵌入失败: documentId={}", id, e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "创建嵌入失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "获取文档处理状态", description = "获取文档的当前处理状态")
    public ResponseEntity<Map<String, Object>> getDocumentStatus(
            @Parameter(description = "文档ID") @PathVariable Long id) {

        try {
            KnowledgeDocument.DocumentStatus status = documentService.getDocumentStatus(id);
            return ResponseEntity.ok(Map.of(
                    "documentId", id,
                    "status", status,
                    "statusName", status != null ? status.name() : "UNKNOWN"
            ));

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats/knowledge-base/{kbId}")
    @Operation(summary = "获取知识库文档统计", description = "获取知识库的文档统计信息")
    public ResponseEntity<KnowledgeDocumentService.DocumentStats> getKnowledgeBaseDocumentStats(
            @Parameter(description = "知识库ID") @PathVariable Long kbId) {

        try {
            KnowledgeDocumentService.DocumentStats stats = documentService.getDocumentStats(kbId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("获取知识库文档统计失败: kbId={}", kbId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/failed")
    @Operation(summary = "获取处理失败的文档", description = "获取处理失败的文档列表")
    public ResponseEntity<List<KnowledgeDocument>> getFailedDocuments() {
        List<KnowledgeDocument> failedDocuments = documentService.getFailedDocuments();
        return ResponseEntity.ok(failedDocuments);
    }

    @PostMapping("/failed/retry")
    @Operation(summary = "重试失败文档", description = "重试所有处理失败的文档")
    public ResponseEntity<Map<String, Object>> retryFailedDocuments() {
        try {
            documentService.retryFailedDocuments();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "失败文档重试已提交"
            ));

        } catch (Exception e) {
            log.error("重试失败文档出错", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "重试失败: " + e.getMessage()
                    ));
        }
    }
} 