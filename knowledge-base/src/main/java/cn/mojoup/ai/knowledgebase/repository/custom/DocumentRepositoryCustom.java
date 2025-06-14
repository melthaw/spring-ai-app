package cn.mojoup.ai.knowledgebase.repository.custom;

import cn.mojoup.ai.knowledgebase.domain.DocumentSearchRequest;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 文档自定义Repository接口
 * 用于实现复杂的动态查询
 * 
 * @author matt
 */
public interface DocumentRepositoryCustom {

    /**
     * 动态搜索文档
     */
    Page<KnowledgeDocument> searchDocuments(DocumentSearchRequest searchRequest, Pageable pageable);
} 