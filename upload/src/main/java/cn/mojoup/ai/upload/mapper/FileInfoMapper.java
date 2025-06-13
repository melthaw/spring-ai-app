package cn.mojoup.ai.upload.mapper;

import cn.mojoup.ai.upload.config.MapStructConfig;
import cn.mojoup.ai.upload.domain.FileInfo;
import cn.mojoup.ai.upload.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * FileInfo 对象转换器
 * 
 * @author matt
 */
@Mapper(config = MapStructConfig.class)
public interface FileInfoMapper {

    /**
     * FileInfo -> FileInfoDTO
     */
    @Mapping(target = "originalFilename", source = "originalFileName")
    @Mapping(target = "storedFilename", source = "storedFileName")
    @Mapping(target = "createdAt", source = "uploadTime")
    @Mapping(target = "updatedAt", source = "uploadTime")
    FileInfoDTO toDTO(FileInfo fileInfo);

    /**
     * FileInfo列表 -> FileInfoDTO列表
     */
    List<FileInfoDTO> toDTOList(List<FileInfo> fileInfoList);

    /**
     * FileInfo -> FileInfoDetailDTO
     */
    @Mapping(target = "originalFilename", source = "originalFileName")
    @Mapping(target = "storedFilename", source = "storedFileName")
    FileInfoDetailDTO toDetailDTO(FileInfo fileInfo);

    /**
     * FileInfo -> FileInfoListDTO
     */
    @Mapping(target = "originalFilename", source = "originalFileName")
    FileInfoListDTO toListDTO(FileInfo fileInfo);

    /**
     * FileInfo列表 -> FileInfoListDTO列表
     */
    List<FileInfoListDTO> toListDTOList(List<FileInfo> fileInfoList);

    /**
     * FileInfo -> FileInfoStatsDTO
     */
    @Mapping(target = "totalFiles", constant = "1")
    @Mapping(target = "totalSize", source = "fileSize")
    @Mapping(target = "averageSize", source = "fileSize")
    FileInfoStatsDTO toStatsDTO(FileInfo fileInfo);

    /**
     * UpdateFileInfoRequest -> FileInfo (部分更新)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileId", ignore = true)
    @Mapping(target = "originalFileName", ignore = true)
    @Mapping(target = "storedFileName", ignore = true)
    @Mapping(target = "fileExtension", ignore = true)
    @Mapping(target = "relativePath", ignore = true)
    @Mapping(target = "fullPath", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "md5Hash", ignore = true)
    @Mapping(target = "sha256Hash", ignore = true)
    @Mapping(target = "storageType", ignore = true)
    @Mapping(target = "bucketName", ignore = true)
    @Mapping(target = "uploadTime", ignore = true)
    @Mapping(target = "uploadUserId", ignore = true)
    @Mapping(target = "accessUrl", ignore = true)
    @Mapping(target = "metadataJson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateFromRequest(UpdateFileInfoRequest request, @MappingTarget FileInfo fileInfo);

    /**
     * FileSearchRequest -> FileSearchCriteria (如果需要的话)
     */
    default FileSearchCriteria toSearchCriteria(FileSearchRequest request) {
        return FileSearchCriteria.builder()
                .filename(request.getFilename())
                .contentType(request.getContentType())
                .uploadedBy(request.getUploadedBy())
                .storageType(request.getStorageType())
                .tags(request.getTags())
                .minSize(request.getMinSize())
                .maxSize(request.getMaxSize())
                .uploadedAfter(request.getUploadedAfter())
                .uploadedBefore(request.getUploadedBefore())
                .build();
    }
} 