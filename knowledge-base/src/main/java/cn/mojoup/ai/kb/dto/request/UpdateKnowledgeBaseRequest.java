package cn.mojoup.ai.kb.dto.request;

import cn.mojoup.ai.kb.entity.KnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * 更新知识库请求DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKnowledgeBaseRequest {

    /**
     * 知识库名称
     */
    @Size(max = 100, message = "知识库名称不能超过100个字符")
    private String name;

    /**
     * 知识库描述
     */
    @Size(max = 500, message = "知识库描述不能超过500个字符")
    private String description;

    /**
     * 知识库类型
     */
    private KnowledgeBase.KnowledgeBaseType type;

    /**
     * 访问级别
     */
    private KnowledgeBase.AccessLevel accessLevel;

    /**
     * 标签
     */
    private String tags;

    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;

    /**
     * 是否启用
     */
    private Boolean enabled;
} 