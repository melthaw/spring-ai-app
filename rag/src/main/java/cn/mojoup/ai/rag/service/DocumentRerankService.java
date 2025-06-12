package cn.mojoup.ai.rag.service;

import cn.mojoup.ai.rag.domain.DocumentSegment;

import java.util.List;

/**
 * 文档重排序服务接口
 * 提供多种重排序策略，提升搜索结果的相关性
 *
 * @author matt
 */
public interface DocumentRerankService {

    /**
     * 对文档列表进行重排序
     *
     * @param documents 待重排序的文档列表
     * @param query     用户查询
     * @return 重排序后的文档列表
     */
    List<DocumentSegment> rerankDocuments(List<DocumentSegment> documents, String query);

    /**
     * 使用AI模型进行重排序
     *
     * @param documents 待重排序的文档列表
     * @param query     用户查询
     * @return 重排序后的文档列表
     */
    List<DocumentSegment> aiRerank(List<DocumentSegment> documents, String query);

    /**
     * 使用Cross-Encoder模型进行重排序
     *
     * @param documents 待重排序的文档列表
     * @param query     用户查询
     * @return 重排序后的文档列表
     */
    List<DocumentSegment> crossEncoderRerank(List<DocumentSegment> documents, String query);

    /**
     * 使用简单算法进行重排序
     *
     * @param documents 待重排序的文档列表
     * @param query     用户查询
     * @return 重排序后的文档列表
     */
    List<DocumentSegment> simpleRerank(List<DocumentSegment> documents, String query);

    /**
     * 获取可用的重排序策略列表
     *
     * @return 重排序策略名称列表
     */
    List<String> getAvailableStrategies();

    /**
     * 使用指定策略进行重排序
     *
     * @param documents 待重排序的文档列表
     * @param query     用户查询
     * @param strategy  重排序策略 (ai, cross_encoder, simple)
     * @return 重排序后的文档列表
     */
    List<DocumentSegment> rerankWithStrategy(List<DocumentSegment> documents, String query, String strategy);
} 