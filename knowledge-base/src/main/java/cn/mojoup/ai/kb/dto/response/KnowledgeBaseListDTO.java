package cn.mojoup.ai.kb.dto.response;

import cn.mojoup.ai.kb.entity.KnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库列表DTO
 * 
 * @author matt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseListDTO {

    private String id;
    private String name;
    private String description;
    private KnowledgeBase.KnowledgeBaseType type;
    private KnowledgeBase.AccessLevel accessLevel;
    private String tags;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private Long documentCount;
    private Long totalSize;
} 