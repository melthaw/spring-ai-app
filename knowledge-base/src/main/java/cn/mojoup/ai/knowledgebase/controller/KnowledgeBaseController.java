package cn.mojoup.ai.knowledgebase.controller;

import cn.mojoup.ai.knowledgebase.domain.CreateKnowledgeBaseRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeBase;
import cn.mojoup.ai.knowledgebase.service.KnowledgeBaseService;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 * 
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge-base")
@RequiredArgsConstructor
@Validated
@Tag(name = "知识库管理", description = "企业级知识库管理相关接口")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    @Operation(summary = "创建知识库", description = "创建一个新的知识库")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "409", description = "知识库编码已存在")
    })
    public ResponseEntity<KnowledgeBase> createKnowledgeBase(
            @Valid @RequestBody CreateKnowledgeBaseRequest request) {
        
        log.info("创建知识库: kbCode={}, kbName={}", request.getKbCode(), request.getKbName());
        
        try {
            // 检查编码是否可用
            if (!knowledgeBaseService.isKbCodeAvailable(request.getKbCode())) {
                return ResponseEntity.badRequest()
                        .header("Error-Message", "知识库编码已存在: " + request.getKbCode())
                        .build();
            }

            KnowledgeBase knowledgeBase = knowledgeBaseService.createKnowledgeBase(request);
            return ResponseEntity.ok(knowledgeBase);
            
        } catch (Exception e) {
            log.error("创建知识库失败: kbCode={}", request.getKbCode(), e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "创建知识库失败: " + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新知识库", description = "更新知识库信息")
    public ResponseEntity<KnowledgeBase> updateKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @Valid @RequestBody CreateKnowledgeBaseRequest request) {
        
        log.info("更新知识库: id={}, kbName={}", id, request.getKbName());
        
        try {
            KnowledgeBase knowledgeBase = knowledgeBaseService.updateKnowledgeBase(id, request);
            return ResponseEntity.ok(knowledgeBase);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新知识库失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "更新知识库失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库详细信息")
    public ResponseEntity<KnowledgeBase> getKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        
        return knowledgeBaseService.getKnowledgeBaseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{kbCode}")
    @Operation(summary = "根据编码获取知识库", description = "根据知识库编码获取详细信息")
    public ResponseEntity<KnowledgeBase> getKnowledgeBaseByCode(
            @Parameter(description = "知识库编码") @PathVariable String kbCode) {
        
        return knowledgeBaseService.getKnowledgeBaseByCode(kbCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识库", description = "逻辑删除知识库")
    public ResponseEntity<Map<String, Object>> deleteKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {
        
        log.info("删除知识库: id={}, operatorId={}", id, operatorId);
        
        try {
            knowledgeBaseService.deleteKnowledgeBase(id, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "知识库删除成功",
                    "kbId", id
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("删除知识库失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "删除知识库失败: " + e.getMessage(),
                            "kbId", id
                    ));
        }
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "归档知识库", description = "将知识库设置为归档状态")
    public ResponseEntity<Map<String, Object>> archiveKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {
        
        try {
            knowledgeBaseService.archiveKnowledgeBase(id, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "知识库归档成功",
                    "kbId", id
            ));
            
        } catch (Exception e) {
            log.error("归档知识库失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "归档知识库失败: " + e.getMessage(),
                            "kbId", id
                    ));
        }
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复知识库", description = "将归档的知识库恢复为活跃状态")
    public ResponseEntity<Map<String, Object>> restoreKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {
        
        try {
            knowledgeBaseService.restoreKnowledgeBase(id, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "知识库恢复成功",
                    "kbId", id
            ));
            
        } catch (Exception e) {
            log.error("恢复知识库失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "恢复知识库失败: " + e.getMessage(),
                            "kbId", id
                    ));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户知识库列表", description = "获取指定用户的知识库列表")
    public ResponseEntity<List<KnowledgeBase>> getUserKnowledgeBases(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.getUserKnowledgeBases(userId);
        return ResponseEntity.ok(knowledgeBases);
    }

    @GetMapping
    @Operation(summary = "分页获取知识库列表", description = "分页获取知识库列表")
    public ResponseEntity<Page<KnowledgeBase>> getKnowledgeBasesPage(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeBase> knowledgeBasesPage = knowledgeBaseService.getKnowledgeBasesPage(pageable);
        return ResponseEntity.ok(knowledgeBasesPage);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索知识库", description = "根据关键词搜索知识库")
    public ResponseEntity<Page<KnowledgeBase>> searchKnowledgeBases(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeBase> result = knowledgeBaseService.searchKnowledgeBases(keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}/search")
    @Operation(summary = "用户搜索知识库", description = "在用户的知识库中搜索")
    public ResponseEntity<Page<KnowledgeBase>> searchUserKnowledgeBases(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeBase> result = knowledgeBaseService.searchUserKnowledgeBases(userId, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/public")
    @Operation(summary = "获取公开知识库", description = "获取公开的知识库列表")
    public ResponseEntity<Page<KnowledgeBase>> getPublicKnowledgeBases(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeBase> result = knowledgeBaseService.getPublicKnowledgeBases(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取知识库统计信息", description = "获取知识库的统计数据")
    public ResponseEntity<KnowledgeBaseService.KnowledgeBaseStats> getKnowledgeBaseStats(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        
        try {
            KnowledgeBaseService.KnowledgeBaseStats stats = knowledgeBaseService.getKnowledgeBaseStats(id);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("获取知识库统计信息失败: id={}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/code/{kbCode}/available")
    @Operation(summary = "检查知识库编码可用性", description = "检查知识库编码是否可用")
    public ResponseEntity<Map<String, Object>> checkKbCodeAvailable(
            @Parameter(description = "知识库编码") @PathVariable String kbCode) {
        
        boolean available = knowledgeBaseService.isKbCodeAvailable(kbCode);
        return ResponseEntity.ok(Map.of(
                "kbCode", kbCode,
                "available", available,
                "message", available ? "编码可用" : "编码已被使用"
        ));
    }
} 