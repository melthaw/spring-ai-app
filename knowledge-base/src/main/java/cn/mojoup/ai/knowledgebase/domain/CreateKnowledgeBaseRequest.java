package cn.mojoup.ai.knowledgebase.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建知识库请求
 * 
 * @author matt
 */
@Data
public class CreateKnowledgeBaseRequest {

    /**
     * 知识库编码
     */
    @NotBlank(message = "知识库编码不能为空")
    @Size(max = 50, message = "知识库编码长度不能超过50个字符")
    private String kbCode;

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 200, message = "知识库名称长度不能超过200个字符")
    private String kbName;

    /**
     * 知识库描述
     */
    @Size(max = 2000, message = "知识库描述长度不能超过2000个字符")
    private String description;

    /**
     * 知识库图标URL
     */
    private String iconUrl;

    /**
     * 知识库封面URL
     */
    private String coverUrl;

    /**
     * 所有者ID
     */
    @NotBlank(message = "所有者ID不能为空")
    private String ownerId;

    /**
     * 所有者名称
     */
    private String ownerName;

    /**
     * 知识库类型
     */
    private KnowledgeBase.KnowledgeBaseType kbType = KnowledgeBase.KnowledgeBaseType.PERSONAL;

    /**
     * 是否公开
     */
    private Boolean isPublic = false;

    /**
     * 访问级别
     */
    private KnowledgeBase.AccessLevel accessLevel = KnowledgeBase.AccessLevel.PRIVATE;

    /**
     * 扩展属性（JSON格式）
     */
    private String metadata;
} 