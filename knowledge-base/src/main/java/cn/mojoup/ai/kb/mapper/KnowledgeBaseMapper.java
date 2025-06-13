package cn.mojoup.ai.kb.mapper;

import cn.mojoup.ai.kb.config.MapStructConfig;
import cn.mojoup.ai.kb.dto.request.CreateKnowledgeBaseRequest;
import cn.mojoup.ai.kb.dto.request.UpdateKnowledgeBaseRequest;
import cn.mojoup.ai.kb.dto.response.KnowledgeBaseDTO;
import cn.mojoup.ai.kb.dto.response.KnowledgeBaseDetailDTO;
import cn.mojoup.ai.kb.dto.response.KnowledgeBaseListDTO;
import cn.mojoup.ai.kb.entity.KnowledgeBase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * KnowledgeBase 对象转换器
 * 
 * @author matt
 */
@Mapper(config = MapStructConfig.class)
public interface KnowledgeBaseMapper {

    /**
     * KnowledgeBase -> KnowledgeBaseDTO
     */
    KnowledgeBaseDTO toDTO(KnowledgeBase knowledgeBase);

    /**
     * KnowledgeBase列表 -> KnowledgeBaseDTO列表
     */
    List<KnowledgeBaseDTO> toDTOList(List<KnowledgeBase> knowledgeBases);

    /**
     * KnowledgeBase -> KnowledgeBaseDetailDTO
     */
    KnowledgeBaseDetailDTO toDetailDTO(KnowledgeBase knowledgeBase);

    /**
     * KnowledgeBase -> KnowledgeBaseListDTO
     */
    KnowledgeBaseListDTO toListDTO(KnowledgeBase knowledgeBase);

    /**
     * KnowledgeBase列表 -> KnowledgeBaseListDTO列表
     */
    List<KnowledgeBaseListDTO> toListDTOList(List<KnowledgeBase> knowledgeBases);

    /**
     * CreateKnowledgeBaseRequest -> KnowledgeBase
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    KnowledgeBase fromCreateRequest(CreateKnowledgeBaseRequest request);

    /**
     * UpdateKnowledgeBaseRequest -> KnowledgeBase (部分更新)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateFromRequest(UpdateKnowledgeBaseRequest request, @MappingTarget KnowledgeBase knowledgeBase);
} 