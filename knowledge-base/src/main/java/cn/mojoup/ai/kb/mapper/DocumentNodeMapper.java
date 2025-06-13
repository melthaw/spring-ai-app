package cn.mojoup.ai.kb.mapper;

import cn.mojoup.ai.kb.config.MapStructConfig;
import cn.mojoup.ai.kb.dto.request.CreateDocumentNodeRequest;
import cn.mojoup.ai.kb.dto.request.UpdateDocumentNodeRequest;
import cn.mojoup.ai.kb.dto.response.DocumentNodeDTO;
import cn.mojoup.ai.kb.dto.response.DocumentNodeDetailDTO;
import cn.mojoup.ai.kb.dto.response.DocumentNodeTreeDTO;
import cn.mojoup.ai.kb.entity.DocumentNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * DocumentNode 对象转换器
 * 
 * @author matt
 */
@Mapper(config = MapStructConfig.class)
public interface DocumentNodeMapper {

    /**
     * DocumentNode -> DocumentNodeDTO
     */
    DocumentNodeDTO toDTO(DocumentNode documentNode);

    /**
     * DocumentNode列表 -> DocumentNodeDTO列表
     */
    List<DocumentNodeDTO> toDTOList(List<DocumentNode> documentNodes);

    /**
     * DocumentNode -> DocumentNodeDetailDTO
     */
    DocumentNodeDetailDTO toDetailDTO(DocumentNode documentNode);

    /**
     * DocumentNode -> DocumentNodeTreeDTO
     */
    @Mapping(target = "children", ignore = true)
    DocumentNodeTreeDTO toTreeDTO(DocumentNode documentNode);

    /**
     * DocumentNode列表 -> DocumentNodeTreeDTO列表
     */
    List<DocumentNodeTreeDTO> toTreeDTOList(List<DocumentNode> documentNodes);

    /**
     * CreateDocumentNodeRequest -> DocumentNode
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgeBase", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    DocumentNode fromCreateRequest(CreateDocumentNodeRequest request);

    /**
     * UpdateDocumentNodeRequest -> DocumentNode (部分更新)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgeBase", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateFromRequest(UpdateDocumentNodeRequest request, @MappingTarget DocumentNode documentNode);
} 