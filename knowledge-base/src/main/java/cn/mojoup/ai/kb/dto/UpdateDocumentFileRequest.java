package cn.mojoup.ai.kb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新文档文件请求DTO
 *
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentFileRequest {

    /**
     * 新文件ID
     */
    @NotBlank(message = "新文件ID不能为空")
    private String newFileId;

    /**
     * 新节点名称（可选）
     */
    private String newNodeName;

    /**
     * 是否保留版本历史
     */
    private Boolean keepVersionHistory = true;

    /**
     * 更新说明
     */
    private String updateNote;
} 