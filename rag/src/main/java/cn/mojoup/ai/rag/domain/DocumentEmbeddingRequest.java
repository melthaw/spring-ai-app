package cn.mojoup.ai.rag.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.Map;

/**
 * 文档嵌入请求
 *
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEmbeddingRequest {

    /**
     * 文件ID（来自upload模块）
     */
    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;

    /**
     * 知识库名称
     */
    private String knowledgeBaseName;

    /**
     * 嵌入模型
     */
    private String embeddingModel = "text-embedding-ada-002";

    /**
     * 文档分块大小
     */
    @Min(value = 100, message = "分块大小不能小于100")
    @Max(value = 10000, message = "分块大小不能大于10000")
    private Integer chunkSize = 1000;

    /**
     * 分块重叠大小
     */
    @Min(value = 0, message = "重叠大小不能小于0")
    @Max(value = 500, message = "重叠大小不能大于500")
    private Integer chunkOverlap = 200;

    /**
     * 是否保留文档元数据
     */
    private Boolean preserveMetadata = true;

    /**
     * 是否启用文档清理
     */
    private Boolean enableCleaning = true;

    /**
     * 自定义标签
     */
    private String[] tags;

    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;

    /**
     * 处理模式
     */
    private ProcessingMode processingMode = ProcessingMode.SYNC;

    /**
     * 处理模式枚举
     */
    public enum ProcessingMode {
        SYNC,   // 同步处理
        ASYNC   // 异步处理
    }
} 