package cn.mojoup.ai.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 更新文件信息请求DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFileInfoRequest {
    
    private String tags;
    private Map<String, Object> metadata;
    private Boolean enabled;
} 