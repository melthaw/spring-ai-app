package cn.mojoup.ai.rag.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 简单查询响应
 *
 * @author matt
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleQueryResponse extends BaseQueryResponse {

    /**
     * 生成的答案
     */
    private String answer;

    /**
     * 相关文档片段
     */
    private List<DocumentSegment> documents;

    /**
     * 知识库ID
     */
    private String knowledgeBaseId;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 消耗的token数
     */
    private Integer tokensUsed;
} 