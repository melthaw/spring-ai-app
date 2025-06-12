package cn.mojoup.ai.rag.domain;

import lombok.Data;

import java.util.Map;

/**
 * 文档片段
 *
 * @author matt
 */
@Data
public class DocumentSegment {

    /**
     * 片段ID
     */
    private String segmentId;

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 知识库ID
     */
    private String knowledgeBaseId;

    /**
     * 片段内容
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档来源
     */
    private String source;

    /**
     * 文档类型
     */
    private String documentType;

    /**
     * 片段在文档中的位置
     */
    private Integer position;

    /**
     * 片段长度
     */
    private Integer length;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 标签
     */
    private String[] tags;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 高亮信息
     */
    private Map<String, String> highlights;
} 