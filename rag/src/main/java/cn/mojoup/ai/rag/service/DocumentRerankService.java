package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 文档重排序服务接口
 * 负责对检索结果进行重排序，提高相关性
 */
public interface DocumentRerankService {

    /**
     * 对文档片段进行重排序
     *
     * @param documents 待重排序的文档片段列表
     * @param query 查询文本
     * @return 重排序后的文档片段列表
     */
    List<DocumentSegment> rerank(List<DocumentSegment> documents, String query);
} 