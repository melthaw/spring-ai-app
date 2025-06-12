package cn.mojoup.ai.rag.controller;

import cn.mojoup.ai.rag.domain.DocumentEmbeddingRequest;
import cn.mojoup.ai.rag.domain.DocumentEmbeddingResponse;
import cn.mojoup.ai.rag.service.DocumentEmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 文档嵌入控制器
 *
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/rag/embedding")
@RequiredArgsConstructor
@Tag(name = "文档嵌入", description = "文档嵌入向量化相关接口")
public class DocumentEmbeddingController {

    private final DocumentEmbeddingService documentEmbeddingService;

    @PostMapping("/process")
    @Operation(summary = "处理文档嵌入", description = "将上传的文档进行嵌入向量化处理")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "处理成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<DocumentEmbeddingResponse> processDocument(
            @Validated @RequestBody DocumentEmbeddingRequest request) {
        
        try {
            log.info("Processing document embedding for file: {}", request.getFileId());
            
            DocumentEmbeddingResponse response;
            if (request.getProcessingMode() == DocumentEmbeddingRequest.ProcessingMode.ASYNC) {
                CompletableFuture<DocumentEmbeddingResponse> future = documentEmbeddingService.processDocumentAsync(request);
                response = DocumentEmbeddingResponse.builder()
                        .processingId(java.util.UUID.randomUUID().toString())
                        .fileId(request.getFileId())
                        .knowledgeBaseId(request.getKnowledgeBaseId())
                        .status(DocumentEmbeddingResponse.ProcessingStatus.PROCESSING)
                        .message("文档处理已开始，请稍后查询结果")
                        .build();
            } else {
                response = documentEmbeddingService.processDocument(request);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to process document embedding: {}", request.getFileId(), e);
            
            DocumentEmbeddingResponse errorResponse = DocumentEmbeddingResponse.failure(
                    java.util.UUID.randomUUID().toString(),
                    request.getFileId(),
                    request.getKnowledgeBaseId(),
                    e.getMessage()
            );
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量处理文档嵌入", description = "批量处理多个文档的嵌入向量化")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "处理成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<DocumentEmbeddingResponse>> processBatchDocuments(
            @Validated @RequestBody List<DocumentEmbeddingRequest> requests) {
        
        try {
            log.info("Processing batch document embedding for {} files", requests.size());
            
            List<DocumentEmbeddingResponse> responses = documentEmbeddingService.processDocuments(requests);
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("Failed to process batch document embedding", e);
            
            List<DocumentEmbeddingResponse> errorResponses = requests.stream()
                    .map(request -> DocumentEmbeddingResponse.failure(
                            java.util.UUID.randomUUID().toString(),
                            request.getFileId(),
                            request.getKnowledgeBaseId(),
                            e.getMessage()))
                    .toList();
            
            return ResponseEntity.ok(errorResponses);
        }
    }

    @GetMapping("/status/{processingId}")
    @Operation(summary = "查询处理状态", description = "查询异步处理任务的状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "处理任务不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<DocumentEmbeddingResponse> getProcessingStatus(
            @Parameter(description = "处理ID", required = true)
            @PathVariable String processingId) {
        
        try {
            DocumentEmbeddingResponse response = documentEmbeddingService.getProcessingStatus(processingId);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get processing status: {}", processingId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/cancel/{processingId}")
    @Operation(summary = "取消处理任务", description = "取消正在进行的处理任务")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "404", description = "处理任务不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> cancelProcessing(
            @Parameter(description = "处理ID", required = true)
            @PathVariable String processingId) {
        
        try {
            boolean cancelled = documentEmbeddingService.cancelProcessing(processingId);
            
            return ResponseEntity.ok(Map.of(
                    "success", cancelled,
                    "processingId", processingId,
                    "message", cancelled ? "处理任务已取消" : "处理任务未找到或无法取消"
            ));
            
        } catch (Exception e) {
            log.error("Failed to cancel processing: {}", processingId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "processingId", processingId,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/retry/{processingId}")
    @Operation(summary = "重试处理任务", description = "重新处理失败的任务")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "重试成功"),
            @ApiResponse(responseCode = "404", description = "处理任务不存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<DocumentEmbeddingResponse> retryProcessing(
            @Parameter(description = "处理ID", required = true)
            @PathVariable String processingId) {
        
        try {
            DocumentEmbeddingResponse response = documentEmbeddingService.retryProcessing(processingId);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retry processing: {}", processingId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/document/{fileId}")
    @Operation(summary = "删除文档嵌入", description = "删除指定文档的所有嵌入数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> deleteDocumentEmbeddings(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            @Parameter(description = "知识库ID", required = true)
            @RequestParam String knowledgeBaseId) {
        
        try {
            boolean deleted = documentEmbeddingService.deleteDocumentEmbeddings(fileId, knowledgeBaseId);
            
            return ResponseEntity.ok(Map.of(
                    "success", deleted,
                    "fileId", fileId,
                    "knowledgeBaseId", knowledgeBaseId,
                    "message", deleted ? "嵌入数据已删除" : "未找到相关嵌入数据"
            ));
            
        } catch (Exception e) {
            log.error("Failed to delete document embeddings: {}", fileId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "fileId", fileId,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/check/{fileId}")
    @Operation(summary = "检查文档嵌入状态", description = "检查文档是否已经嵌入")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> checkDocumentEmbedded(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            @Parameter(description = "知识库ID", required = true)
            @RequestParam String knowledgeBaseId) {
        
        try {
            boolean embedded = documentEmbeddingService.isDocumentEmbedded(fileId, knowledgeBaseId);
            
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "knowledgeBaseId", knowledgeBaseId,
                    "embedded", embedded,
                    "message", embedded ? "文档已嵌入" : "文档未嵌入"
            ));
            
        } catch (Exception e) {
            log.error("Failed to check document embedded status: {}", fileId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "fileId", fileId,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/supported-types")
    @Operation(summary = "获取支持的文件类型", description = "获取系统支持的文件扩展名列表")
    public ResponseEntity<Map<String, Object>> getSupportedFileTypes() {
        try {
            List<String> supportedTypes = documentEmbeddingService.getSupportedFileTypes();
            
            return ResponseEntity.ok(Map.of(
                    "supportedTypes", supportedTypes,
                    "count", supportedTypes.size()
            ));
            
        } catch (Exception e) {
            log.error("Failed to get supported file types", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/supported-models")
    @Operation(summary = "获取支持的嵌入模型", description = "获取系统支持的嵌入模型列表")
    public ResponseEntity<Map<String, Object>> getSupportedEmbeddingModels() {
        try {
            List<String> supportedModels = documentEmbeddingService.getSupportedEmbeddingModels();
            
            return ResponseEntity.ok(Map.of(
                    "supportedModels", supportedModels,
                    "count", supportedModels.size()
            ));
            
        } catch (Exception e) {
            log.error("Failed to get supported embedding models", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
} 