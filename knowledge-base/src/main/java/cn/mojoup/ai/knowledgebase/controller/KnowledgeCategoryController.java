package cn.mojoup.ai.knowledgebase.controller;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeCategory;
import cn.mojoup.ai.knowledgebase.service.KnowledgeCategoryService;
import cn.mojoup.ai.knowledgebase.service.KnowledgeCategoryService.CreateCategoryRequest;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 知识分类管理控制器
 * 
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge-base/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "知识分类管理", description = "知识库分类的树形结构管理相关接口")
public class KnowledgeCategoryController {

    private final KnowledgeCategoryService categoryService;

    @PostMapping
    @Operation(summary = "创建分类", description = "创建一个新的知识分类")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "409", description = "分类名称已存在")
    })
    public ResponseEntity<KnowledgeCategory> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        
        log.info("创建知识分类: name={}, kbId={}, parentId={}", 
                request.categoryName(), request.knowledgeBaseId(), request.parentId());
        
        try {
            KnowledgeCategory category = categoryService.createCategory(request);
            return ResponseEntity.ok(category);
            
        } catch (IllegalArgumentException e) {
            log.error("创建分类参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("创建分类失败: name={}", request.categoryName(), e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "创建分类失败: " + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新分类信息")
    public ResponseEntity<KnowledgeCategory> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        
        log.info("更新知识分类: id={}, name={}", id, request.categoryName());
        
        try {
            KnowledgeCategory category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(category);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新分类失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .header("Error-Message", "更新分类失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    public ResponseEntity<KnowledgeCategory> getCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "逻辑删除分类")
    public ResponseEntity<Map<String, Object>> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {
        
        log.info("删除知识分类: id={}, operatorId={}", id, operatorId);
        
        try {
            categoryService.deleteCategory(id, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类删除成功",
                    "categoryId", id
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "categoryId", id
                    ));
        } catch (Exception e) {
            log.error("删除分类失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "删除分类失败: " + e.getMessage(),
                            "categoryId", id
                    ));
        }
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复分类", description = "恢复已删除的分类")
    public ResponseEntity<Map<String, Object>> restoreCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {
        
        try {
            categoryService.restoreCategory(id, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类恢复成功",
                    "categoryId", id
            ));
            
        } catch (Exception e) {
            log.error("恢复分类失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "恢复分类失败: " + e.getMessage(),
                            "categoryId", id
                    ));
        }
    }

    @PostMapping("/{id}/move")
    @Operation(summary = "移动分类", description = "移动分类到新的父分类下")
    public ResponseEntity<Map<String, Object>> moveCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Parameter(description = "新父分类ID") @RequestParam(required = false) Long newParentId,
            @Parameter(description = "操作者ID") @RequestParam String operatorId) {
        
        log.info("移动知识分类: categoryId={}, newParentId={}, operatorId={}", id, newParentId, operatorId);
        
        try {
            categoryService.moveCategory(id, newParentId, operatorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类移动成功",
                    "categoryId", id,
                    "newParentId", newParentId
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage(),
                            "categoryId", id
                    ));
        } catch (Exception e) {
            log.error("移动分类失败: id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "移动分类失败: " + e.getMessage(),
                            "categoryId", id
                    ));
        }
    }

    @GetMapping("/tree/knowledge-base/{kbId}")
    @Operation(summary = "获取知识库分类树", description = "获取指定知识库的完整分类树结构")
    public ResponseEntity<List<KnowledgeCategory>> getCategoryTree(
            @Parameter(description = "知识库ID") @PathVariable Long kbId) {
        
        List<KnowledgeCategory> categoryTree = categoryService.getCategoryTree(kbId);
        return ResponseEntity.ok(categoryTree);
    }

    @GetMapping("/top-level/knowledge-base/{kbId}")
    @Operation(summary = "获取顶级分类", description = "获取指定知识库的顶级分类列表")
    public ResponseEntity<List<KnowledgeCategory>> getTopLevelCategories(
            @Parameter(description = "知识库ID") @PathVariable Long kbId) {
        
        List<KnowledgeCategory> topLevelCategories = categoryService.getTopLevelCategories(kbId);
        return ResponseEntity.ok(topLevelCategories);
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子分类", description = "获取指定分类的直接子分类")
    public ResponseEntity<List<KnowledgeCategory>> getChildCategories(
            @Parameter(description = "父分类ID") @PathVariable Long parentId) {
        
        List<KnowledgeCategory> childCategories = categoryService.getChildCategories(parentId);
        return ResponseEntity.ok(childCategories);
    }

    @GetMapping("/{id}/path")
    @Operation(summary = "获取分类路径", description = "获取从根分类到指定分类的完整路径")
    public ResponseEntity<Map<String, Object>> getCategoryPath(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        String path = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(Map.of(
                "categoryId", id,
                "path", path
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索分类", description = "在指定知识库中搜索分类")
    public ResponseEntity<List<KnowledgeCategory>> searchCategories(
            @Parameter(description = "知识库ID") @RequestParam Long kbId,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        
        List<KnowledgeCategory> categories = categoryService.searchCategories(kbId, keyword);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/check-name")
    @Operation(summary = "检查分类名称可用性", description = "检查在指定位置下分类名称是否可用")
    public ResponseEntity<Map<String, Object>> checkCategoryNameAvailable(
            @Parameter(description = "知识库ID") @RequestParam Long kbId,
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "分类名称") @RequestParam String categoryName) {
        
        boolean available = categoryService.isCategoryNameAvailable(kbId, parentId, categoryName);
        return ResponseEntity.ok(Map.of(
                "kbId", kbId,
                "parentId", parentId,
                "categoryName", categoryName,
                "available", available,
                "message", available ? "名称可用" : "名称已被使用"
        ));
    }
} 