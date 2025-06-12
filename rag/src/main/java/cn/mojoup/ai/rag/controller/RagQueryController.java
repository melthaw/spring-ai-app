package cn.mojoup.ai.rag.controller;

import cn.mojoup.ai.rag.domain.*;
import cn.mojoup.ai.rag.service.RagQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RAG查询控制器
 *
 * @author matt
 */
@Slf4j
@RestController
@RequestMapping("/api/rag/query")
@Tag(name = "RAG查询", description = "RAG查询相关接口")
public class RagQueryController {

    @Autowired
    private RagQueryService ragQueryService;

    // ==================== Case by Case 查询接口 ====================

    @PostMapping("/simple")
    @Operation(summary = "简单查询", description = "基于单个知识库的基础问答")
    public SimpleQueryResponse simpleQuery(@Validated @RequestBody SimpleQueryRequest request) {
        return ragQueryService.simpleQuery(request);
    }

    @PostMapping("/multi-knowledge-base")
    @Operation(summary = "多知识库查询", description = "跨多个知识库进行查询")
    public MultiKnowledgeBaseQueryResponse multiKnowledgeBaseQuery(
            @Validated @RequestBody MultiKnowledgeBaseQueryRequest request) {
        return ragQueryService.multiKnowledgeBaseQuery(request);
    }

    @PostMapping("/semantic")
    @Operation(summary = "语义查询", description = "基于向量相似度的文档检索")
    public SemanticQueryResponse semanticQuery(@Validated @RequestBody SemanticQueryRequest request) {
        return ragQueryService.semanticQuery(request);
    }

    @PostMapping("/hybrid")
    @Operation(summary = "混合查询", description = "结合关键词和语义的混合检索")
    public HybridQueryResponse hybridQuery(@Validated @RequestBody HybridQueryRequest request) {
        return ragQueryService.hybridQuery(request);
    }

    @PostMapping("/conversational")
    @Operation(summary = "对话式查询", description = "支持上下文的多轮对话")
    public ConversationalQueryResponse conversationalQuery(
            @Validated @RequestBody ConversationalQueryRequest request) {
        return ragQueryService.conversationalQuery(request);
    }

    @PostMapping("/structured")
    @Operation(summary = "结构化查询", description = "支持过滤条件的结构化检索")
    public StructuredQueryResponse structuredQuery(@Validated @RequestBody StructuredQueryRequest request) {
        return ragQueryService.structuredQuery(request);
    }

    @PostMapping("/summary")
    @Operation(summary = "摘要查询", description = "对检索结果进行摘要生成")
    public SummaryQueryResponse summaryQuery(@Validated @RequestBody SummaryQueryRequest request) {
        return ragQueryService.summaryQuery(request);
    }

    @PostMapping("/citation")
    @Operation(summary = "引用查询", description = "提供详细的引用信息和来源")
    public CitationQueryResponse citationQuery(@Validated @RequestBody CitationQueryRequest request) {
        return ragQueryService.citationQuery(request);
    }

    // ==================== All in One 查询接口 ====================

    @PostMapping("/intelligent")
    @Operation(summary = "智能查询", description = "一体化查询接口，自动选择最佳查询策略")
    public IntelligentQueryResponse intelligentQuery(@Validated @RequestBody IntelligentQueryRequest request) {
        return ragQueryService.intelligentQuery(request);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量查询", description = "支持批量问题的并行处理")
    public List<BatchQueryResponse> batchQuery(@Validated @RequestBody BatchQueryRequest request) {
        return ragQueryService.batchQuery(request);
    }

    // ==================== 辅助查询接口 ====================

    @PostMapping("/suggestions")
    @Operation(summary = "查询建议", description = "基于输入提供查询建议")
    public QuerySuggestionResponse getQuerySuggestions(@Validated @RequestBody QuerySuggestionRequest request) {
        return ragQueryService.getQuerySuggestions(request);
    }

    @PostMapping("/related")
    @Operation(summary = "相关查询", description = "基于当前查询推荐相关问题")
    public RelatedQueryResponse getRelatedQueries(@Validated @RequestBody RelatedQueryRequest request) {
        return ragQueryService.getRelatedQueries(request);
    }

    @PostMapping("/history")
    @Operation(summary = "查询历史", description = "获取用户查询历史")
    public QueryHistoryResponse getQueryHistory(@Validated @RequestBody QueryHistoryRequest request) {
        return ragQueryService.getQueryHistory(request);
    }
} 