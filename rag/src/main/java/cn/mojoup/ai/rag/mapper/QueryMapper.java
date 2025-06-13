package cn.mojoup.ai.rag.mapper;

import cn.mojoup.ai.rag.config.MapStructConfig;
import cn.mojoup.ai.rag.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * RAG查询对象转换器
 * 
 * @author matt
 */
@Mapper(config = MapStructConfig.class)
public interface QueryMapper {

    /**
     * DocumentEmbeddingRequest -> DocumentEmbeddingResponse (初始化)
     */
    @Mapping(target = "processingId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "startTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "processingTime", ignore = true)
    @Mapping(target = "segmentCount", ignore = true)
    @Mapping(target = "successCount", ignore = true)
    @Mapping(target = "failureCount", ignore = true)
    @Mapping(target = "segments", ignore = true)
    @Mapping(target = "errors", ignore = true)
    @Mapping(target = "message", constant = "Processing started")
    DocumentEmbeddingResponse toInitialResponse(DocumentEmbeddingRequest request);

    /**
     * BatchQueryRequest -> BatchQueryResponse (初始化)
     */
    @Mapping(target = "batchId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "results", ignore = true)
    @Mapping(target = "totalQuestions", source = "questions", qualifiedByName = "listSize")
    @Mapping(target = "successCount", ignore = true)
    @Mapping(target = "failureCount", ignore = true)
    @Mapping(target = "totalProcessingTime", ignore = true)
    BatchQueryResponse toInitialBatchResponse(BatchQueryRequest request);

    /**
     * QueryHistoryRequest -> QueryHistoryResponse (初始化)
     */
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "totalCount", ignore = true)
    QueryHistoryResponse toInitialHistoryResponse(QueryHistoryRequest request);

    /**
     * RelatedQueryRequest -> RelatedQueryResponse (初始化)
     */
    @Mapping(target = "relatedQueries", ignore = true)
    RelatedQueryResponse toInitialRelatedResponse(RelatedQueryRequest request);

    /**
     * QuerySuggestionRequest -> QuerySuggestionResponse (初始化)
     */
    @Mapping(target = "suggestions", ignore = true)
    QuerySuggestionResponse toInitialSuggestionResponse(QuerySuggestionRequest request);

    /**
     * 通用方法：获取列表大小
     */
    @org.mapstruct.Named("listSize")
    default Integer getListSize(List<?> list) {
        return list != null ? list.size() : 0;
    }

    /**
     * 通用方法：初始化BaseQueryResponse字段
     */
    default void initializeBaseResponse(BaseQueryResponse response, BaseQueryRequest request) {
        response.setQueryId(java.util.UUID.randomUUID().toString());
        response.setQuestion(request.getQuestion());
        response.setQueryTime(java.time.LocalDateTime.now());
        response.setSuccess(true);
    }
} 