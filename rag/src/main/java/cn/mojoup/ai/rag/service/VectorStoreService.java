package cn.mojoup.ai.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;

/**
 * 向量存储服务接口
 * 封装向量数据库的操作
 *
 * @author matt
 */
public interface VectorStoreService {

    /**
     * 添加文档到向量存储
     *
     * @param documents 文档列表
     * @param knowledgeBaseId 知识库ID
     */
    void addDocuments(List<Document> documents, String knowledgeBaseId);

    /**
     * 删除文档
     *
     * @param documentIds 文档ID列表
     * @param knowledgeBaseId 知识库ID
     */
    void deleteDocuments(List<String> documentIds, String knowledgeBaseId);

    /**
     * 根据文件ID删除所有相关文档
     *
     * @param fileId 文件ID
     * @param knowledgeBaseId 知识库ID
     */
    void deleteDocumentsByFileId(String fileId, String knowledgeBaseId);

    /**
     * 相似性搜索
     *
     * @param query 查询文本
     * @param knowledgeBaseId 知识库ID
     * @param topK 返回结果数量
     * @param similarityThreshold 相似度阈值
     * @return 相似文档列表
     */
    List<Document> similaritySearch(String query, String knowledgeBaseId, int topK, double similarityThreshold);

    /**
     * 向量搜索
     *
     * @param queryEmbedding 查询向量
     * @param knowledgeBaseId 知识库ID
     * @param topK 返回结果数量
     * @param similarityThreshold 相似度阈值
     * @return 相似文档列表
     */
    List<Document> vectorSearch(List<Double> queryEmbedding, String knowledgeBaseId, int topK, double similarityThreshold);

    /**
     * 高级搜索
     *
     * @param searchRequest 搜索请求
     * @param knowledgeBaseId 知识库ID
     * @return 搜索结果
     */
    List<Document> search(SearchRequest searchRequest, String knowledgeBaseId);

    /**
     * 检查知识库是否存在
     *
     * @param knowledgeBaseId 知识库ID
     * @return 是否存在
     */
    boolean knowledgeBaseExists(String knowledgeBaseId);

    /**
     * 创建知识库
     *
     * @param knowledgeBaseId 知识库ID
     * @param description 描述
     */
    void createKnowledgeBase(String knowledgeBaseId, String description);

    /**
     * 删除知识库
     *
     * @param knowledgeBaseId 知识库ID
     */
    void deleteKnowledgeBase(String knowledgeBaseId);

    /**
     * 获取知识库统计信息
     *
     * @param knowledgeBaseId 知识库ID
     * @return 统计信息
     */
    KnowledgeBaseStats getKnowledgeBaseStats(String knowledgeBaseId);

    /**
     * 知识库统计信息
     */
    class KnowledgeBaseStats {
        private String knowledgeBaseId;
        private long documentCount;
        private long totalSize;
        private String createdAt;
        private String updatedAt;

        public KnowledgeBaseStats(String knowledgeBaseId, long documentCount, long totalSize, String createdAt, String updatedAt) {
            this.knowledgeBaseId = knowledgeBaseId;
            this.documentCount = documentCount;
            this.totalSize = totalSize;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getKnowledgeBaseId() { return knowledgeBaseId; }
        public void setKnowledgeBaseId(String knowledgeBaseId) { this.knowledgeBaseId = knowledgeBaseId; }
        public long getDocumentCount() { return documentCount; }
        public void setDocumentCount(long documentCount) { this.documentCount = documentCount; }
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
} 