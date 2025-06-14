package cn.mojoup.ai.knowledgebase.repository.custom;

import cn.mojoup.ai.knowledgebase.domain.KnowledgeBase;
import cn.mojoup.ai.knowledgebase.domain.KnowledgeBaseSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 知识库自定义Repository接口
 * 用于实现复杂的动态查询
 * 
 * @author matt
 */
public interface KnowledgeBaseRepositoryCustom {

    /**
     * 动态搜索知识库
     */
    Page<KnowledgeBase> searchKnowledgeBases(KnowledgeBaseSearchRequest searchRequest, Pageable pageable);
} 