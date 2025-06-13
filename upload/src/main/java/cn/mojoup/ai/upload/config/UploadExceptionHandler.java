package cn.mojoup.ai.upload.config;

import cn.mojoup.ai.upload.domain.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;

/**
 * 全局异常处理器
 * 
 * @author matt
 */
@Slf4j
@RestControllerAdvice
public class UploadExceptionHandler {
    
    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<UploadResponse> handleMaxSizeException(MaxUploadSizeExceededException e) {
        log.error("File upload size exceeded", e);
        
        UploadResponse response = UploadResponse.failure("File size exceeds maximum allowed size");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    /**
     * 处理文件上传相关异常
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<UploadResponse> handleMultipartException(MultipartException e) {
        log.error("Multipart upload error", e);
        
        UploadResponse response = UploadResponse.failure("File upload error: " + e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UploadResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument", e);
        
        UploadResponse response = UploadResponse.failure(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception", e);
        
        return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        
        return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Unexpected error occurred",
                "timestamp", System.currentTimeMillis()
        ));
    }
} 