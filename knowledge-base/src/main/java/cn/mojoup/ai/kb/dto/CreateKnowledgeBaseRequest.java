package cn.mojoup.ai.kb.dto;

import cn.mojoup.ai.kb.entity.KnowledgeBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建知识库请求DTO
 *
 * @author matt
 */
@Data
public class CreateKnowledgeBaseRequest {

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称长度不能超过100字符")
    private String kbName;

    /**
     * 知识库描述
     */
    @Size(max = 500, message = "知识库描述长度不能超过500字符")
    private String description;

    /**
     * 知识库类型
     */
    @NotNull(message = "知识库类型不能为空")
    private KnowledgeBase.KnowledgeBaseType kbType;

    /**
     * 所属组织ID
     */
    @NotBlank(message = "组织ID不能为空")
    private String organizationId;

    /**
     * 所属部门ID
     */
    private String departmentId;

    /**
     * 访问级别
     */
    @NotNull(message = "访问级别不能为空")
    private KnowledgeBase.AccessLevel accessLevel;

    /**
     * 是否启用版本控制
     */
    private Boolean versionEnabled = false;

    /**
     * 默认嵌入模型
     */
    private String defaultEmbeddingModel = "text-embedding-ada-002";

    /**
     * 默认分块大小
     */
    private Integer defaultChunkSize = 1000;

    /**
     * 默认分块重叠
     */
    private Integer defaultChunkOverlap = 200;

    /**
     * 管理员ID列表
     */
    private String adminIds;

    /**
     * 初始权限设置
     */
    private InitialPermissionSettings initialPermissions;

    @Data
    public static class InitialPermissionSettings {
        /**
         * 是否允许组织内部访问
         */
        private Boolean allowOrganizationAccess = true;

        /**
         * 是否允许部门访问
         */
        private Boolean allowDepartmentAccess = true;

        /**
         * 默认用户权限
         */
        private String defaultUserPermissions = "READ,QUERY";

        /**
         * 默认部门权限
         */
        private String defaultDepartmentPermissions = "READ,QUERY";
    }
} 