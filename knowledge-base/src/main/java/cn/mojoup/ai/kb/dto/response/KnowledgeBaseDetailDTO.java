package cn.mojoup.ai.kb.dto.response;

import cn.mojoup.ai.kb.entity.KnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 知识库详情DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseDetailDTO {

    private String id;
    private String name;
    private String description;
    private KnowledgeBase.KnowledgeBaseType type;
    private KnowledgeBase.AccessLevel accessLevel;
    private String tags;
    private Map<String, Object> metadata;
    private Boolean enabled;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    
    // 统计信息
    private Long documentCount;
    private Long totalSize;
    private LocalDateTime lastModified;
} 