package cn.mojoup.ai.upload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 文件上传请求模型
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {
    
    /**
     * 上传用户ID
     */
    private String uploadUserId;
    
    /**
     * 文件标签
     */
    private String tags;
    
    /**
     * 自定义存储目录（相对路径）
     */
    private String customPath;
    
    /**
     * 是否覆盖同名文件
     */
    @Builder.Default
    private Boolean overwrite = false;
    
    /**
     * 是否生成唯一文件名
     */
    @Builder.Default
    private Boolean generateUniqueName = true;
    
    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
} 