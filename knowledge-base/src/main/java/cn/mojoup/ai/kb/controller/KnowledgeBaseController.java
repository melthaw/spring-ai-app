package cn.mojoup.ai.kb.controller;

import cn.mojoup.ai.kb.dto.*;
import cn.mojoup.ai.kb.entity.KnowledgeBase;
import cn.mojoup.ai.kb.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 *
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
@Tag(name = "知识库管理", description = "知识库的创建、管理、权限控制等功能")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    @Operation(summary = "创建知识库", description = "创建新的知识库")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "409", description = "知识库名称已存在")
    })
    @PreAuthorize("hasRole('KB_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<KnowledgeBase> createKnowledgeBase(
            @Valid @RequestBody CreateKnowledgeBaseRequest request) {
        
        log.info("创建知识库: {}", request.getKbName());
        KnowledgeBase knowledgeBase = knowledgeBaseService.createKnowledgeBase(request);
        return ResponseEntity.ok(knowledgeBase);
    }

    @PutMapping("/{kbId}")
    @Operation(summary = "更新知识库", description = "更新知识库信息")
    @PreAuthorize("@knowledgeBaseService.hasManagePermission(#kbId, authentication.name)")
    public ResponseEntity<KnowledgeBase> updateKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String kbId,
            @Valid @RequestBody UpdateKnowledgeBaseRequest request) {
        
        log.info("更新知识库: {}", kbId);
        KnowledgeBase knowledgeBase = knowledgeBaseService.updateKnowledgeBase(kbId, request);
        return ResponseEntity.ok(knowledgeBase);
    }

    @DeleteMapping("/{kbId}")
    @Operation(summary = "删除知识库", description = "软删除知识库")
    @PreAuthorize("@knowledgeBaseService.hasManagePermission(#kbId, authentication.name)")
    public ResponseEntity<Map<String, Object>> deleteKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String kbId) {
        
        log.info("删除知识库: {}", kbId);
        knowledgeBaseService.deleteKnowledgeBase(kbId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "知识库删除成功",
                "kbId", kbId
        ));
    }

    @GetMapping("/{kbId}")
    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库详细信息")
    @PreAuthorize("@knowledgeBaseService.hasAccessPermission(#kbId, authentication.name)")
    public ResponseEntity<KnowledgeBaseDetailDTO> getKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String kbId) {
        
        KnowledgeBaseDetailDTO knowledgeBase = knowledgeBaseService.getKnowledgeBaseById(kbId);
        return ResponseEntity.ok(knowledgeBase);
    }

    @GetMapping
    @Operation(summary = "分页查询知识库", description = "分页查询用户可访问的知识库")
    public ResponseEntity<Page<KnowledgeBaseListDTO>> getKnowledgeBases(
            @PageableDefault(size = 20, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<KnowledgeBaseListDTO> knowledgeBases = knowledgeBaseService.getAccessibleKnowledgeBases(pageable);
        return ResponseEntity.ok(knowledgeBases);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "按组织查询知识库", description = "查询指定组织下的知识库")
    public ResponseEntity<List<KnowledgeBaseListDTO>> getKnowledgeBasesByOrganization(
            @Parameter(description = "组织ID") @PathVariable String organizationId) {
        
        List<KnowledgeBaseListDTO> knowledgeBases = knowledgeBaseService.getKnowledgeBasesByOrganization(organizationId);
        return ResponseEntity.ok(knowledgeBases);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "按部门查询知识库", description = "查询指定部门下的知识库")
    public ResponseEntity<List<KnowledgeBaseListDTO>> getKnowledgeBasesByDepartment(
            @Parameter(description = "部门ID") @PathVariable String departmentId) {
        
        List<KnowledgeBaseListDTO> knowledgeBases = knowledgeBaseService.getKnowledgeBasesByDepartment(departmentId);
        return ResponseEntity.ok(knowledgeBases);
    }

    @PostMapping("/search")
    @Operation(summary = "搜索知识库", description = "根据条件搜索知识库")
    public ResponseEntity<Page<KnowledgeBaseListDTO>> searchKnowledgeBases(
            @Valid @RequestBody KnowledgeBaseSearchRequest request,
            @PageableDefault(size = 20, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<KnowledgeBaseListDTO> result = knowledgeBaseService.searchKnowledgeBases(request, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{kbId}/stats")
    @Operation(summary = "获取知识库统计", description = "获取知识库的统计信息")
    @PreAuthorize("@knowledgeBaseService.hasAccessPermission(#kbId, authentication.name)")
    public ResponseEntity<KnowledgeBaseStatsDTO> getKnowledgeBaseStats(
            @Parameter(description = "知识库ID") @PathVariable String kbId) {
        
        KnowledgeBaseStatsDTO stats = knowledgeBaseService.getKnowledgeBaseStats(kbId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/overview")
    @Operation(summary = "获取用户知识库概览", description = "获取当前用户的知识库概览信息")
    public ResponseEntity<UserKnowledgeBaseOverviewDTO> getUserOverview() {
        UserKnowledgeBaseOverviewDTO overview = knowledgeBaseService.getUserKnowledgeBaseOverview();
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/{kbId}/clone")
    @Operation(summary = "复制知识库", description = "复制已有知识库创建新的知识库")
    @PreAuthorize("@knowledgeBaseService.hasAccessPermission(#kbId, authentication.name)")
    public ResponseEntity<KnowledgeBase> cloneKnowledgeBase(
            @Parameter(description = "源知识库ID") @PathVariable String kbId,
            @Valid @RequestBody CloneKnowledgeBaseRequest request) {
        
        log.info("复制知识库: {} -> {}", kbId, request.getNewKbName());
        KnowledgeBase newKnowledgeBase = knowledgeBaseService.cloneKnowledgeBase(kbId, request);
        return ResponseEntity.ok(newKnowledgeBase);
    }

    @PostMapping("/{kbId}/archive")
    @Operation(summary = "归档知识库", description = "将知识库设为归档状态")
    @PreAuthorize("@knowledgeBaseService.hasManagePermission(#kbId, authentication.name)")
    public ResponseEntity<Map<String, Object>> archiveKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String kbId) {
        
        log.info("归档知识库: {}", kbId);
        knowledgeBaseService.archiveKnowledgeBase(kbId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "知识库归档成功",
                "kbId", kbId
        ));
    }

    @PostMapping("/{kbId}/restore")
    @Operation(summary = "恢复知识库", description = "将归档的知识库恢复为活跃状态")
    @PreAuthorize("@knowledgeBaseService.hasManagePermission(#kbId, authentication.name)")
    public ResponseEntity<Map<String, Object>> restoreKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String kbId) {
        
        log.info("恢复知识库: {}", kbId);
        knowledgeBaseService.restoreKnowledgeBase(kbId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "知识库恢复成功",
                "kbId", kbId
        ));
    }

    @GetMapping("/{kbId}/export")
    @Operation(summary = "导出知识库配置", description = "导出知识库的配置信息")
    @PreAuthorize("@knowledgeBaseService.hasManagePermission(#kbId, authentication.name)")
    public ResponseEntity<KnowledgeBaseExportDTO> exportKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String kbId) {
        
        KnowledgeBaseExportDTO exportData = knowledgeBaseService.exportKnowledgeBase(kbId);
        return ResponseEntity.ok(exportData);
    }

    @PostMapping("/import")
    @Operation(summary = "导入知识库配置", description = "根据配置文件导入知识库")
    @PreAuthorize("hasRole('KB_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<KnowledgeBase> importKnowledgeBase(
            @Valid @RequestBody KnowledgeBaseImportRequest request) {
        
        log.info("导入知识库配置");
        KnowledgeBase knowledgeBase = knowledgeBaseService.importKnowledgeBase(request);
        return ResponseEntity.ok(knowledgeBase);
    }

    @GetMapping("/{kbId}/activities")
    @Operation(summary = "获取知识库活动日志", description = "获取知识库的操作活动记录")
    @PreAuthorize("@knowledgeBaseService.hasAccessPermission(#kbId, authentication.name)")
    public ResponseEntity<Page<ActivityLogDTO>> getKnowledgeBaseActivities(
            @Parameter(description = "知识库ID") @PathVariable String kbId,
            @PageableDefault(size = 20, sort = "operationTime", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ActivityLogDTO> activities = knowledgeBaseService.getKnowledgeBaseActivities(kbId, pageable);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/name-check")
    @Operation(summary = "检查知识库名称", description = "检查知识库名称是否可用")
    public ResponseEntity<Map<String, Object>> checkKnowledgeBaseName(
            @RequestParam String kbName,
            @RequestParam String organizationId,
            @RequestParam(required = false) String excludeId) {
        
        boolean available = knowledgeBaseService.isKnowledgeBaseNameAvailable(kbName, organizationId, excludeId);
        return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "名称可用" : "名称已被使用"
        ));
    }

    @GetMapping("/recommended-settings")
    @Operation(summary = "获取推荐设置", description = "根据知识库类型获取推荐的配置设置")
    public ResponseEntity<Map<String, Object>> getRecommendedSettings(
            @RequestParam String kbType,
            @RequestParam String organizationId) {
        
        Map<String, Object> settings = knowledgeBaseService.getRecommendedSettings(kbType, organizationId);
        return ResponseEntity.ok(settings);
    }
} 