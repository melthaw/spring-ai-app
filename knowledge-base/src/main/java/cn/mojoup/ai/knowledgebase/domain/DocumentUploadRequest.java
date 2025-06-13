package cn.mojoup.ai.knowledgebase.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 文档上传请求
 * 
 * @author matt
 */
@Data
public class DocumentUploadRequest {

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 500, message = "文档标题长度不能超过500个字符")
    private String title;

    /**
     * 文档描述
     */
    @Size(max = 2000, message = "文档描述长度不能超过2000个字符")
    private String description;

    /**
     * 所属分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 文档标签（逗号分隔）
     */
    private String tags;

    /**
     * 文档访问权限
     */
    private KnowledgeDocument.AccessLevel accessLevel = KnowledgeDocument.AccessLevel.PRIVATE;

    /**
     * 排序顺序
     */
    private Integer sortOrder = 0;

    /**
     * 上传用户ID
     */
    @NotBlank(message = "上传用户ID不能为空")
    private String uploadUserId;

    /**
     * 是否立即处理嵌入
     */
    private Boolean immediateEmbedding = true;

    /**
     * 嵌入模型名称
     */
    private String embeddingModel = "text-embedding-ada-002";

    /**
     * 分块策略
     */
    private DocumentEmbedding.ChunkStrategy chunkStrategy = DocumentEmbedding.ChunkStrategy.FIXED_SIZE;

    /**
     * 分块大小
     */
    private Integer chunkSize = 1000;

    /**
     * 分块重叠大小
     */
    private Integer chunkOverlap = 200;

    /**
     * 扩展属性（JSON格式）
     */
    private String metadata;
} 