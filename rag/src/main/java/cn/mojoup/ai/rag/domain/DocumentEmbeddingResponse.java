package cn.mojoup.ai.rag.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文档嵌入响应
 *
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEmbeddingResponse {

    /**
     * 处理ID
     */
    private String processingId;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 知识库ID
     */
    private String knowledgeBaseId;

    /**
     * 处理状态
     */
    private ProcessingStatus status;

    /**
     * 处理结果消息
     */
    private String message;

    /**
     * 生成的文档片段数量
     */
    private Integer segmentCount;

    /**
     * 成功嵌入的片段数量
     */
    private Integer successCount;

    /**
     * 失败的片段数量
     */
    private Integer failureCount;

    /**
     * 处理开始时间
     */
    private LocalDateTime startTime;

    /**
     * 处理结束时间
     */
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 文档片段详情
     */
    private List<EmbeddedSegment> segments;

    /**
     * 错误信息列表
     */
    private List<String> errors;

    /**
     * 扩展信息
     */
    private Map<String, Object> metadata;

    /**
     * 处理状态枚举
     */
    public enum ProcessingStatus {
        PENDING,     // 待处理
        PROCESSING,  // 处理中
        COMPLETED,   // 完成
        FAILED,      // 失败
        PARTIAL      // 部分成功
    }

    /**
     * 嵌入片段信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddedSegment {
        /**
         * 片段ID
         */
        private String segmentId;

        /**
         * 片段内容
         */
        private String content;

        /**
         * 片段长度
         */
        private Integer length;

        /**
         * 向量维度
         */
        private Integer vectorDimension;

        /**
         * 嵌入状态
         */
        private boolean embedded;

        /**
         * 错误信息
         */
        private String error;
    }

    /**
     * 创建成功响应
     */
    public static DocumentEmbeddingResponse success(String processingId, String fileId, 
                                                   String knowledgeBaseId, List<EmbeddedSegment> segments) {
        int successCount = (int) segments.stream().filter(EmbeddedSegment::isEmbedded).count();
        int failureCount = segments.size() - successCount;
        
        return DocumentEmbeddingResponse.builder()
                .processingId(processingId)
                .fileId(fileId)
                .knowledgeBaseId(knowledgeBaseId)
                .status(failureCount == 0 ? ProcessingStatus.COMPLETED : ProcessingStatus.PARTIAL)
                .message(failureCount == 0 ? "文档嵌入完成" : String.format("文档嵌入部分完成，成功 %d，失败 %d", successCount, failureCount))
                .segmentCount(segments.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .segments(segments)
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败响应
     */
    public static DocumentEmbeddingResponse failure(String processingId, String fileId, 
                                                   String knowledgeBaseId, String error) {
        return DocumentEmbeddingResponse.builder()
                .processingId(processingId)
                .fileId(fileId)
                .knowledgeBaseId(knowledgeBaseId)
                .status(ProcessingStatus.FAILED)
                .message("文档嵌入失败: " + error)
                .segmentCount(0)
                .successCount(0)
                .failureCount(0)
                .endTime(LocalDateTime.now())
                .errors(List.of(error))
                .build();
    }
} 