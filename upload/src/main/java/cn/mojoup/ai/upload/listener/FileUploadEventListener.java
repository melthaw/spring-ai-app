package cn.mojoup.ai.upload.listener;

import cn.mojoup.ai.upload.domain.FileUploadEvent;
import cn.mojoup.ai.upload.service.FileInfoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文件上传事件监听器
 * 负责处理文件上传事件，将文件信息持久化到存储服务
 * 
 * @author matt
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadEventListener {
    
    private final FileInfoStorageService fileInfoStorageService;
    
    /**
     * 处理文件上传成功事件
     */
    @Async("fileUploadEventExecutor")
    @EventListener
    public void handleFileUploadedEvent(FileUploadEvent event) {
        if (event.getEventType() == FileUploadEvent.EventType.FILE_UPLOADED) {
            try {
                log.info("Processing file upload event: eventId={}, fileId={}, fileName={}", 
                        event.getEventId(), 
                        event.getFileInfo().getFileId(), 
                        event.getFileInfo().getOriginalFileName());
                
                // 保存文件信息到存储服务
                fileInfoStorageService.saveFileInfo(event.getFileInfo());
                
                log.info("File info saved successfully: fileId={}", event.getFileInfo().getFileId());
                
            } catch (Exception e) {
                log.error("Failed to save file info for event: eventId={}, fileId={}", 
                        event.getEventId(), 
                        event.getFileInfo().getFileId(), e);
                
                // 这里可以考虑重试机制或者发送警告通知
                // 例如：发送到死信队列、记录到错误日志表等
            }
        }
    }
    
    /**
     * 处理批量文件上传成功事件
     */
    @Async("fileUploadEventExecutor")
    @EventListener
    public void handleBatchFilesUploadedEvent(FileUploadEvent event) {
        if (event.getEventType() == FileUploadEvent.EventType.BATCH_FILES_UPLOADED) {
            try {
                // 对于批量上传，extraData中包含文件列表
                if (event.getExtraData() instanceof java.util.List<?> fileInfos) {
                    @SuppressWarnings("unchecked")
                    java.util.List<cn.mojoup.ai.upload.domain.FileInfo> typedFileInfos = 
                            (java.util.List<cn.mojoup.ai.upload.domain.FileInfo>) fileInfos;
                    
                    log.info("Processing batch file upload event: eventId={}, fileCount={}", 
                            event.getEventId(), typedFileInfos.size());
                    
                    // 批量保存文件信息
                    fileInfoStorageService.saveFileInfos(typedFileInfos);
                    
                    log.info("Batch file infos saved successfully: eventId={}, fileCount={}", 
                            event.getEventId(), typedFileInfos.size());
                }
                
            } catch (Exception e) {
                log.error("Failed to save batch file infos for event: eventId={}", 
                        event.getEventId(), e);
            }
        }
    }
    
    /**
     * 处理文件删除事件
     */
    @Async("fileUploadEventExecutor")
    @EventListener
    public void handleFileDeletedEvent(FileUploadEvent event) {
        if (event.getEventType() == FileUploadEvent.EventType.FILE_DELETED) {
            try {
                log.info("Processing file delete event: eventId={}, fileId={}", 
                        event.getEventId(), 
                        event.getFileInfo().getFileId());
                
                // 软删除文件信息
                boolean deleted = fileInfoStorageService.softDeleteByFileId(event.getFileInfo().getFileId());
                
                if (deleted) {
                    log.info("File info soft deleted successfully: fileId={}", event.getFileInfo().getFileId());
                } else {
                    log.warn("File info not found for soft delete: fileId={}", event.getFileInfo().getFileId());
                }
                
            } catch (Exception e) {
                log.error("Failed to soft delete file info for event: eventId={}, fileId={}", 
                        event.getEventId(), 
                        event.getFileInfo().getFileId(), e);
            }
        }
    }
} 