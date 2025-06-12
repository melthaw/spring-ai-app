package cn.mojoup.ai.upload.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 文件上传事件
 * 
 * @author matt
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FileUploadEvent extends ApplicationEvent {
    
    /**
     * 事件ID
     */
    private String eventId;
    
    /**
     * 事件类型
     */
    private EventType eventType;
    
    /**
     * 文件信息
     */
    private FileInfo fileInfo;
    
    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 事件来源
     */
    private String sourceClass;
    
    /**
     * 额外数据
     */
    private Object extraData;
    
    /**
     * 构造函数（Spring事件必需）
     */
    public FileUploadEvent(Object source, FileInfo fileInfo, EventType eventType) {
        super(source);
        this.fileInfo = fileInfo;
        this.eventType = eventType;
        this.eventTime = LocalDateTime.now();
        this.sourceClass = source.getClass().getSimpleName();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 文件上传成功
         */
        FILE_UPLOADED("FILE_UPLOADED", "文件上传成功"),
        
        /**
         * 文件删除成功
         */
        FILE_DELETED("FILE_DELETED", "文件删除成功"),
        
        /**
         * 批量文件上传成功
         */
        BATCH_FILES_UPLOADED("BATCH_FILES_UPLOADED", "批量文件上传成功");
        
        private final String code;
        private final String description;
        
        EventType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 