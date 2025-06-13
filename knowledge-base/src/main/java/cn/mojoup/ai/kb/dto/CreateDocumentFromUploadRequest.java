package cn.mojoup.ai.kb.dto;

import cn.mojoup.ai.kb.entity.DocumentNode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从上传文件创建文档节点请求DTO
 *
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentFromUploadRequest {

    /**
     * 上传文件ID
     */
    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    /**
     * 节点名称（可选，默认使用文件名）
     */
    private String nodeName;

    /**
     * 文档描述
     */
    private String description;

    /**
     * 标签（JSON格式）
     */
    private String tags;

    /**
     * 扩展属性（JSON格式）
     */
    private String attributes;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 访问级别
     */
    private DocumentNode.AccessLevel accessLevel;
} 