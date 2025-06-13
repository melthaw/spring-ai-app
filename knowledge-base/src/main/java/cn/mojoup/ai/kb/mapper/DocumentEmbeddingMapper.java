package cn.mojoup.ai.kb.mapper;

import cn.mojoup.ai.kb.config.MapStructConfig;
import cn.mojoup.ai.kb.dto.request.CreateDocumentEmbeddingRequest;
import cn.mojoup.ai.kb.dto.response.DocumentEmbeddingDTO;
import cn.mojoup.ai.kb.entity.DocumentEmbedding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * DocumentEmbedding 对象转换器
 * 
 * @author matt
 */
@Mapper(config = MapStructConfig.class)
public interface DocumentEmbeddingMapper {

    /**
     * DocumentEmbedding -> DocumentEmbeddingDTO
     */
    DocumentEmbeddingDTO toDTO(DocumentEmbedding documentEmbedding);

    /**
     * DocumentEmbedding列表 -> DocumentEmbeddingDTO列表
     */
    List<DocumentEmbeddingDTO> toDTOList(List<DocumentEmbedding> documentEmbeddings);

    /**
     * CreateDocumentEmbeddingRequest -> DocumentEmbedding
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentNode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    DocumentEmbedding fromCreateRequest(CreateDocumentEmbeddingRequest request);
} 